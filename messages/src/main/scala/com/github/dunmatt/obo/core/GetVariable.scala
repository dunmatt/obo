package com.github.dunmatt.obo.core

import msgpack.{ MsgBuilder, MsgReader }
import scala.util.Try

case class GetVariable(name: RuntimeResourceName) extends Message[GetVariable] {
  type Factory = GetVariableFactory
  def factory = classOf[GetVariableFactory]

  def getBytes: Array[Byte] = (new MsgBuilder).putString(name.name).getBytes
}

class GetVariableFactory extends MessageFactory[GetVariable] {
  def unpack(rdr: MsgReader): Try[GetVariable] = {
    for {
      name <- rdr.getString(0)
    } yield {
      GetVariable(RuntimeResourceName(name))
    }
  }
}

case class SetVariable(name: RuntimeResourceName, value: String) extends Message[SetVariable] {
  type Factory = SetVariableFactory
  def factory = classOf[SetVariableFactory]
  def getBytes: Array[Byte] = (new MsgBuilder).putString(name.name).putString(value).getBytes
}

class SetVariableFactory extends MessageFactory[SetVariable] {
  def unpack(rdr: MsgReader): Try[SetVariable] = {
    for {
      name <- rdr.getString(0)
      value <- rdr.getString(1)
    } yield {
      SetVariable(RuntimeResourceName(name), value)
    }
  }
}

case class VariableValue(name: RuntimeResourceName, value: String) extends Message[VariableValue] {
  type Factory = VariableValueFactory
  def factory = classOf[VariableValueFactory]
  def getBytes: Array[Byte] = (new MsgBuilder).putString(name.name).putString(value).getBytes
}

class VariableValueFactory extends MessageFactory[VariableValue] {
  def unpack(rdr: MsgReader): Try[VariableValue] = {
    for {
      name <- rdr.getString(0)
      value <- rdr.getString(1)
    } yield {
      VariableValue(RuntimeResourceName(name), value)
    }
  }
}
