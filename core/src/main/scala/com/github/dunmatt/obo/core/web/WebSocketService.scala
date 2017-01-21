package com.github.dunmatt.obo.core.web

import fi.iki.elonen.{ NanoHTTPD, NanoWSD }
import scala.collection.concurrent.TrieMap

class WebSocketService(port: Int) extends NanoWSD(port) {
  import NanoWSD._
  import NanoHTTPD._

  protected val allConnections = TrieMap.empty[String, OboWebSocket]

  def broadcast(msg: String): Unit = {
    allConnections.values.foreach(_.send(msg))
  }

  override protected def openWebSocket(handshake: IHTTPSession): WebSocket = {
    val ip = handshake.getRemoteIpAddress
    val sock = new OboWebSocket(handshake, () => allConnections -= ip)
    allConnections += (ip -> sock)
    sock
  }
}
