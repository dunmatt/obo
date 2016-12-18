package com.github.dunmatt.obo.iRobotCreate

import org.slf4j.LoggerFactory
import squants.electro.{ ElectricCharge, ElectricCurrent, ElectricPotential }
import squants.motion.Velocity
import squants.space.{ Angle, Length }
import squants.thermal.Temperature

import iRobotCreateState.{ ChargingState, RobotMode }

sealed trait CreateTelemetryPacket

object TelemetryPackets {
  def packetDataSize(packetTypeId: Int): Int = packetTypeId match {
    case  0 => 26
    case  1 => 10
    case  2 => 6
    case  3 => 10
    case  4 => 14
    case  5 => 12
    case  6 => 52
    case  7 => 1
    case  8 => 1
    case  9 => 1
    case 10 => 1
    case 11 => 1
    case 12 => 1
    case 13 => 1
    case 14 => 1
    case 15 => 1
    case 16 => 1
    case 17 => 1
    case 18 => 1
    case 19 => 2
    case 20 => 2
    case 21 => 1
    case 22 => 2
    case 23 => 2
    case 24 => 1
    case 25 => 2
    case 26 => 2
    case 27 => 2
    case 28 => 2
    case 29 => 2
    case 30 => 2
    case 31 => 2
    case 32 => 1
    case 33 => 2
    case 34 => 1
    case 35 => 1
    case 36 => 1
    case 37 => 1
    case 38 => 1
    case 39 => 2
    case 40 => 2
    case 41 => 2
    case 42 => 2
    case _ => -1
  }
}

case class UnionTelemetryPacket(packets: Set[CreateTelemetryPacket]) extends CreateTelemetryPacket

case class TelemetryPacket0( tp1: TelemetryPacket1
                           , tp2: TelemetryPacket2
                           , tp3: TelemetryPacket3) extends CreateTelemetryPacket

case class TelemetryPacket1( tp7: TelemetryPacket7
                           , tp8: TelemetryPacket8
                           , tp9: TelemetryPacket9
                           , tp10: TelemetryPacket10
                           , tp11: TelemetryPacket11
                           , tp12: TelemetryPacket12
                           , tp13: TelemetryPacket13
                           , tp14: TelemetryPacket14) extends CreateTelemetryPacket

case class TelemetryPacket2( tp17: TelemetryPacket17
                           , tp18: TelemetryPacket18
                           , tp19: TelemetryPacket19
                           , tp20: TelemetryPacket20) extends CreateTelemetryPacket

case class TelemetryPacket3( tp21: TelemetryPacket21
                           , tp22: TelemetryPacket22
                           , tp23: TelemetryPacket23
                           , tp24: TelemetryPacket24
                           , tp25: TelemetryPacket25
                           , tp26: TelemetryPacket26) extends CreateTelemetryPacket

case class TelemetryPacket4( tp27: TelemetryPacket27
                           , tp28: TelemetryPacket28
                           , tp29: TelemetryPacket29
                           , tp30: TelemetryPacket30
                           , tp31: TelemetryPacket31
                           , tp32: TelemetryPacket32
                           , tp33: TelemetryPacket33
                           , tp34: TelemetryPacket34) extends CreateTelemetryPacket

case class TelemetryPacket5( tp35: TelemetryPacket35
                           , tp36: TelemetryPacket36
                           , tp37: TelemetryPacket37
                           , tp38: TelemetryPacket38
                           , tp39: TelemetryPacket39
                           , tp40: TelemetryPacket40
                           , tp41: TelemetryPacket41
                           , tp42: TelemetryPacket42) extends CreateTelemetryPacket

case class TelemetryPacket6( tp1: TelemetryPacket1
                           , tp2: TelemetryPacket2
                           , tp3: TelemetryPacket3
                           , tp4: TelemetryPacket4
                           , tp5: TelemetryPacket5) extends CreateTelemetryPacket

