package com.github.dunmatt.obo.core

import msgpack.{ MsgBuilder, MsgReader }
import scala.util.Try

// TODO: put actual information here
case class ComponentCapabilities(topics: Set[String]) extends Message[ComponentCapabilities] {
  type Factory = ComponentCapabilitiesFactory
  def factory = classOf[ComponentCapabilitiesFactory]

  // TODO: put topics, once collections are supported by msgpack
  def getBytes: Array[Byte] = (new MsgBuilder).putString("factoryClassName").getBytes
}

class ComponentCapabilitiesFactory extends MessageFactory[ComponentCapabilities] {
  def unpack(rdr: MsgReader): Try[ComponentCapabilities] = {
    for {
      // TODO: get topics, once collections are supported by msgpack
      fcn <- rdr.getString(0)
    } yield {
      ComponentCapabilities(Set(fcn))
    }
  }
}
