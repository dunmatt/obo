package com.github.dunmatt.obo.android.components.examples.createVoiceControl

import com.github.dunmatt.obo.core.{ Connection, Message, OboIdentifier }
import com.github.dunmatt.obo.iRobotCreate.{ DriveStraight, Stop }
import com.github.dunmatt.obo.android.core.AndroidComponent
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ
import scala.concurrent.{ Await, Future, Promise }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import squants.motion.VelocityConversions._

class CreateVoiceControlComponent extends AndroidComponent {
  val log = LoggerFactory.getLogger(getClass)
  override val uiActivityClass = Some(classOf[VoiceControlActivity])
  protected val zctx = ZMQ.context(1)
  private val socket = zctx.socket(ZMQ.PULL)
  socket.bind(s"inproc://$instanceId")
  private var forwardCommands = true

  // private val createComponentP = Promise[Connection]
  // createComponentP.future.onSuccess { case conn =>
  //   new Thread(new Runnable {
  //     def run {
  //       log.info("Running")
  //       Thread.sleep(1000)
  //       conn.send(DriveStraight(0.1 mps))
  //       Thread.sleep(10000)
  //       conn.send(new Stop)
  //       conn.close
  //       log.info("Done")
  //     }
  //   }).start
  // }
  // createComponentP.future.onFailure { case e => log.error(s"Couldn't connect to Create.  $e") }

  override def onStart {
    super.onStart
    val createId = OboIdentifier("com.github.dunmatt.obo.iRobotCreate.CreateComponent")
    val createComponent = connectionFactory.connectTo(createId)
    // createComponentP.completeWith(createComponent)

    new Thread(new Runnable {
      def run {
        try {
          while (forwardCommands) {
            socket.recv(ZMQ.NOBLOCK) match {
              case null => Thread.sleep(100)  // ms
              case bytes => maybeSendCommand(new String(bytes))
            }
          }
        } catch {
          case e: Throwable => log.error(s"Closing command forwarding due to $e ", e)
        }
      }
    }).start
  }

  def maybeSendCommand(spokenText: String): Unit = {
  }

  override def onHalt {
    super.onHalt
    forwardCommands = false
  }

  def handleMessage(m: Message[_]): Option[Message[_]] = None
}
