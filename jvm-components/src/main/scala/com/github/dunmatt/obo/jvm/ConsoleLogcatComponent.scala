package com.github.dunmatt.obo.jvm

import com.github.dunmatt.obo.core.{ Component, LogEntry, Message, RuntimeResourceName }

class ConsoleLogcatComponent() extends Component {
  override def handleMessage(m: Message[_]): Option[Message[_]] = None

  override def handleSubscriptionMessage(topic: RuntimeResourceName, m: Message[_]): Unit = m match {
    case le: LogEntry => println(le)
  }

  override def onStart = {
    super.onStart
    subscribeTo(RuntimeResourceName("/log"))
  }
}
