package com.github.dunmatt.obo.core.web

import fi.iki.elonen.NanoHTTPD
import java.net.URL
import scala.io.Source

class UiServer(port: Int) extends NanoHTTPD(port) {
  import UiServer._
  import NanoHTTPD._
  start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)

  override def serve(session: IHTTPSession): Response = {
    val path = if (session.getUri == "/") "/index.html" else session.getUri
    getClass.getResourceAsStream(RESOURCE_BASE + path) match {
      case null =>
        newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", "Lol, Nope!  404 Biatch.")
      case stream =>
        val contents = Source.fromInputStream(stream).getLines.mkString("\n")
        newFixedLengthResponse(contents)
    }
  }

  val webSockets = new WebSocketService(port + 1)
}

object UiServer {
  val RESOURCE_BASE = "/ui/web"
}
