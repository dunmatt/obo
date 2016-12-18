package com.github.dunmatt.obo.iRobotCreate

import com.github.dunmatt.obo.utils.implicits.ByteBufferEnhancements._
import java.nio.ByteBuffer
import org.slf4j.LoggerFactory
import scala.util.{ Failure, Success, Try }
import squants.electro.ElectricChargeConversions._
import squants.electro.ElectricCurrentConversions._
import squants.electro.ElectricPotentialConversions._
import squants.motion.VelocityConversions._
import squants.space.AngleConversions._
import squants.space.LengthConversions._
import squants.thermal.TemperatureConversions._

object iRobotCreateDataParser {
  val HEADER = 19.toByte
  val log = LoggerFactory.getLogger(getClass)

  protected def unsign(b: Byte): Int = b & 0xFF

  protected implicit def iff(x: Int): Boolean = x != 0

  protected def streamSegmentSize(query: ByteBuffer): Int = {
    query.rewind
    var sum = 3  // header bytecount and checksum
    while (query.hasRemaining) {
      sum += 1 + TelemetryPackets.packetDataSize(query.get)
    }
    sum
  }

  def parsePacket(packet: ByteBuffer, query: ByteBuffer): Try[CreateTelemetryPacket] = {
    if (checksumPasses(packet, query)) {
      query.rewind
      packet.position(2)  // location of the first packetID in the compound packet
      if (query.limit == 1) {
        parsePacket(packet, query.get(0))
      } else {
        (0 until query.limit).map(parsePacket(packet, _)).fold(Try(UnionTelemetryPacket(Set.empty))) {
          case (Success(UnionTelemetryPacket(a)), Success(b)) => Success(UnionTelemetryPacket(a + b))
          case (Success(UnionTelemetryPacket(a)), e) => e
          case (e, _) => e
        }
      }
    } else {
      log.warn(s"""Response (${packet.mkString(", ")}) failed checksum!""")
      Failure(new Exception("Response failed checksum!"))
    }
  }

  def checksumPasses(packet: ByteBuffer, query: ByteBuffer): Boolean = {
    if (packet.limit >= streamSegmentSize(query) && packet.get(0) == HEADER) {
      packet.rewind
      var sum = 0
      while (packet.hasRemaining) {
        sum += unsign(packet.get)
      }
      sum & 0xff == 0
    } else {
      false
    }
  }

  protected def parsePacket(packet: ByteBuffer, packetType: Int): Try[CreateTelemetryPacket] = {
    if (packet.get(packet.position) != packetType) {
      log.warn("Response data out of sync.")
      Failure(new Exception("Response data out of sync... perhaps the query changed asynchronously?"))
    } else if (packet.limit < TelemetryPackets.packetDataSize(packetType)) {
      log.warn(s"Insufficient data received for packet type $packetType")
      Failure(new Exception(s"Insufficient data received for packet type $packetType"))
    } else {
      packetType match {
        case  0 => parsePacket0(packet)
        case  1 => parsePacket1(packet)
        case  2 => parsePacket2(packet)
        case  3 => parsePacket3(packet)
        case  4 => parsePacket4(packet)
        case  5 => parsePacket5(packet)
        case  6 => parsePacket6(packet)
        case  7 => parsePacket7(packet)
        case  8 => parsePacket8(packet)
        case  9 => parsePacket9(packet)

        case 10 => parsePacket10(packet)
        case 11 => parsePacket11(packet)
        case 12 => parsePacket12(packet)
        case 13 => parsePacket13(packet)
        case 14 => parsePacket14(packet)
        case 15 => Failure(new Exception("Can't parse packet type 15 as it doesn't exist..."))
        case 16 => Failure(new Exception("Can't parse packet type 16 as it doesn't exist..."))
        case 17 => parsePacket17(packet)
        case 18 => parsePacket18(packet)
        case 19 => parsePacket19(packet)

        case 20 => parsePacket20(packet)
        case 21 => parsePacket21(packet)
        case 22 => parsePacket22(packet)
        case 23 => parsePacket23(packet)
        case 24 => parsePacket24(packet)
        case 25 => parsePacket25(packet)
        case 26 => parsePacket26(packet)
        case 27 => parsePacket27(packet)
        case 28 => parsePacket28(packet)
        case 29 => parsePacket29(packet)

        case 30 => parsePacket30(packet)
        case 31 => parsePacket31(packet)
        case 32 => parsePacket32(packet)
        case 33 => parsePacket33(packet)
        case 34 => parsePacket34(packet)
        case 35 => parsePacket35(packet)
        case 36 => parsePacket36(packet)
        case 37 => parsePacket37(packet)
        case 38 => parsePacket38(packet)
        case 39 => parsePacket39(packet)

        case 40 => parsePacket40(packet)
        case 41 => parsePacket41(packet)
        case 42 => parsePacket42(packet)
        case _  => Failure(new Exception(s"Unsupported packet type $packetType."))
      }
    }
  }

