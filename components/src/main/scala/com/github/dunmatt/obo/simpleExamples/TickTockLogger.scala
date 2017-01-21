package com.github.dunmatt.obo.simpleExamples

import com.github.dunmatt.obo.core.{ Component, Message }

class TickTockLogger() extends Component {
  val ui = new UiServerScratchpad(8008)
  var keepRunning = true

  override def handleMessage(m: Message[_]): Option[Message[_]] = None

  override def onHalt = {
    keepRunning = false
    super.onHalt
  }

  override def onStart = {
    super.onStart
    new Thread(new Runnable {
      def run {
        while (keepRunning) {
          log.info("Tick")
          Thread.sleep(1000)
          log.info("Tock")
          Thread.sleep(1000)
        }
      }
    }).start
  }
}

import fi.iki.elonen.NanoHTTPD
import java.net.URL
import scala.io.Source

class UiServerScratchpad(port: Int) extends NanoHTTPD(port) {
  import UiServerScratchpad._
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
}

object UiServerScratchpad {
  val RESOURCE_BASE = "/ui/web"
}
