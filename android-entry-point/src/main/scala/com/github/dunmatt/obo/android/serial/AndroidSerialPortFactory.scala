package com.github.dunmatt.obo.android.serial

import android.app.PendingIntent
import android.content.{ BroadcastReceiver, Context, Intent, IntentFilter }
import android.hardware.usb.{ UsbDevice, UsbManager }
import com.github.dunmatt.obo.core.serial.{ SerialPort, SerialPortFactory, SerialPortRequest }
import com.felhr.usbserial.{ UsbSerialDevice, UsbSerialInterface }
import org.zeromq.ZMQ
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Future, Promise }
import scala.util.{ Failure, Success, Try }

class AndroidSerialPortFactory(actx: Context, zctx: ZMQ.Context) extends SerialPortFactory {
  import AndroidSerialPortFactory._
  private val manager = actx.getSystemService(Context.USB_SERVICE).asInstanceOf[UsbManager]

  // TODO: clean this up!
  protected def buildSerialPort(req: SerialPortRequest): Future[SerialPort] = {
    val device = {
      val deviceP = Promise[UsbDevice]
      actx.registerReceiver(new BroadcastReceiver {
        def onReceive(context: Context, intent: Intent): Unit = {
          if (SERIAL_PERMISSION_NAME == intent.getAction) {
            val serialDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE).asInstanceOf[UsbDevice]
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
              deviceP.complete(Try{Option(serialDevice).get})
            } else {
              deviceP.complete(Failure(new Exception("Serial port permission denied by user... silly user.")))
            }
            actx.unregisterReceiver(this)
          }
        }
      }, new IntentFilter(SERIAL_PERMISSION_NAME))
      val permissionIntent = PendingIntent.getBroadcast(actx, 0, new Intent(SERIAL_PERMISSION_NAME), 0)
      val chosenDevice = manager.getDeviceList.values.head  // TODO: do something smarter than blindly taking the first port
      manager.requestPermission(chosenDevice, permissionIntent)
      deviceP.future
    }
    val connection = device.map { dev => manager.openDevice(dev) }
    connection.flatMap { conn =>
      device.map { dev =>
        val port = UsbSerialDevice.createUsbSerialDevice(dev, conn)
        port.open
        port.setBaudRate(req.baudRate)
        port.setDataBits(getDataBits(req))
        port.setStopBits(getStopBits(req))
        port.setParity(getParity(req))
        port.setFlowControl(FLOW_CONTROL_OFF)
        val result = new AndroidSerialPort(port, zctx)
        port.read(result)
        result
      }
    }
  }
}

object AndroidSerialPortFactory {
  val SERIAL_PERMISSION_NAME = "com.github.dunmatt.obo.android.serial.SERIAL_PORT_PERMISSION"

  // the following magic number madness brought to you by the fact that https://github.com/felHR85/UsbSerial/blob/master/usbserial/src/main/java/com/felhr/usbserial/UsbSerialInterface.java
  // doesn't expose its constants >:-|
  val FLOW_CONTROL_OFF = 0

  protected def getDataBits(req: SerialPortRequest): Int = req.dataBits match {
    case SerialPortRequest.DATABITS_8 => 8
    case SerialPortRequest.DATABITS_7 => 7
    case SerialPortRequest.DATABITS_6 => 6
    case SerialPortRequest.DATABITS_5 => 5
    case bits => throw new Exception(s"Unsupported number of data bits: $bits")
  }

  protected def getStopBits(req: SerialPortRequest): Int = req.stopBits match {
    case SerialPortRequest.STOPBITS_1 => 1
    case SerialPortRequest.STOPBITS_1_5 => 3
    case SerialPortRequest.STOPBITS_2 => 2
    case bits => throw new Exception(s"Unsupported number of stop bits: $bits")
  }

  protected def getParity(req: SerialPortRequest): Int = req.parity match {
    case SerialPortRequest.PARITY_NONE => 0
    case SerialPortRequest.PARITY_ODD => 1
    case SerialPortRequest.PARITY_EVEN => 2
    case SerialPortRequest.PARITY_MARK => 3
    case SerialPortRequest.PARITY_SPACE => 4
    case parity => throw new Exception(s"Unsupported parity type: $parity")
  }
}
