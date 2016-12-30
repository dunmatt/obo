package com.github.dunmatt.obo.android.components.examples.createVoiceControl

import android.widget.Toast
import com.github.dunmatt.obo.core.{ Connection, Message, OboIdentifier }
import com.github.dunmatt.obo.iRobotCreate.{ DriveStraight, Stop, TurnInPlace }
import com.github.dunmatt.obo.android.core.AndroidComponent
import org.zeromq.ZMQ
import scala.concurrent.{ Await, Future, Promise }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import squants.motion.AngularVelocityConversions._
import squants.motion.VelocityConversions._

class CreateVoiceControlComponent extends AndroidComponent {
  import CreateVoiceControlComponent._
  override val uiActivityClass = Some(classOf[VoiceControlActivity])
  private val activitySocket = zctx.socket(ZMQ.SUB)
  private val killActivity = zctx.socket(ZMQ.PUB)
  activitySocket.subscribe(Array.empty)  // empty array here means subscribe to everything
  log.info(s"Binding to inproc://$instanceId")
  activitySocket.bind(s"inproc://$instanceId")
  killActivity.bind(s"inproc://$instanceId/kill")
  private var forwardCommands = true

  override def onStart {
    super.onStart
    val createComponentConnection = connectTo(OboIdentifier("com.github.dunmatt.obo.iRobotCreate.CreateComponent"))

    new Thread(new Runnable {
      def run {
        try {
          while (forwardCommands) {
            activitySocket.recv(ZMQ.NOBLOCK) match {
              case null => Thread.sleep(100)  // ms
              case bytes =>
                log.debug(s"Got ${new String(bytes)} from the voice recognition activity.")
                maybeSendCommand(new String(bytes), createComponentConnection)
            }
          }
        } catch {
          case e: Throwable => log.error(s"Closing command forwarding due to $e ", e)
        } finally {
          createComponentConnection.foreach(_.close)
          log.info("Closed connection to CreateComponent")
        }
      }
    }).start
  }

  def maybeSendCommand(spokenText: String, connection: Future[Connection]): Unit = {
    if (connection.isCompleted) {
      if (spokenText.contains("stop")) {
        connection.foreach(_.send(new Stop))
      } else if (spokenText.contains("hurry")) {
        connection.foreach(_.send(DriveStraight(0.5 mps)))
      } else if (spokenText.contains("forward")) {
        connection.foreach(_.send(DriveStraight(0.1 mps)))
      } else if (spokenText.contains("right")) {
        connection.foreach(_.send(TurnInPlace(-0.2 radiansPerSecond)))
      } else if (spokenText.contains("left")) {
        connection.foreach(_.send(TurnInPlace(0.2 radiansPerSecond)))
      }
    }
  }

  override def onHalt {
    super.onHalt
    forwardCommands = false
    killActivity.send(Array(0.toByte))  // the zero here doesn't matter, the other side doesn't read it
    log.info("Sent kill signal to VoiceControlActivity")
    killActivity.close
    activitySocket.close
  }

  def handleMessage(m: Message[_]): Option[Message[_]] = None
}

object CreateVoiceControlComponent {
  val zctx = ZMQ.context(1)
}
