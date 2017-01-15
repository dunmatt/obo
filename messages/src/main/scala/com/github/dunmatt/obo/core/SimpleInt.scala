package com.github.dunmatt.obo.core

import msgpack.{ MsgBuilder, MsgReader }
import scala.util.Try

case class SimpleInt(n: Int) extends Message[SimpleInt] {
  type Factory = SimpleIntFactory
  def factory = classOf[SimpleIntFactory]

  def getBytes: Array[Byte] = (new MsgBuilder).putInt(n).getBytes
}

class SimpleIntFactory() extends MessageFactory[SimpleInt] {
  def unpack(rdr: MsgReader): Try[SimpleInt] = {
    for {
      l <- rdr.getInt(0)
    } yield {
      SimpleInt(l.toInt)
    }
  }
}

