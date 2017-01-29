package com.github.dunmatt.obo.core

import com.github.dunmatt.obo.core.serial.{ SerialPort, SerialPortFactory, SerialPortRequest }
import java.util.UUID
import org.joda.time.Instant
import org.slf4j.event.Level
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ
import scala.collection.concurrent.TrieMap
import scala.collection.JavaConversions._
import scala.concurrent.Future
import zmq.ZMQ.ZMQ_SNDMORE

trait Component extends ComponentMetadataTracker {
  private val logName = classOf[Component].getName
  val instanceId = UUID.randomUUID
  final val shortId = instanceId.getLeastSignificantBits.toInt
  private[core] var topicSubscriber: TopicSubscriber = null  // this is populated by the runner
  private[core] var broadcastSocket: ZMQ.Socket = null  // this is populated by the runner
  private[core] var broadcastPort: Int = -1  // this is populated by the runner
  private[core] var serialPortFactory: SerialPortFactory = null  // this is populated by the runner
  val runtimeNamespace = RuntimeResourceName.ROOT  // TODO: make this mutable and/or push it down a level
  final val instanceNamespace = RuntimeResourceName.ROOT / RuntimeResourceName(instanceId)
  private val portRequestTopic = instanceNamespace / Constants.BROADCAST_PORT_KEY
  private val parameterRequestTopic = instanceNamespace / "parameter"
  private val advertizedTopics = TrieMap.empty[RuntimeResourceName, Class[_ <: MessageFactory[_ <: Message[_]]]]
  private var topicSubscriptions = Set.empty[RuntimeResourceName]

  def parameters: Set[_ <: TypedOperationalParameter[_]] = Set.empty

  val log = new OboLogger(LoggerFactory.getLogger(getClass)) {
    val logNs = RuntimeResourceName("log")
    advertizeTopic(logNs, LogEntry.factory)

    protected def publish(name: String, level: Level, msg: String): Unit = {
      broadcast(logNs, LogEntry(name, level, Instant.now, msg))
    }
  }

  // TODO: do the cleanup in a final method and only expose onHalt as an optional thing so it doesn't matter if they fail to call super.onHalt
  // TODO: look through the rest of core and see if there are other places that need a similar cleanup
  def onHalt: Unit = {
    serialPortFactory.closeEverything
  }

  def handleMessage(m: Message[_]): Option[Message[_]]

  final def handleMessageBase(m: Message[_]): Option[Message[_]] = m match {
    case GetVariable(name) if name.startsWith(parameterRequestTopic) =>
      parameters.find(_.name == name - parameterRequestTopic).map(p => VariableValue(name, p.valueString))
    case SetVariable(name, value) if name.startsWith(parameterRequestTopic) =>
      parameters.find(_.name == name - parameterRequestTopic).foreach(_.fromString(value))
      None
    case msg: ComponentCapabilitiesRequest =>
      Some(ComponentCapabilities(advertizedTopics.readOnlySnapshot))
    case _ =>
      handleMessage(m)
  }

  def handleSubscriptionMessage(topic: RuntimeResourceName, m: Message[_]): Unit = {
    log.error(logName, "You need to override handleSubscriptionMessage to get your subscription messages!")
  }

  def name: String = getClass.getName  // TODO: consider changing this to only the classname and final namespace chunk

  def onStart: Unit = Unit

  def advertizeTopic(topic: RuntimeResourceName, factory: Class[_ <: MessageFactory[_ <: Message[_]]]): Unit = {
    if (topic.isGlobal) {
      advertizedTopics += (topic -> factory)
    } else {
      advertizedTopics += (runtimeNamespace / topic -> factory)
      advertizedTopics += (instanceNamespace / topic -> factory)
    }
    // TODO: potentially broadcast the new resource (make a message for this)
  }

  def broadcast(topic: RuntimeResourceName, msg: Message[_ <: Message[_]]): Unit = {
    if (!topicSubscriptions.contains(topic)) {
      advertizeTopic(topic, msg.factory)
    }
    // TODO: check that the message is of the appropriate type for the topic
    if (topic.isGlobal) {
      broadcast(topic.name, msg.getBytes)
    } else {
      val bytes = msg.getBytes
      broadcast((runtimeNamespace / topic).name, bytes)
      broadcast((instanceNamespace / topic).name, bytes)
    }
  }

  private def broadcast(topicName: String, data: Array[Byte]): Unit = {
    broadcastSocket.send(topicName, ZMQ_SNDMORE)
    broadcastSocket.send(data, 0)
  }

  def connectTo(id: OboIdentifier): Future[Connection] = ??? // connectionFactory.connectTo(id)

  def requestSerialPort(req: SerialPortRequest): Future[SerialPort] = serialPortFactory.requestSerialPort(req)

  def subscribeTo(topic: RuntimeResourceName): Unit = {
    topicSubscriptions = topicSubscriptions + topic
    getComponentsThatPublish(topic).foreach(topicSubscriber.connectTo)
    topicSubscriber.subscribeTo(topic)
  }

  override private[core] def onComponentDiscovered(info: ComponentMetadata): Unit = {
    info.capabilities.map(_.topics.keySet & topicSubscriptions).foreach { ts =>
      if (ts.nonEmpty) {
        topicSubscriber.connectTo(info)
      }
    }
  }

  def unsubscribeFrom(topic: RuntimeResourceName): Unit = {
    topicSubscriber.unsubscribeFrom(topic)
    topicSubscriptions = topicSubscriptions - topic
  }

  def setSerialPortFactory(spf: SerialPortFactory): Unit = serialPortFactory = spf
}
