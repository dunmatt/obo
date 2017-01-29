package com.github.dunmatt.obo.jvm

import com.github.dunmatt.obo.core.{ Component, OperationalFlag, TypedOperationalParameter, UrlStreamHandlerSetup }
import java.nio.file.{ Files, Paths }
import org.apache.commons.cli
import org.apache.commons.cli.{ DefaultParser, HelpFormatter, Options }
import org.slf4j.LoggerFactory
// import scala.io.Source
import scala.util.{ Failure, Success, Try }

object EntryPoint extends UrlStreamHandlerSetup {
  private val log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    val options = new Options
    options.addOption("c", "component", true, "Specifies which component to run.")
    options.addOption("h", "help", false, "Shoves a super helpful message all up in your console.")
    val line = (new DefaultParser()).parse(options, args)
    if (line.hasOption("help")) {
      if (line.hasOption("component")) {
        populateOptionsFromComponentClassname(line.getOptionValue("component"), options)
      }
      (new HelpFormatter()).printHelp("jvmEntryPoint/run", options)
    } else {
      runComponent(line.getOptionValue("component"), args)
    }
  }

  def runComponent(name: String, args: Array[String]): Unit = {
    Try(Class.forName(name)).filter(isComponent) match {
      case Success(componentCls) => runComponent(componentCls, args)
      case _ => log.error(s"Couldn't find a component called $name")
    }
  }

  def runComponent(componentCls: Class[_], args: Array[String]): Unit = {
    val runner = new JvmComponentRunner(componentCls)
    // parseArguments(runner.getParameters, args)
    val mainThread = Thread.currentThread
    Runtime.getRuntime.addShutdownHook(new Thread() {override def run = {
      log.info("Shutdown hook triggered.")
      runner.stop
      mainThread.join
    }})
    runner.go
    // component.foreach(_.processMessageQueue)
    log.debug("exiting runComponent")
  }

  def parseArguments(params: Set[TypedOperationalParameter[_]], args: Array[String]): Unit = {
    val options = new Options
    params.map(paramToOption).foreach(options.addOption)
    val parse = (new DefaultParser()).parse(options, args)
    params.foreach {
      case flag: OperationalFlag =>
        flag.value = parse.hasOption(flag.name)
      case param if parse.hasOption(param.name) =>
        param.fromString(parse.getOptionValue(param.name))
    }
  }

  def paramToOption(param: TypedOperationalParameter[_]): cli.Option = param match {
    case flag: OperationalFlag =>
      cli.Option.builder(param.name).desc(param.description).build
    case _ =>
      cli.Option.builder(param.name).desc(param.description).hasArg.build
  }

  def populateOptionsFromComponentClassname(name: String, options: Options): Unit = {
    try {
      new JvmComponentRunner(Class.forName(name)).constructComponent(name).foreach { c =>
        c.parameters.map(paramToOption).foreach(options.addOption)
      }
    } catch {
      case t: Throwable => Unit  // something went wrong, we don't really care
    }
  }

  private def isComponent(c: Class[_]): Boolean = classOf[Component].isAssignableFrom(c)

  // private def loadRdf(filename: String): Try[String] = {
  //   Try {
  //     val source = Source.fromFile(filename)
  //     try source.getLines.mkString("\n") finally source.close
  //   }
  // }
}
