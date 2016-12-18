package com.github.dunmatt.obo.examples.createVoiceControl

import com.github.dunmatt.obo.core.{ Component, Connection, Message, OboIdentifier }
import com.github.dunmatt.obo.iRobotCreate.{ DriveStraight, Stop }
import org.slf4j.LoggerFactory
import scala.concurrent.{ Await, Future, Promise }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import squants.motion.VelocityConversions._

class CreateVoiceControlComponent extends Component {
  val log = LoggerFactory.getLogger(getClass)
  private val createComponentP = Promise[Connection]
  createComponentP.future.onSuccess { case conn =>
    new Thread(new Runnable {
      def run {
        log.info("Running")
        Thread.sleep(1000)
        conn.send(DriveStraight(0.1 mps))
        Thread.sleep(10000)
        conn.send(new Stop)
        conn.close
        log.info("Done")
      }
    }).start
  }
  createComponentP.future.onFailure { case e => log.error(s"Couldn't connect to Create.  $e") }

  override def onStart {
    val createId = OboIdentifier("com.github.dunmatt.obo.iRobotCreate.CreateComponent")
    createComponentP.completeWith(connectionFactory.connectTo(createId))
  }

  def handleMessage(m: Message[_]): Option[Message[_]] = None
}
