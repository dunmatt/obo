package com.github.dunmatt.obo.core

import com.github.dunmatt.obo.core.serial.{ SerialPort, SerialPortFactory, SerialPortRequest }
import java.util.UUID
import org.joda.time.Instant
import org.slf4j.event.Level
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ
import scala.concurrent.Future
import zmq.ZMQ.ZMQ_SNDMORE

trait Component {
  val instanceId = UUID.randomUUID
  final val shortId = instanceId.getLeastSignificantBits.toInt
  private var broadcastSocket: ZMQ.Socket = null  // this is populated by the runner
  private var connectionFactory: ConnectionFactory = null  // this is populated by the runner
  private var serialPortFactory: SerialPortFactory = null  // this is populated by the runner
  val runtimeNamespace = RuntimeResourceName.ROOT  // TODO: make this mutable
  final val instanceNamespace = RuntimeResourceName(instanceId)

  val log = new OboLogger(LoggerFactory.getLogger(getClass)) {
    val logNs = runtimeNamespace / "log"
    val instLogNs = instanceNamespace / "log"

    protected def publish(name: String, level: Level, msg: String): Unit = {
      val entry = LogEntry(name, level, Instant.now, msg)
      broadcast(logNs, entry)
      broadcast(instLogNs, entry)
    }
  }

  def onHalt: Unit = {
    connectionFactory.closeEverything
    serialPortFactory.closeEverything
  }

  def handleMessage(m: Message[_]): Option[Message[_]]

  def name: String = getClass.getName  // TODO: consider changing this to only the classname and final namespace chunk

  def onStart: Unit = Unit

  def broadcast(name: RuntimeResourceName, msg: Message[_]): Unit = ???

  def connectTo(id: OboIdentifier): Future[Connection] = connectionFactory.connectTo(id)

  def requestSerialPort(req: SerialPortRequest): Future[SerialPort] = serialPortFactory.requestSerialPort(req)

  def subscribeTo(name: RuntimeResourceName): Unit = ???

  def unsubscribeFrom(name: RuntimeResourceName): Unit = ???

  def setBroadcastSocket(sock: ZMQ.Socket): Unit = broadcastSocket = sock

  def setConnectionFactory(cf: ConnectionFactory): Unit = connectionFactory = cf

  def setSerialPortFactory(spf: SerialPortFactory): Unit = serialPortFactory = spf
}
