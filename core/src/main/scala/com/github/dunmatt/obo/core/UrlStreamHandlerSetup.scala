package com.github.dunmatt.obo.core

import java.net.{ URL, URLConnection, URLStreamHandler, URLStreamHandlerFactory }

trait UrlStreamHandlerSetup {
  URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory{
    override def createURLStreamHandler(protocol: String): URLStreamHandler = protocol match {
      case Constants.PROTOCOL_NAME =>
        new URLStreamHandler {
          // NOTE: this should never be called, if it is, consider implenting it
          override def openConnection(u: URL): URLConnection = ???
        }
      case _ => null
    }
  })
}
