package com.github.dunmatt.obo.core

import msgpack.{ MsgBuilder, MsgReader, StringField }
import scala.util.Try

case class ComponentCapabilities(topics: Set[RuntimeResourceName]) extends Message[ComponentCapabilities] {
  type Factory = ComponentCapabilitiesFactory
  def factory = classOf[ComponentCapabilitiesFactory]

  def getBytes: Array[Byte] = {
    (new MsgBuilder).putSeq(topics.map(t => new StringField(t.name)).toSeq).getBytes
  }
}

class ComponentCapabilitiesFactory extends MessageFactory[ComponentCapabilities] {
  def unpack(rdr: MsgReader): Try[ComponentCapabilities] = {
    for {
      ts <- rdr.getSeq(0, "")
    } yield {
      ComponentCapabilities(ts.map(t => RuntimeResourceName(t)).toSet)
    }
  }
}
