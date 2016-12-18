package com.github.dunmatt.obo.android.examples

import com.github.dunmatt.obo.core.{ Component, Message }
import com.github.dunmatt.obo.core.msgpack.MsgReader
import org.slf4j.LoggerFactory

class IncrementerComponent extends Component {
  val log = LoggerFactory.getLogger(getClass)

  def handleMessage(m: Message[_]): Option[Message[_]] = m match {
    case reader: MsgReader =>
      log.info(s"${reader.getString(0)}")
      None
  }
}
