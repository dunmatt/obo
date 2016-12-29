package com.github.dunmatt.obo.jvm.serial

import com.github.dunmatt.obo.core.serial.SerialPort
import gnu.io.CommPort
import java.io.{ InputStream, OutputStream }

class RxtxSerialPort(port: CommPort) extends SerialPort {
  def close: Unit = port.close

  def getInputStream: InputStream = port.getInputStream

  def getOutputStream: OutputStream = port.getOutputStream
}
