package com.github.dunmatt.obo.core

import com.github.dunmatt.obo.core.serial.{ SerialPort, SerialPortFactory, SerialPortRequest }
import java.util.UUID
import org.joda.time.Instant
import org.slf4j.event.Level
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ
import scala.concurrent.Future
import zmq.ZMQ.ZMQ_SNDMORE

trait Component extends ComponentMetadataTracker {
  private val logName = classOf[Component].getName
  val instanceId = UUID.randomUUID
  final val shortId = instanceId.getLeastSignificantBits.toInt
  private[core] var broadcastSocket: ZMQ.Socket = null  // this is populated by the runner
  // private[core] var subscriptionSocket: ZMQ.Socket = null  // this is populated by the runner
  // private var connectionFactory: ConnectionFactory = null  // this is populated by the runner
  private var serialPortFactory: SerialPortFactory = null  // this is populated by the runner
  val runtimeNamespace = RuntimeResourceName.ROOT  // TODO: make this mutable
  final val instanceNamespace = RuntimeResourceName(instanceId)
  private var advertizedTopics = Set.empty[RuntimeResourceName]

  val log = new OboLogger(LoggerFactory.getLogger(getClass)) {
    val logNs = runtimeNamespace / "log"
    val instLogNs = instanceNamespace / "log"
    advertizeTopic(logNs)
    advertizeTopic(instLogNs)

    protected def publish(name: String, level: Level, msg: String): Unit = {
      val entry = LogEntry(name, level, Instant.now, msg)
      broadcast(logNs, entry)
      broadcast(instLogNs, entry)
    }
  }

  // TODO: do the cleanup in a final method and only expose onHalt as an optional thing so it doesn't matter if they fail to call super.onHalt
  // TODO: look through the rest of core and see if there are other places that need a similar cleanup
  def onHalt: Unit = {
    // connectionFactory.closeEverything
    serialPortFactory.closeEverything
  }

  def handleMessage(m: Message[_]): Option[Message[_]]

  final def handleMessageBase(m: Message[_]): Option[Message[_]] = m match {
    case msg: ComponentCapabilitiesRequest =>
      Some(ComponentCapabilities(advertizedTopics))
    case _ =>
      handleMessage(m)
  }

  def handleSubscriptionMessage(topic: RuntimeResourceName, m: Message[_]): Unit = {
    log.error(logName, "You need to override handleSubscriptionMessage to get your subscription messages!")
  }

  def name: String = getClass.getName  // TODO: consider changing this to only the classname and final namespace chunk

  def onStart: Unit = Unit

  def broadcast(name: RuntimeResourceName, msg: Message[_]): Unit = {
    broadcastSocket.send(name.name, ZMQ_SNDMORE)
    broadcastSocket.send(msg.getBytes, 0)
  }

  def connectTo(id: OboIdentifier): Future[Connection] = ??? // connectionFactory.connectTo(id)

  def requestSerialPort(req: SerialPortRequest): Future[SerialPort] = serialPortFactory.requestSerialPort(req)

  def subscribeTo(name: RuntimeResourceName): Unit = ???

  def unsubscribeFrom(name: RuntimeResourceName): Unit = ???

  // def setConnectionFactory(cf: ConnectionFactory): Unit = connectionFactory = cf

  def setSerialPortFactory(spf: SerialPortFactory): Unit = serialPortFactory = spf

  def advertizeTopic(name: RuntimeResourceName): Unit = {
    advertizedTopics = advertizedTopics + name
    // TODO: potentially broadcast the new resource
  }
}