  protected def parsePacket0(packet: ByteBuffer): Try[TelemetryPacket0] = {
    packet.get
    for {
        a <- parsePacket1(packet)
        b <- parsePacket2(packet)
        c <- parsePacket3(packet)
    } yield {
      TelemetryPacket0(a, b, c)
    }
  }

  protected def parsePacket1(packet: ByteBuffer): Try[TelemetryPacket1] = {
    packet.get
    for {
      a <- parsePacket7(packet)
      b <- parsePacket8(packet)
      c <- parsePacket9(packet)
      d <- parsePacket10(packet)
      e <- parsePacket11(packet)
      f <- parsePacket12(packet)
      g <- parsePacket13(packet)
      h <- parsePacket14(packet)
    } yield {
      TelemetryPacket1(a, b, c, d, e, f, g, h)
    }
  }

  protected def parsePacket2(packet: ByteBuffer): Try[TelemetryPacket2] = {
    packet.get
    for {
      a <- parsePacket17(packet)
      b <- parsePacket18(packet)
      c <- parsePacket19(packet)
      d <- parsePacket20(packet)
    } yield {
      TelemetryPacket2(a, b, c, d)
    }
  }

  protected def parsePacket3(packet: ByteBuffer): Try[TelemetryPacket3] = {
    packet.get
    for {
      a <- parsePacket21(packet)
      b <- parsePacket22(packet)
      c <- parsePacket23(packet)
      d <- parsePacket24(packet)
      e <- parsePacket25(packet)
      f <- parsePacket26(packet)
    } yield {
      TelemetryPacket3(a, b, c, d, e, f)
    }
  }

  protected def parsePacket4(packet: ByteBuffer): Try[TelemetryPacket4] = {
    packet.get
    for {
      a <- parsePacket27(packet)
      b <- parsePacket28(packet)
      c <- parsePacket29(packet)
      d <- parsePacket30(packet)
      e <- parsePacket31(packet)
      f <- parsePacket32(packet)
      g <- parsePacket33(packet)
      h <- parsePacket34(packet)
    } yield {
      TelemetryPacket4(a, b, c, d, e, f, g, h)
    }
  }

  protected def parsePacket5(packet: ByteBuffer): Try[TelemetryPacket5] = {
    packet.get
    for {
      a <- parsePacket35(packet)
      b <- parsePacket36(packet)
      c <- parsePacket37(packet)
      d <- parsePacket38(packet)
      e <- parsePacket39(packet)
      f <- parsePacket40(packet)
      g <- parsePacket41(packet)
      h <- parsePacket42(packet)
    } yield {
      TelemetryPacket5(a, b, c, d, e, f, g, h)
    }
  }

  protected def parsePacket6(packet: ByteBuffer): Try[TelemetryPacket6] = {
    packet.get
    for {
      a <- parsePacket1(packet)
      b <- parsePacket2(packet)
      c <- parsePacket3(packet)
      d <- parsePacket4(packet)
      e <- parsePacket5(packet)
    } yield {
      TelemetryPacket6(a, b, c, d, e)
    }
  }

