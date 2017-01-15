package com.github.dunmatt.obo.core

import com.github.dunmatt.obo.core.msgpack.MsgReader
import java.net.URL
import org.slf4j.Logger
import org.zeromq.ZMQ
import scala.util.{ Failure, Success, Try }
import zmq.ZMQ.ZMQ_SNDMORE  // I can't help but wonder if this has anything to do with ZeroMQ

trait ComponentRunner {
  import ComponentRunner._
  protected def listeningForData: Boolean

  implicit protected val zctx = ZMQ.context(1)
  protected val serviceSocket = zctx.socket(ZMQ.REP)
  protected val servicePort = serviceSocket.bindToRandomPort("tcp://*")
  protected val broadcastSocket = zctx.socket(ZMQ.PUB)
  protected val broadcastPort = broadcastSocket.bindToRandomPort("tcp://*")
  protected val subscriptionSocket = zctx.socket(ZMQ.SUB)
  protected val metaMessageFactory = new MetaMessageFactory
  private val logName = classOf[ComponentRunner].getName
  // TODO: convert this to a TrieMap
  private var messageFactoryCache = Map.empty[String, MessageFactory[_ <: Message[_]]]

  protected def constructComponent(className: String): Try[Component] = {
    Try(Class.forName(className)).flatMap(constructComponent)
  }

  protected def constructComponent(cls: Class[_]): Try[Component] = {
    if (classOf[Component].isAssignableFrom(cls)) {
      Try {
        val component = cls.newInstance.asInstanceOf[Component]
        component.broadcastSocket = broadcastSocket
        component.broadcastPort = broadcastPort
        component.zctx = zctx
        component.topicSubscriber = topicSubscriber
        // component.setBroadcastSocket(broadcastSocket)
        component
      }
    } else {
      Failure(new Exception(s"${cls.getName} is not a subclass of Component!"))
    }
  }

  def addFactory(name: String, fac: MessageFactory[_ <: Message[_]]): Unit = {
    if (!messageFactoryCache.contains(name)) {
      messageFactoryCache = messageFactoryCache + ((name -> fac))
    }
  }

  def makeFactory(name: String): MessageFactory[_ <: Message[_]] = {  // TODO: consider moving this somewhere else
    Try {
      Class.forName(name).newInstance.asInstanceOf[MessageFactory[_ <: Message[_]]]
    }.getOrElse(new RawMessageFactory)
  }

  protected def receiveMessage(factoryName: String): Try[Message[_]] = {
    if (!messageFactoryCache.contains(factoryName)) {
      addFactory(factoryName, makeFactory(factoryName))
    }
    messageFactoryCache(factoryName).unpack(new MsgReader(serviceSocket.recv(0)))
  }

  protected def handleReceivedMessage(msg: Message[_], component: Component): Unit = {
    handleMessageInternally(msg, component).orElse(component.handleMessageBase(msg)) match {
      case Some(reply) =>
        serviceSocket.send(MetaMessage(reply.factory.getName).getBytes, ZMQ_SNDMORE)
        serviceSocket.send(reply.getBytes, 0)
      case None => serviceSocket.send(ACK)
    }
  }

  protected def handleReceivedMessage(m: Try[Message[_]], component: Component): Unit = m match {
    case Success(msg) => handleReceivedMessage(msg, component)
    case Failure(e) =>
      component.log.error(logName, s"Couldn't receive or parse main message, failed with: ", e)
      abortRecv
  }

  protected def handleReceivedMetaBytes(bytes: Array[Byte], component: Component): Unit = {
    metaMessageFactory.unpack(new MsgReader(bytes)) match {
      case Success(meta) => handleReceivedMessage(receiveMessage(meta.factoryClassName), component)
      case Failure(e) =>
        component.log.error(logName, s"""Couldn't parse [${bytes.mkString(",")}] as a MetaMessage.""", e)
        abortRecv
    }
  }

  protected def abortRecv: Unit = {
    while (serviceSocket.hasReceiveMore) {
      serviceSocket.recv
    }
    serviceSocket.send(NACK)
  }

  protected def serviceRecvStep(component: Component): Boolean = serviceSocket.recv(ZMQ.NOBLOCK) match {
    case null => false
    case bytes => handleReceivedMetaBytes(bytes, component); true
  }

  protected def subscriptionRecvStep(component: Component): Boolean = subscriptionSocket.recv(ZMQ.NOBLOCK) match {
    case null => false
    case bytes => 
      val topic = RuntimeResourceName(new String(bytes))
      messageFactoryCache(topic.name).unpack(new MsgReader(serviceSocket.recv(0))).foreach { msg =>
        component.handleSubscriptionMessage(topic, msg)
      }
      true
  }

  protected def mainLoop(component: Component): Unit = {
    component.onStart
    try {
      while (listeningForData) {
        val serviced = serviceRecvStep(component)
        val listened = subscriptionRecvStep(component)
        if (!(serviced || listened)) {
          Thread.sleep(50)  // ms
        }
      }
    } catch {
      case e: Throwable => component.log.error(logName, "Encountered an error while recv-ing: ", e)
    }
    component.onHalt
  }

  protected def handleMessageInternally(msg: Message[_], component: Component): Option[Message[_]] = msg match {
    // TODO: respond to some message types here
    case _ =>
      // log.debug(s"Got message $msg")
      None
  }

  def stop: Unit = {
    broadcastSocket.close
    serviceSocket.close
    subscriptionSocket.close
  }

  private val topicSubscriber = new TopicSubscriber {
    override def connectTo(component: ComponentMetadata) = component.capabilities match {
      case Some(ComponentCapabilities(topics)) =>
        topics.foreach { case (topic, factory) =>
          messageFactoryCache = messageFactoryCache + (topic.name -> factory.newInstance)
        }
        subscriptionSocket.connect(component.broadcastUrl.toString)
      case _ => Unit  // TODO: perhaps send a request for the info
    }

    override def subscribeTo(topic: RuntimeResourceName) {
      subscriptionSocket.subscribe(topic.name.getBytes)
    }

    override def unsubscribeFrom(topic: RuntimeResourceName) {
      subscriptionSocket.unsubscribe(topic.name.getBytes)
    }
  }
}

object ComponentRunner {
  val ACK = Array(msgpack.Constants.TRUE)
  val NACK = Array(msgpack.Constants.FALSE)
}
