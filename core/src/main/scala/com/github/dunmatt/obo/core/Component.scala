package com.github.dunmatt.obo.core

import java.util.UUID
import org.slf4j.Logger
import scala.concurrent.Future

trait Component {
  val log: Logger
  val instanceId = UUID.randomUUID

  def handleMessage(m: Message): Unit

  def sendMessage(m: Message, dest: OboIdentifier): Future[Message]

  protected[core] def processMessageQueue: Unit

  protected[core] def halt: Unit
}
