package com.github.dunmatt.obo.iRobotCreate

import com.github.dunmatt.obo.core.{ Message, MessageFactory }
import com.github.dunmatt.obo.core.msgpack.{ MsgBuilder, MsgReader }
import scala.util.Try
import squants.motion.{ AngularVelocity, Velocity }
import squants.motion.AngularVelocityConversions._
import squants.motion.VelocityConversions._
import squants.space.Length
import squants.space.LengthConversions._

case class Drive(velocity: Velocity, radius: Length) extends Message[Drive] {
  type Factory = DriveFactory
  val factory = classOf[DriveFactory]

  def getBytes: Array[Byte] = (new MsgBuilder).putFloat(velocity.toMetersPerSecond).putFloat(radius.toMeters).getBytes
}

class DriveFactory extends MessageFactory[Drive] {
  def unpack(rdr: MsgReader): Try[Drive] = {
    for {
      velScalar <- rdr.getFloat(0)
      radScalar <- rdr.getFloat(1)
    } yield {
      Drive(velScalar mps, radScalar meters)
    }
  }
}

case class DriveStraight(velocity: Velocity) extends Message[DriveStraight] {
  type Factory = DriveStraightFactory
  val factory = classOf[DriveStraightFactory]
  def getBytes: Array[Byte] = (new MsgBuilder).putFloat(velocity.toMetersPerSecond).getBytes
}

class DriveStraightFactory extends MessageFactory[DriveStraight] {
  def unpack(rdr: MsgReader): Try[DriveStraight] = rdr.getFloat(0).map(f => DriveStraight(f.mps))
}

case class TurnInPlace(velocity: AngularVelocity) extends Message[TurnInPlace] {
  type Factory = TurnInPlaceFactory
  val factory = classOf[TurnInPlaceFactory]
  def getBytes: Array[Byte] = (new MsgBuilder).putFloat(velocity.toRadiansPerSecond).getBytes
}

class TurnInPlaceFactory extends MessageFactory[TurnInPlace] {
  def unpack(rdr: MsgReader): Try[TurnInPlace] = rdr.getFloat(0).map(f => TurnInPlace(f.radiansPerSecond))
}

class Stop extends Message[Stop] {
  type Factory = StopFactory
  val factory = classOf[StopFactory]
  def getBytes: Array[Byte] = (new MsgBuilder).putNil.getBytes
}

class StopFactory extends MessageFactory[Stop] {
  def unpack(rdr: MsgReader): Try[Stop] = Try(new Stop)
}
