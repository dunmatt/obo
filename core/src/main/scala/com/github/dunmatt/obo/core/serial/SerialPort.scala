package com.github.dunmatt.obo.core.serial

import java.io.{ InputStream, OutputStream }

trait SerialPort {
  def close: Unit

  def getInputStream: InputStream

  def getOutputStream: OutputStream
}
