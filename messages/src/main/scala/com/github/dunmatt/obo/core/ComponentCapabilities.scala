package com.github.dunmatt.obo.core

import com.github.dunmatt.obo.utils.implicits.MapImprovements._
import msgpack.{ MsgBuilder, MsgReader, StringField }
import scala.util.Try

case class ComponentCapabilities(topics: collection.Map[RuntimeResourceName, Class[_ <: MessageFactory[_ <: Message[_]]]])
           extends Message[ComponentCapabilities] {
  type Factory = ComponentCapabilitiesFactory
  def factory = classOf[ComponentCapabilitiesFactory]

  private def serializePair(pair: ((RuntimeResourceName, Class[_ <: MessageFactory[_]]))): ((StringField, StringField)) = pair match {
    case (k, v) => ((new StringField(k.name), new StringField(v.getName)))
  }

  def getBytes: Array[Byte] = {
    (new MsgBuilder).putMap(topics.doubleMap(serializePair)).getBytes
  }
}

class ComponentCapabilitiesFactory extends MessageFactory[ComponentCapabilities] {
  private def deserializePair(pair: ((String, String))): ((RuntimeResourceName, Class[_ <: MessageFactory[_ <: Message[_]]])) = pair match {
    case (k, v) => ((RuntimeResourceName(k), Class.forName(v).asInstanceOf[Class[MessageFactory[_ <: Message[_]]]]))
  }

  def unpack(rdr: MsgReader): Try[ComponentCapabilities] = {
    for {
      ts <- rdr.getMap[String, String](0)
    } yield {
      ComponentCapabilities(ts.doubleMap(deserializePair))
    }
  }
}
