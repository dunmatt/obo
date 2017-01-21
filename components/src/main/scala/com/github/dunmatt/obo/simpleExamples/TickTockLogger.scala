package com.github.dunmatt.obo.simpleExamples

import com.github.dunmatt.obo.core.{ Component, Message }

class TickTockLogger() extends Component {
  var keepRunning = true

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
          Thread.sleep(1000)
          log.info("Tock")
          Thread.sleep(1000)
        }
      }
    }).start
  }
}
