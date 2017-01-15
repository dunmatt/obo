package com.github.dunmatt.obo.core

import msgpack.{ MsgBuilder, MsgReader }
import scala.util.Try

case class GetVariable(name: RuntimeResourceName) extends Message[GetVariable] {
  type Factory = GetVariableFactory
  def factory = classOf[GetVariableFactory]

  def getBytes: Array[Byte] = (new MsgBuilder).putString(name.name).getBytes
}

class GetVariableFactory() extends MessageFactory[GetVariable] {
  def unpack(rdr: MsgReader): Try[GetVariable] = {
    for {
      name <- rdr.getString(0)
    } yield {
      GetVariable(RuntimeResourceName(name))
    }
  }
}
