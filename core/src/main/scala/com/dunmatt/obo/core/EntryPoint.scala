package com.github.dunmatt.obo.core

import java.nio.file.{ Files, Paths }
import org.slf4j.LoggerFactory
// import scala.io.Source
import scala.util.Try

object EntryPoint {
  private val log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    args.foreach(processArgument)
  }

  def processArgument(arg: String): Unit = {
    val component = Try(Class.forName(arg)).filter(isComponent).map(_.newInstance.asInstanceOf[Component])
    log.info(s"$component")
    // TODO: is it a file?
    // TODO: read the file into jena
    val mainThread = Thread.currentThread
    Runtime.getRuntime.addShutdownHook(new Thread() {override def run = {
      log.info("Shutdown hook triggered.")
      component.foreach(_.halt)
      mainThread.join
    }})
    component.foreach(_.processMessageQueue)
    log.debug("exiting processArgument")
  }

  private def isComponent(c: Class[_]): Boolean = classOf[Component].isAssignableFrom(c)

  // private def loadRdf(filename: String): Try[String] = {
  //   Try {
  //     val source = Source.fromFile(filename)
  //     try source.getLines.mkString("\n") finally source.close
  //   }
  // }
}
