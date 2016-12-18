package com.github.dunmatt.obo.core.serial

import squants.time.Time
import squants.time.TimeConversions._

case class SerialPortRequest( name: Option[String] = None
                            , baudRate: Int = SerialPortRequest.BAUD_9600
                            , dataBits: Int = 8
                            , stopBits: Int = 1
                            , parity: Int = 0
                            , maxWait: Time = 10 seconds)

object SerialPortRequest {
  // TODO: consider moving these to somewhere more general?
  val BAUD_115200 = 115200
  val BAUD_57600 = 57600
  val BAUD_38400 = 38400
  val BAUD_19200 = 19200
  val BAUD_9600 = 9600

  val DATABITS_8 = 8
  val DATABITS_7 = 7
  val DATABITS_6 = 6
  val DATABITS_5 = 5

  val STOPBITS_1 = 1
  val STOPBITS_1_5 = 15
  val STOPBITS_2 = 2

  val PARITY_NONE = 0
  val PARITY_ODD = 1
  val PARITY_EVEN = 2
  val PARITY_MARK = 4
  val PARITY_SPACE = 5
}