case class TelemetryPacket7( casterDropped: Boolean
                           , leftWheelDropped: Boolean
                           , rightWheelDropped: Boolean
                           , leftBumperPressed: Boolean
                           , rightBumperPressed: Boolean) extends CreateTelemetryPacket

case class TelemetryPacket8(wallDetected: Boolean) extends CreateTelemetryPacket

case class TelemetryPacket9(cliffDetectedLeft: Boolean) extends CreateTelemetryPacket

case class TelemetryPacket10(cliffDetectedFrontLeft: Boolean) extends CreateTelemetryPacket

case class TelemetryPacket11(cliffDetectedFrontRight: Boolean) extends CreateTelemetryPacket

case class TelemetryPacket12(cliffDetectedRight: Boolean) extends CreateTelemetryPacket

case class TelemetryPacket13(virtualWallDetected: Boolean) extends CreateTelemetryPacket

case class TelemetryPacket14( leftWheelOverCurrent: Boolean
                            , rightWheelOverCurrent: Boolean
                            , ld2OverCurrent: Boolean
                            , ld0OverCurrent: Boolean  // may need to switch this with ld1, likely a typo in the docs
                            , ld1OverCurrent: Boolean) extends CreateTelemetryPacket

case class TelemetryPacket17(byteFromIR: Int) extends CreateTelemetryPacket

case class TelemetryPacket18(nextPressed: Boolean, playPressed: Boolean) extends CreateTelemetryPacket

case class TelemetryPacket19(distanceIncrement: Length) extends CreateTelemetryPacket

case class TelemetryPacket20(angleIncrement: Angle) extends CreateTelemetryPacket

case class TelemetryPacket21(chargingState: ChargingState) extends CreateTelemetryPacket

case class TelemetryPacket22(voltage: ElectricPotential) extends CreateTelemetryPacket

case class TelemetryPacket23(current: ElectricCurrent) extends CreateTelemetryPacket

case class TelemetryPacket24(batteryTemperature: Temperature) extends CreateTelemetryPacket

case class TelemetryPacket25(batteryCharge: ElectricCharge) extends CreateTelemetryPacket

case class TelemetryPacket26(batteryCapacity: ElectricCharge) extends CreateTelemetryPacket

case class TelemetryPacket27(wallSensorSignalStrength: Int) extends CreateTelemetryPacket

case class TelemetryPacket28(leftCliffDetectorSignalStrength: Int) extends CreateTelemetryPacket

case class TelemetryPacket29(leftFrontCliffDetectorSignalStrength: Int) extends CreateTelemetryPacket

case class TelemetryPacket30(rightFrontCliffDetectorSignalStrength: Int) extends CreateTelemetryPacket

case class TelemetryPacket31(rightCliffDetectorSignalStrength: Int) extends CreateTelemetryPacket

case class TelemetryPacket32( deviceDetectHigh: Boolean
                            , digitalInput3High: Boolean
                            , digitalInput2High: Boolean
                            , digitalInput1High: Boolean
                            , digitalInput0High: Boolean) extends CreateTelemetryPacket

case class TelemetryPacket33(analogInputSignal: Int) extends CreateTelemetryPacket

case class TelemetryPacket34(chargingFromBase: Boolean, chargingFromCable: Boolean) extends CreateTelemetryPacket

case class TelemetryPacket35(currentMode: RobotMode) extends CreateTelemetryPacket

case class TelemetryPacket36(currentSong: Int) extends CreateTelemetryPacket

case class TelemetryPacket37(songPlaying: Boolean) extends CreateTelemetryPacket

case class TelemetryPacket38(streamPacketsCount: Int) extends CreateTelemetryPacket

case class TelemetryPacket39(requestedVelocity: Velocity) extends CreateTelemetryPacket

case class TelemetryPacket40(requestedRadius: Length) extends CreateTelemetryPacket

case class TelemetryPacket41(requestedRightVelocity: Velocity) extends CreateTelemetryPacket

case class TelemetryPacket42(requestedLeftVelocity: Velocity) extends CreateTelemetryPacket

