package com.github.dunmatt.obo.iRobotCreate

import com.github.dunmatt.obo.core.{ Component, Message }
import com.github.dunmatt.obo.core.serial.{ SerialPort, SerialPortRequest }
import com.github.dunmatt.obo.utils.implicits.QuantityRangeImprovements._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future, Promise }
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Success }
import squants.QuantityRange
import squants.motion.AngularVelocityConversions._
import squants.motion.VelocityConversions._
import squants.space.Length
import squants.space.LengthConversions._

class CreateComponent extends Component {
  import CreateComponent._
  private val portP = Promise[SerialPort]
  val port = portP.future
  val toRobot = port.map(_.getOutputStream)
  val fromRobot = port.map(_.getInputStream)
  var commandQueue: Future[_] = toRobot

  override def onStart {
    // TODO: make the serial port a parameter
    val req = SerialPortRequest(None, SerialPortRequest.BAUD_57600)
    // val req = SerialPortRequest(Some("/dev/ttyUSB0"), SerialPortRequest.BAUD_57600)
    portP.completeWith(serialPortFactory.requestSerialPort(req))

    sendCommand(Commands.START)
    sendCommand(Commands.LOAD_STATUS_SONGS)
    sendCommand(Commands.SAFE)    // TODO: make this into a parameter, the user might want FULL control
    sendCommand(Commands.SING_READY)
    commandQueue.onSuccess { case _ => log.info("Done starting up!") }
  }

  override def onHalt {
    log.info("Halting robot.")
    sendCommand(Commands.STOP)
    sendCommand(Commands.PAUSE_STREAM)
    sendCommand(Commands.PASSIVE)
    try {
      Await.ready(commandQueue, Duration(100, "millis"))
    } catch {
      case t: Throwable => log.error("Flushing the command queue timed out!", t)
    } finally {
      port.foreach(_.close)
    }
  }

  def handleMessage(m: Message[_]): Option[Message[_]] = m match {
    case s: Stop =>
      sendCommand(Commands.STOP)
      None
    case Drive(vel, rad) =>
      if (rad.abs > 1.millimeters) {
        if (vel.abs > 0.001.mps) {
          val speed = (ACCEPTABLE_TRANSLATIONAL_SPEEDS.nearestPointTo(vel).toMetersPerSecond * 1000).toInt
          val radius = ACCEPTABLE_TURNING_RADII.nearestPointTo(rad).toMillimeters.toInt
          sendCommand(Array(Commands.DRIVE, highByte(speed), lowByte(speed), highByte(radius), lowByte(radius)).map(_.toByte))
        } else {
          handleMessage(TurnInPlace((rad / DRIVE_RADIUS).radiansPerSecond))
        }
      } else {
        if (vel.abs > 0.001.mps) {
          handleMessage(DriveStraight(vel))
        } else {
          handleMessage(new Stop)
        }
      }
      None
    case DriveStraight(vel) =>
      val speed = (ACCEPTABLE_TRANSLATIONAL_SPEEDS.nearestPointTo(vel).toMetersPerSecond * 1000).toInt
      sendCommand(Array(Commands.DRIVE, highByte(speed), lowByte(speed), highByte(Commands.STRAIGHT), lowByte(Commands.STRAIGHT)).map(_.toByte))
      None
    case TurnInPlace(vel) =>
      val rSpeed = (vel.toRadiansPerSecond * DRIVE_RADIUS.toMillimeters).toInt
      val lSpeed = -rSpeed
      sendCommand(Array(Commands.DRIVE_DIRECT, highByte(rSpeed), lowByte(rSpeed), highByte(lSpeed), lowByte(lSpeed)).map(_.toByte))
      None
    case msg =>
      log.warn(s"Ignoring unknown message: $msg.")
      None
  }

  protected def sendCommand(cmd: Array[Byte]) {
    commandQueue = commandQueue.map { _ =>
      toRobot.foreach(_.write(cmd))
    }
    if (log.isDebugEnabled) {  // between the closure and the string interpolation this is a reasonably expensive block, so we check if it's necessary
      commandQueue.onComplete {
        case r => log.debug(s"""Completed a command: ${cmd.map(_&255).mkString(", ")} with result: $r""")
      }
    }
  }
}

object CreateComponent {
  val ACCEPTABLE_TRANSLATIONAL_SPEEDS = QuantityRange(-0.5 mps, 0.5 mps)
  val ACCEPTABLE_TURNING_RADII = QuantityRange(-2 meters, 2 meters)
  val DRIVE_DIAMETER = 258.millimeters
  val DRIVE_RADIUS = DRIVE_DIAMETER / 2

  def highByte(n: Int): Int = (n >> 8) & 0xff
  def lowByte(n: Int): Int = n & 0xff
}
