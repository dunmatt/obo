package com.github.dunmatt.obo.android.serial

import com.felhr.usbserial.{ UsbSerialDevice, UsbSerialInterface }
import com.github.dunmatt.obo.core.serial.SerialPort
import java.io.{ InputStream, OutputStream }
import org.zeromq.ZMQ

class AndroidSerialPort(port: UsbSerialDevice, ctx: ZMQ.Context)
      extends SerialPort with UsbSerialInterface.UsbReadCallback {
  import AndroidSerialPort._
  private val sender = ctx.socket(ZMQ.PAIR)
  sender.bind(RECV_RELAY_ADDRESS)

  def close: Unit = {
    sender.send(Array(DONE), 0)
    sender.close
    port.close
  }

  def getInputStream: InputStream = new InputStream {
    private val receiver = ctx.socket(ZMQ.PAIR)
    receiver.connect(RECV_RELAY_ADDRESS)

    // TODO: add a byte buffer here so that onReceivedData can send whole arrays at once
    def read: Int = receiver.recv(0)(0) match {
      case DONE => -1
      case BYTE => receiver.recv(0)(0) & 0xff
      case EXCEPTION => throw new Exception(new String(receiver.recv(0)))
    }
  }

  def getOutputStream: OutputStream = new OutputStream {
    def write(b: Int): Unit = port.write(Array(b.toByte))
    override def write(b: Array[Byte]): Unit = port.write(b)
  }

  def onReceivedData(data: Array[Byte]): Unit = {
    data.foreach { b =>
      sender.send(Array(BYTE), ZMQ.SNDMORE)
      sender.send(Array(b), 0)
    }
  }
}

object AndroidSerialPort {
  val RECV_RELAY_ADDRESS = "inproc://android-serial-port"
  val DONE = 0.toByte
  val BYTE = 1.toByte
  // val DATA = 2.toByte
  val EXCEPTION = -1.toByte
}
