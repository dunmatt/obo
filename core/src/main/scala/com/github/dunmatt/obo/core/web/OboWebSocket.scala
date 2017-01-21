package com.github.dunmatt.obo.core.web

import fi.iki.elonen.NanoHTTPD.IHTTPSession
import fi.iki.elonen.NanoWSD
import fi.iki.elonen.NanoWSD.WebSocket
import java.io.IOException

// TODO: pass logging in here
class OboWebSocket(handshakeRequest: IHTTPSession, closeHandler: ()=>Unit) extends WebSocket(handshakeRequest) {
  import NanoWSD._

  override protected def onClose( code: WebSocketFrame.CloseCode
                                , reason: String
                                , closedRemotely: Boolean): Unit = {
    closeHandler()
  }

  override protected def onException(ex: IOException): Unit = {
  }

  override protected def onMessage(message: WebSocketFrame): Unit = {
    // TODO: do something with the message (perhaps parse it??)
  }

  override protected def onOpen(): Unit = {
  }

  override protected def onPong(pong: WebSocketFrame): Unit = {
  }
}
