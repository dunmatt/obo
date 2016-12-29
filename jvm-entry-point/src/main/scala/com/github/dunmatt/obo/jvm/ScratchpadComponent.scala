package com.github.dunmatt.obo.jvm

import com.github.dunmatt.obo.core.{ Component, Message, OboIdentifier }
import com.github.dunmatt.obo.core.msgpack.{ MsgBuilder, MsgReader }
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global

class ScratchpadComponent extends Component {
  private var sendPings = true

  override def onStart = {
    val other = connectionFactory.connectTo(OboIdentifier("com.github.dunmatt.obo.android.examples.IncrementerComponent"))
    other.onSuccess { case conn =>
      val msg = (new MsgBuilder).putString("yo!")
      while (sendPings) {
        // conn.send(msg)
        log.info("Sent message!")
        Thread.sleep(1000)
      }
      conn.close
    }
  }
  
  def handleMessage(m: Message[_]): Option[Message[_]] = m match {
    case reader: MsgReader =>
      log.info(s"${reader.getString(0)}")
      None
  }

  override def onHalt = {
    sendPings = false
  }
}
