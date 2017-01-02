package com.github.dunmatt.obo.core

import msgpack.{ MsgBuilder, MsgReader }
import scala.util.Try

// TODO: perhaps add some sort of token here
class ComponentCapabilitiesRequest extends Message[ComponentCapabilitiesRequest] {
  type Factory = ComponentCapabilitiesRequestFactory
  def factory = classOf[ComponentCapabilitiesRequestFactory]

  def getBytes: Array[Byte] = (new MsgBuilder).putNil.getBytes
}

class ComponentCapabilitiesRequestFactory extends MessageFactory[ComponentCapabilitiesRequest] {
  def unpack(rdr: MsgReader): Try[ComponentCapabilitiesRequest] = {
    // TODO: if ComponentCapabilitiesRequest starts requiring data fix this!
    Try(new ComponentCapabilitiesRequest)
  }
}
