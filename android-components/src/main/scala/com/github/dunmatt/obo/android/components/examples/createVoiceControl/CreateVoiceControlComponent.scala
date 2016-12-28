package com.github.dunmatt.obo.android.components.examples.createVoiceControl

import android.widget.Toast
import com.github.dunmatt.obo.core.{ Connection, Message, OboIdentifier }
import com.github.dunmatt.obo.iRobotCreate.{ DriveStraight, Stop, TurnInPlace }
import com.github.dunmatt.obo.android.core.AndroidComponent
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ
import scala.concurrent.{ Await, Future, Promise }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import squants.motion.AngularVelocityConversions._
import squants.motion.VelocityConversions._

class CreateVoiceControlComponent extends AndroidComponent {
  import CreateVoiceControlComponent._
  val log = LoggerFactory.getLogger(getClass)
  override val uiActivityClass = Some(classOf[VoiceControlActivity])
  private val socket = zctx.socket(ZMQ.PULL)
  socket.bind(s"inproc://$instanceId")
  log.info(s"Binding to inproc://$instanceId")
  private var forwardCommands = true

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
    super.onStart
    val createId = OboIdentifier("com.github.dunmatt.obo.iRobotCreate.CreateComponent")
    val createComponentConnection = connectionFactory.connectTo(createId)
    createComponentP.completeWith(createComponentConnection)

    new Thread(new Runnable {
      def run {
        try {
          while (forwardCommands) {
            socket.recv(ZMQ.NOBLOCK) match {
              case null => Thread.sleep(100)  // ms
            // socket.recv match {
            //   case null => forwardCommands = false
              case bytes =>
                log.info(s"Got ${new String(bytes)} from the voice recognition component.")
                maybeSendCommand(new String(bytes), createComponentConnection)
            }
          }
        } catch {
          case e: Throwable => log.error(s"Closing command forwarding due to $e ", e)
        }
      }
    }).start
  }

  def maybeSendCommand(spokenText: String, connection: Future[Connection]): Unit = {
    // Toast.makeText(context, spokenText, Toast.LENGTH_LONG).show
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
  }

  def handleMessage(m: Message[_]): Option[Message[_]] = None
}

object CreateVoiceControlComponent {
  val zctx = ZMQ.context(1)
}
