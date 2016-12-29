package com.github.dunmatt.obo.core

import com.github.dunmatt.obo.core.msgpack.MsgReader
import org.slf4j.Logger
import org.zeromq.ZMQ
import scala.util.{ Failure, Success, Try }
import zmq.ZMQ.ZMQ_SNDMORE  // I can't help but wonder if this has anything to do with ZeroMQ

trait ComponentRunner {
  import ComponentRunner._
  protected def socket: ZMQ.Socket
  protected def listeningForData: Boolean
  protected val metaMessageFactory = new MetaMessageFactory
  private val logName = classOf[ComponentRunner].getName
  private var factoryCache = Map.empty[String, MessageFactory[_ <: Message[_]]]

  protected def constructComponent(className: String): Try[Component] = {
    Try(Class.forName(className)).flatMap(constructComponent)
  }

  protected def constructComponent(cls: Class[_]): Try[Component] = {
    if (classOf[Component].isAssignableFrom(cls)) {
      Try(cls.newInstance.asInstanceOf[Component])
    } else {
      Failure(new Exception(s"${cls.getName} is not a subclass of Component!"))
    }
  }

  protected def makeFactory(name: String): MessageFactory[_ <: Message[_]] = {
    Try {
      Class.forName(name).newInstance.asInstanceOf[MessageFactory[_ <: Message[_]]]
    }.getOrElse(new RawMessageFactory)
  }

  protected def receiveMessage(factoryName: String): Try[Message[_]] = {
    if (!factoryCache.contains(factoryName)) {
      factoryCache = factoryCache + ((factoryName -> makeFactory(factoryName)))
    }
    factoryCache(factoryName).unpack(new MsgReader(socket.recv(0)))
  }

  protected def handleReceivedMessage(msg: Message[_], component: Component): Unit = {
    handleMessageInternally(msg).orElse(component.handleMessage(msg)) match {
      case Some(reply) =>
        socket.send(MetaMessage(reply.factory.getName).getBytes, ZMQ_SNDMORE)
        socket.send(reply.getBytes, 0)
      case None => socket.send(ACK)
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
        component.log.error(logName, s"Couldn't parse [${bytes.mkString(",")}] as a MetaMessage.", e)
        abortRecv
    }
  }

  protected def abortRecv: Unit = {
    while (socket.hasReceiveMore) {
      socket.recv
    }
    socket.send(NACK)
  }

  protected def mainLoop(component: Component): Unit = {
    component.onStart
    try {
      while (listeningForData) {
        socket.recv(ZMQ.NOBLOCK) match {
          case null => Thread.sleep(50)  // ms
          case bytes => handleReceivedMetaBytes(bytes, component)
        }
      }
    } catch {
      case e: Throwable => component.log.error(logName, "Encountered an error while recv-ing: ", e)
    }
    component.onHalt
  }

  protected def handleMessageInternally(msg: Message[_]): Option[Message[_]] = msg match {
    // TODO: respond to some message types here
    case _ =>
      // log.debug(s"Got message $msg")
      None
  }
}

object ComponentRunner {
  val ACK = Array(msgpack.Constants.TRUE)
  val NACK = Array(msgpack.Constants.FALSE)
}
