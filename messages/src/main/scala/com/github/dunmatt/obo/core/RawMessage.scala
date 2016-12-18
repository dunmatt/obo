package com.github.dunmatt.obo.core

import com.github.dunmatt.obo.core.msgpack.MsgReader
import scala.util.Try

case class RawMessage(rdr: MsgReader) extends Message[RawMessage] {
  type Factory = RawMessageFactory
  val factory = classOf[RawMessageFactory]

  def getBytes: Array[Byte] = ???  // TODO: should this be allowed to be sent?
}

class RawMessageFactory extends MessageFactory[RawMessage] {
  def unpack(rdr: MsgReader): Try[RawMessage] = Try(RawMessage(rdr))
}