  protected def parsePacket7(packet: ByteBuffer): Try[TelemetryPacket7] = {
    packet.get
    Try(packet.get).map { b =>
      TelemetryPacket7( iff(b & 0x10)
                      , iff(b & 0x08)
                      , iff(b & 0x04)
                      , iff(b & 0x02)
                      , iff(b & 0x01))
    }
  }

  protected def parsePacket8(packet: ByteBuffer): Try[TelemetryPacket8] = {
    packet.get
    Try(TelemetryPacket8(iff(packet.get)))
  }

  protected def parsePacket9(packet: ByteBuffer): Try[TelemetryPacket9] = {
    packet.get
    Try(TelemetryPacket9(iff(packet.get)))
  }

  protected def parsePacket10(packet: ByteBuffer): Try[TelemetryPacket10] = {
    packet.get
    Try(TelemetryPacket10(iff(packet.get)))
  }

  protected def parsePacket11(packet: ByteBuffer): Try[TelemetryPacket11] = {
    packet.get
    Try(TelemetryPacket11(iff(packet.get)))
  }

  protected def parsePacket12(packet: ByteBuffer): Try[TelemetryPacket12] = {
    packet.get
    Try(TelemetryPacket12(iff(packet.get)))
  }

  protected def parsePacket13(packet: ByteBuffer): Try[TelemetryPacket13] = {
    packet.get
    Try(TelemetryPacket13(iff(packet.get)))
  }

  protected def parsePacket14(packet: ByteBuffer): Try[TelemetryPacket14] = {
    packet.get
    Try(packet.get).map { b =>
      TelemetryPacket14( iff(b & 0x10)
                       , iff(b & 0x08)
                       , iff(b & 0x04)
                       , iff(b & 0x02)
                       , iff(b & 0x01))
    }
  }

  protected def parsePacket17(packet: ByteBuffer): Try[TelemetryPacket17] = {
    packet.get
    Try(TelemetryPacket17(unsign(packet.get)))
  }

  protected def parsePacket18(packet: ByteBuffer): Try[TelemetryPacket18] = {
    packet.get
    Try(packet.get).map { b => TelemetryPacket18(iff(b & 0x4), iff(b & 0x1)) }
  }

  protected def parsePacket19(packet: ByteBuffer): Try[TelemetryPacket19] = {
    packet.get
    Try(TelemetryPacket19(packet.getShort millimeters))
  }

  protected def parsePacket20(packet: ByteBuffer): Try[TelemetryPacket20] = {
    packet.get
    Try(TelemetryPacket20(packet.getShort degrees))
  }

  protected def parsePacket21(packet: ByteBuffer): Try[TelemetryPacket21] = {
    packet.get
    Try {
      TelemetryPacket21(packet.get match {
        case 0 => iRobotCreateState.NOT_CHARGING
        case 1 => iRobotCreateState.RECONDITIONING
        case 2 => iRobotCreateState.FULL
        case 3 => iRobotCreateState.TRICKLE
        case 4 => iRobotCreateState.WAITING
        case 5 => iRobotCreateState.CHARGING_FAULT
        case 6 => iRobotCreateState.UNKNOWN
      })
    }
  }

  protected def parsePacket22(packet: ByteBuffer): Try[TelemetryPacket22] = {
    packet.get
    Try(TelemetryPacket22(packet.getShort millivolts))
  }

  protected def parsePacket23(packet: ByteBuffer): Try[TelemetryPacket23] = {
    packet.get
    Try(TelemetryPacket23(packet.getShort milliamps))
  }

  protected def parsePacket24(packet: ByteBuffer): Try[TelemetryPacket24] = {
    packet.get
    Try(TelemetryPacket24(packet.get celsius))
  }

