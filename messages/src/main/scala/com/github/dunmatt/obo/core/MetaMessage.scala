package com.github.dunmatt.obo.core

import msgpack.{ MsgBuilder, MsgReader }
import scala.util.Try

case class MetaMessage(factoryClassName: String) extends Message[MetaMessage] {
  type Factory = MetaMessageFactory
  def factory = classOf[MetaMessageFactory]

  def getBytes: Array[Byte] = (new MsgBuilder).putString(factoryClassName).getBytes
}

class MetaMessageFactory extends MessageFactory[MetaMessage] {
  def unpack(rdr: MsgReader): Try[MetaMessage] = {
    for {
      fcn <- rdr.getString(0)
    } yield {
      MetaMessage(fcn)
    }
  }
}
