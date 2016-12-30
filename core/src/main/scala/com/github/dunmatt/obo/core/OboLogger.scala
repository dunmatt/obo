package com.github.dunmatt.obo.core

import org.slf4j.{ Logger, Marker }
import org.slf4j.event.Level

abstract class OboLogger(basis: Logger) extends Logger {

  protected def publish(level: Level, msg: String): Unit = publish(getName, level, msg)

  protected def publish(name: String, level: Level, msg: String): Unit

  def isLevelEnabled(level: Level): Boolean = level match {
    case Level.DEBUG => isDebugEnabled
    case Level.ERROR => isErrorEnabled
    case Level.INFO => isInfoEnabled
    case Level.TRACE => isTraceEnabled
    case Level.WARN => isWarnEnabled
    case _ => false
  }

  def getName = basis.getName

  def isDebugEnabled            = basis.isDebugEnabled
  def isDebugEnabled(m: Marker) = basis.isDebugEnabled(m)
  def isErrorEnabled            = basis.isErrorEnabled
  def isErrorEnabled(m: Marker) = basis.isErrorEnabled(m)
  def isInfoEnabled             = basis.isInfoEnabled
  def isInfoEnabled(m: Marker)  = basis.isInfoEnabled(m)
  def isTraceEnabled            = basis.isTraceEnabled
  def isTraceEnabled(m: Marker) = basis.isTraceEnabled(m)
  def isWarnEnabled             = basis.isWarnEnabled
  def isWarnEnabled(m: Marker)  = basis.isWarnEnabled(m)

  def debug(name: String, msg: String): Unit = {
    publish(name, Level.DEBUG, msg)
    basis.debug(msg)
  }

  def error(name: String, msg: String): Unit = {
    publish(name, Level.ERROR, msg)
    basis.error(msg)
  }

  def info(name: String, msg: String): Unit = {
    publish(name, Level.INFO, msg)
    basis.info(msg)
  }

  def trace(name: String, msg: String): Unit = {
    publish(name, Level.TRACE, msg)
    basis.trace(msg)
  }

  def warn(name: String, msg: String): Unit = {
    publish(name, Level.WARN, msg)
    basis.warn(msg)
  }

  def debug(m: Marker, msg: String): Unit = {
    publish(Level.DEBUG, msg)
    basis.debug(m, msg)
  }

  def debug(m: Marker, format: String, args: Object*): Unit = debug(m, format.format(args: _*))

  def debug(m: Marker, format: String, arg: Object): Unit = debug(m, format.format(arg))

  def debug(m: Marker, format: String, arg1: Object, arg2: Object): Unit = debug(m, format.format(arg1, arg2))

  def debug(m: Marker, msg: String, t: Throwable): Unit = {
    publish(Level.DEBUG, s"$msg\n${t.getMessage}")  // TODO: is getMessage really what we want here?
    basis.debug(m, msg, t)
  }

  def debug(msg: String): Unit = {
    publish(Level.DEBUG, msg)
    basis.debug(msg)
  }

  def debug(format: String, args: Object*): Unit = debug(format.format(args: _*))

  def debug(format: String, arg: Object): Unit = debug(format.format(arg))

  def debug(format: String, arg1: Object, arg2: Object): Unit = debug(format.format(arg1, arg2))

  def debug(msg: String, t: Throwable): Unit = {
    publish(Level.DEBUG, s"$msg\n${t.getMessage}")  // TODO: is getMessage really what we want here?
    basis.debug(msg, t)
  }

  def error(m: Marker, msg: String): Unit = {
    publish(Level.ERROR, msg)
    basis.error(m, msg)
  }

  def error(m: Marker, format: String, args: Object*): Unit = error(m, format.format(args: _*))

  def error(m: Marker, format: String, arg: Object): Unit = error(m, format.format(arg))

  def error(m: Marker, format: String, arg1: Object, arg2: Object): Unit = error(m, format.format(arg1, arg2))

  def error(m: Marker, msg: String, t: Throwable): Unit = {
    publish(Level.ERROR, s"$msg\n${t.getMessage}")  // TODO: is getMessage really what we want here?
    basis.error(m, msg, t)
  }

  def error(msg: String): Unit = {
    publish(Level.ERROR, msg)
    basis.error(msg)
  }

