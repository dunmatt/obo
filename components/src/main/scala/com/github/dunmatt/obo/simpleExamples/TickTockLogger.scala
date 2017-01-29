package com.github.dunmatt.obo.simpleExamples

import com.github.dunmatt.obo.core.{ Component, Message, TypedOperationalParameter }
import com.github.dunmatt.obo.core.SquantsParameterSerializers._
import squants.time.TimeConversions._

class TickTockLogger() extends Component {
  var keepRunning = true

  val delay = new TypedOperationalParameter("delay", 1 seconds, "How long should the clock wait between ticks?")

  override val parameters = Set(delay)

  override def handleMessage(m: Message[_]): Option[Message[_]] = None

  override def onHalt = {
    keepRunning = false
    super.onHalt
  }

  override def onStart = {
    super.onStart
    new Thread(new Runnable {
      def run {
        while (keepRunning) {
          log.info("Tick")
          Thread.sleep(delay.value.toMilliseconds.toInt)
          log.info("Tock")
          Thread.sleep(delay.value.toMilliseconds.toInt)
        }
      }
    }).start
  }
}
