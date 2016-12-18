package com.github.dunmatt.obo.core

import com.github.dunmatt.obo.core.serial.SerialPortFactory
import java.util.UUID
import org.slf4j.Logger
import scala.concurrent.Future

trait Component {
  val log: Logger
  final val instanceId = UUID.randomUUID
  var connectionFactory: ConnectionFactory = null  // this is populated by the runner
  var serialPortFactory: SerialPortFactory = null  // this is populated by the runner

  def onHalt: Unit = Unit

  def handleMessage(m: Message[_]): Option[Message[_]]

  def name: String = getClass.getName

  def onStart: Unit = Unit
}