  def error(format: String, args: Object*): Unit = error(format.format(args: _*))

  def error(format: String, arg: Object): Unit = error(format.format(arg))

  def error(format: String, arg1: Object, arg2: Object): Unit = error(format.format(arg1, arg2))

  def error(msg: String, t: Throwable): Unit = {
    publish(Level.ERROR, s"$msg\n${t.getMessage}")  // TODO: is getMessage really what we want here?
    basis.error(msg, t)
  }

  def info(m: Marker, msg: String): Unit = {
    publish(Level.INFO, msg)
    basis.info(m, msg)
  }

  def info(m: Marker, format: String, args: Object*): Unit = info(m, format.format(args: _*))

  def info(m: Marker, format: String, arg: Object): Unit = info(m, format.format(arg))

  def info(m: Marker, format: String, arg1: Object, arg2: Object): Unit = info(m, format.format(arg1, arg2))

  def info(m: Marker, msg: String, t: Throwable): Unit = {
    publish(Level.INFO, s"$msg\n${t.getMessage}")  // TODO: is getMessage really what we want here?
    basis.info(m, msg, t)
  }

  def info(msg: String): Unit = {
    publish(Level.INFO, msg)
    basis.info(msg)
  }

  def info(format: String, args: Object*): Unit = info(format.format(args: _*))

  def info(format: String, arg: Object): Unit = info(format.format(arg))

  def info(format: String, arg1: Object, arg2: Object): Unit = info(format.format(arg1, arg2))

  def info(msg: String, t: Throwable): Unit = {
    publish(Level.INFO, s"$msg\n${t.getMessage}")  // TODO: is getMessage really what we want here?
    basis.info(msg, t)
  }

  def trace(m: Marker, msg: String): Unit = {
    publish(Level.TRACE, msg)
    basis.trace(m, msg)
  }

  def trace(m: Marker, format: String, args: Object*): Unit = trace(m, format.format(args: _*))

  def trace(m: Marker, format: String, arg: Object): Unit = trace(m, format.format(arg))

  def trace(m: Marker, format: String, arg1: Object, arg2: Object): Unit = trace(m, format.format(arg1, arg2))

  def trace(m: Marker, msg: String, t: Throwable): Unit = {
    publish(Level.TRACE, s"$msg\n${t.getMessage}")  // TODO: is getMessage really what we want here?
    basis.trace(m, msg, t)
  }

  def trace(msg: String): Unit = {
    publish(Level.TRACE, msg)
    basis.trace(msg)
  }

  def trace(format: String, args: Object*): Unit = trace(format.format(args: _*))

  def trace(format: String, arg: Object): Unit = trace(format.format(arg))

  def trace(format: String, arg1: Object, arg2: Object): Unit = trace(format.format(arg1, arg2))

  def trace(msg: String, t: Throwable): Unit = {
    publish(Level.TRACE, s"$msg\n${t.getMessage}")  // TODO: is getMessage really what we want here?
    basis.trace(msg, t)
  }

  def warn(m: Marker, msg: String): Unit = {
    publish(Level.WARN, msg)
    basis.warn(m, msg)
  }

  def warn(m: Marker, format: String, args: Object*): Unit = warn(m, format.format(args: _*))

  def warn(m: Marker, format: String, arg: Object): Unit = warn(m, format.format(arg))

  def warn(m: Marker, format: String, arg1: Object, arg2: Object): Unit = warn(m, format.format(arg1, arg2))

  def warn(m: Marker, msg: String, t: Throwable): Unit = {
    publish(Level.WARN, s"$msg\n${t.getMessage}")  // TODO: is getMessage really what we want here?
    basis.warn(m, msg, t)
  }

  def warn(msg: String): Unit = {
    publish(Level.WARN, msg)
    basis.warn(msg)
  }

  def warn(format: String, args: Object*): Unit = warn(format.format(args: _*))

  def warn(format: String, arg: Object): Unit = warn(format.format(arg))

  def warn(format: String, arg1: Object, arg2: Object): Unit = warn(format.format(arg1, arg2))

  def warn(msg: String, t: Throwable): Unit = {
    publish(Level.WARN, s"$msg\n${t.getMessage}")  // TODO: is getMessage really what we want here?
    basis.warn(msg, t)
  }
}
