package com.github.dunmatt.obo.core

import scala.util.Try

trait Message[M <: Message[M]] {
  type Factory <: MessageFactory[M]
  def factory: Class[Factory]

  def getBytes: Array[Byte]
}

trait MessageFactory[M] {
  def unpack(rdr: msgpack.MsgReader): Try[M]
}

object Message {
  // val NULL_MESSAGE = new Message {
  //   def apply(rdr: msgpack.MsgReader): Message = null  // this should never be called
  //   def getBytes: Array[Byte] = Array.empty
  // }
}
