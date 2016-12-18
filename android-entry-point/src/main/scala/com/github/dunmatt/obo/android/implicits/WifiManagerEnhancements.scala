package com.github.dunmatt.obo.android

import android.net.wifi.WifiManager
import java.net.InetAddress
import java.nio.{ ByteBuffer, ByteOrder }

object WifiManagerEnhancements {
  implicit class WifiManagerWithIpAddress(wm: WifiManager) {
    // TODO: move this to a util class somewhere
    def intToIP(i: Int): InetAddress = {
      InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(i).array)
    }

    def ipAddress: Option[InetAddress] = {
      // TODO: test what happens with this if Wifi is turned off (it ought to work, but it needs testing)
      Option(wm.getConnectionInfo).map { info => intToIP(info.getIpAddress) }
    }
  }
}
