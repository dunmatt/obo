package com.github.dunmatt.obo.jvm.serial

import com.github.dunmatt.obo.core.serial.{ SerialPort, SerialPortFactory, SerialPortRequest }
import com.github.dunmatt.obo.jvm.JvmConstants
import gnu.io.CommPortIdentifier
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RxtxSerialPortFactory extends SerialPortFactory {
  import RxtxSerialPortFactory._
  protected def buildSerialPort(req: SerialPortRequest): Future[SerialPort] = {
    Future {
      req.name.foreach(n => System.setProperty(JvmConstants.RXTX_PORT_PROPERTY, n))
      val info = portInfoWithName(req.name)
      val port = info.open(JvmConstants.RXTX_PORT_OWNER, req.maxWait.toMilliseconds.toInt).asInstanceOf[gnu.io.SerialPort]
      port.setSerialPortParams(req.baudRate, getDataBits(req), getStopBits(req), getParity(req))
      new RxtxSerialPort(port)
    }
  }
}

object RxtxSerialPortFactory {
  protected def portInfoWithName(name: Option[String]): CommPortIdentifier = name match {
    case Some(n) => CommPortIdentifier.getPortIdentifier(n)
    // TODO: maybe throw a warning if they don't specify a name?
    case None => CommPortIdentifier.getPortIdentifiers.toSeq.head.asInstanceOf[CommPortIdentifier]
  }

  protected def getDataBits(req: SerialPortRequest): Int = req.dataBits match {
    case SerialPortRequest.DATABITS_8 => gnu.io.SerialPort.DATABITS_8
    case SerialPortRequest.DATABITS_7 => gnu.io.SerialPort.DATABITS_7
    case SerialPortRequest.DATABITS_6 => gnu.io.SerialPort.DATABITS_6
    case SerialPortRequest.DATABITS_5 => gnu.io.SerialPort.DATABITS_5
    case bits => throw new Exception(s"Unsupported number of data bits: $bits")
  }

  protected def getStopBits(req: SerialPortRequest): Int = req.stopBits match {
    case SerialPortRequest.STOPBITS_1 => gnu.io.SerialPort.STOPBITS_1
    case SerialPortRequest.STOPBITS_1_5 => gnu.io.SerialPort.STOPBITS_1_5
    case SerialPortRequest.STOPBITS_2 => gnu.io.SerialPort.STOPBITS_2
    case bits => throw new Exception(s"Unsupported number of stop bits: $bits")
  }

  protected def getParity(req: SerialPortRequest): Int = req.parity match {
    case SerialPortRequest.PARITY_NONE => gnu.io.SerialPort.PARITY_NONE
    case SerialPortRequest.PARITY_ODD => gnu.io.SerialPort.PARITY_ODD
    case SerialPortRequest.PARITY_EVEN => gnu.io.SerialPort.PARITY_EVEN
    case SerialPortRequest.PARITY_MARK => gnu.io.SerialPort.PARITY_MARK
    case SerialPortRequest.PARITY_SPACE => gnu.io.SerialPort.PARITY_SPACE
    case parity => throw new Exception(s"Unsupported parity type: $parity")
  }
}
