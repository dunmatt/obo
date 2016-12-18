package com.github.dunmatt.obo.core

import scala.concurrent.Future

trait Connection {
  def close: Unit = Unit
  def send(msg: Message[_]): Future[Option[Message[_]]]
}
