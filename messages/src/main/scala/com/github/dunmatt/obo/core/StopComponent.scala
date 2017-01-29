package com.github.dunmatt.obo.core

import msgpack.{ MsgBuilder, MsgReader }
import scala.util.Try

case class StopComponent() extends Message[StopComponent] {
  type Factory = StopComponentFactory
  def factory = classOf[StopComponentFactory]
  def getBytes: Array[Byte] = (new MsgBuilder).putNil.getBytes
}

class StopComponentFactory extends MessageFactory[StopComponent] {
  def unpack(rdr: MsgReader): Try[StopComponent] = Try(StopComponent())
}