  protected def parsePacket25(packet: ByteBuffer): Try[TelemetryPacket25] = {
    packet.get
    Try(TelemetryPacket25((packet.getShort & 0xFFFF) milliampereHours))
  }

  protected def parsePacket26(packet: ByteBuffer): Try[TelemetryPacket26] = {
    packet.get
    Try(TelemetryPacket26((packet.getShort & 0xFFFF) milliampereHours))
  }

  protected def parsePacket27(packet: ByteBuffer): Try[TelemetryPacket27] = {
    packet.get
    Try(TelemetryPacket27(packet.getShort & 0xFFFF))
  }

  protected def parsePacket28(packet: ByteBuffer): Try[TelemetryPacket28] = {
    packet.get
    Try(TelemetryPacket28(packet.getShort & 0xFFFF))
  }

  protected def parsePacket29(packet: ByteBuffer): Try[TelemetryPacket29] = {
    packet.get
    Try(TelemetryPacket29(packet.getShort & 0xFFFF))
  }

  protected def parsePacket30(packet: ByteBuffer): Try[TelemetryPacket30] = {
    packet.get
    Try(TelemetryPacket30(packet.getShort & 0xFFFF))
  }

  protected def parsePacket31(packet: ByteBuffer): Try[TelemetryPacket31] = {
    packet.get
    Try(TelemetryPacket31(packet.getShort & 0xFFFF))
  }

  protected def parsePacket32(packet: ByteBuffer): Try[TelemetryPacket32] = {
    packet.get
    Try(packet.get).map { b =>
      TelemetryPacket32( iff(b & 0x10)
                       , iff(b & 0x08)
                       , iff(b & 0x04)
                       , iff(b & 0x02)
                       , iff(b & 0x01))
    }
  }

  protected def parsePacket33(packet: ByteBuffer): Try[TelemetryPacket33] = {
    packet.get
    Try(TelemetryPacket33(packet.getShort & 0xFFFF))
  }

  protected def parsePacket34(packet: ByteBuffer): Try[TelemetryPacket34] = {
    packet.get
    Try(packet.get).map { b => TelemetryPacket34(iff(b & 0x02), iff(b & 0x01)) }
  }

  protected def parsePacket35(packet: ByteBuffer): Try[TelemetryPacket35] = {
    packet.get
    Try {
      TelemetryPacket35(packet.get match {
        case 0 => iRobotCreateState.OFF
        case 1 => iRobotCreateState.PASSIVE
        case 2 => iRobotCreateState.SAFE
        case 3 => iRobotCreateState.FULL_CONTROL
        case 4 => iRobotCreateState.UNKNOWN
      })
    }
  }

  protected def parsePacket36(packet: ByteBuffer): Try[TelemetryPacket36] = {
    packet.get
    Try(TelemetryPacket36(packet.get & 0xFF))
  }

  protected def parsePacket37(packet: ByteBuffer): Try[TelemetryPacket37] = {
    packet.get
    Try(TelemetryPacket37(iff(packet.get)))
  }

  protected def parsePacket38(packet: ByteBuffer): Try[TelemetryPacket38] = {
    packet.get
    Try(TelemetryPacket38(packet.get & 0xFF))
  }

  protected def parsePacket39(packet: ByteBuffer): Try[TelemetryPacket39] = {
    packet.get
    Try(TelemetryPacket39((packet.getShort * 1000) mps))
  }

  protected def parsePacket40(packet: ByteBuffer): Try[TelemetryPacket40] = {
    packet.get
    Try(TelemetryPacket40(packet.getShort millimeters))
  }

  protected def parsePacket41(packet: ByteBuffer): Try[TelemetryPacket41] = {
    packet.get
    Try(TelemetryPacket41((packet.getShort * 1000) mps))
  }

  protected def parsePacket42(packet: ByteBuffer): Try[TelemetryPacket42] = {
    packet.get
    Try(TelemetryPacket42((packet.getShort * 1000) mps))
  }
}
