package com.github.dunmatt.obo.core

import msgpack.{ MsgBuilder, MsgReader }
import org.joda.time.Instant
import org.slf4j.event.Level
import scala.util.Try

case class LogEntry( name: String
                   , level: Level
                   , timestamp: Instant
                   , message: String) extends Message[LogEntry] {
  type Factory = LogEntryFactory
  def factory = classOf[LogEntryFactory]

  def getBytes: Array[Byte] = (new MsgBuilder).putString(name)
                                              .putInt(level.ordinal)
                                              .putInt(timestamp.getMillis)
                                              .putString(message)
                                              .getBytes
}

object LogEntry {
  val factory = classOf[LogEntryFactory]
}

class LogEntryFactory extends MessageFactory[LogEntry] {
  def unpack(rdr: MsgReader): Try[LogEntry] = {
    for {
      name <- rdr.getString(0)
      lvl <- rdr.getInt(1)
      time <- rdr.getInt(2)
      msg <- rdr.getString(3)
    } yield {
      LogEntry(name, Level.values.apply(lvl.toInt), new Instant(time), msg)
    }
  }
}
