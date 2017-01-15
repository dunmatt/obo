package com.github.dunmatt.obo.jvm

import com.github.dunmatt.obo.core.Component
import java.nio.file.{ Files, Paths }
import org.slf4j.LoggerFactory
// import scala.io.Source
// import scala.concurrent.Await
// import scala.concurrent.duration.Duration
// import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object EntryPoint {
  private val log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    // processArgument("com.github.dunmatt.obo.iRobotCreate.CreateComponent")
    args.foreach(processArgument)
  }

  def processArgument(arg: String): Unit = {
    val component = Try(Class.forName(arg)).filter(isComponent).getOrElse(classOf[ScratchpadComponent])
    // log.info(s"$component")
    // TODO: is it a file?
    // TODO: read the file into jena
    val runner = new JvmComponentRunner(component)

    val mainThread = Thread.currentThread
    Runtime.getRuntime.addShutdownHook(new Thread() {override def run = {
      log.info("Shutdown hook triggered.")
      runner.stop
      mainThread.join
    }})
    runner.go
    // component.foreach(_.processMessageQueue)
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
