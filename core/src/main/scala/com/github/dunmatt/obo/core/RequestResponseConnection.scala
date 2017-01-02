package com.github.dunmatt.obo.core

import com.github.dunmatt.obo.core.msgpack.MsgReader
import java.net.URL
import org.zeromq.ZMQ
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success, Try }
import zmq.ZMQ.ZMQ_SNDMORE

class RequestResponseConnection(url: URL, log: OboLogger)(implicit val zctx: ZMQ.Context) extends Connection {
  def this(u: String, l: OboLogger)(implicit z: ZMQ.Context) = this(new URL(u), l)
  import RequestResponseConnection._
  private val logName = classOf[RequestResponseConnection].getName
  private val socket = zctx.socket(ZMQ.REQ)
  private val metaMessageFactory = new MetaMessageFactory
  private var factoryCache = Map.empty[String, MessageFactory[_ <: Message[_]]]
  socket.connect(url.toString)
  log.info(logName, s"Connected to $url")
  
  def send(msg: Message[_]): Future[Option[Message[_]]] = {
    Future {
      socket.synchronized {
        // log.debug(logName, s"""About to send $msg (${msg.getBytes.mkString(", ")})""")
        socket.send(MetaMessage(msg.factory.getName).getBytes, ZMQ_SNDMORE)
        socket.send(msg.getBytes)
        log.debug(logName, s"Sent $msg")
        // TODO: put a timeout here
        val reply = socket.recv(0)
        reply.toSeq match {  // TODO: possible null ref here, fix it!!
          case ACK => None  // this is fine, there was just no response
          case NACK => log.warn(logName, "Got a NACK back, connection may be in a weird state."); None
          case _ => handleReceivedMetaBytes(reply)
        }
      }
    }
  }

  private def handleReceivedMetaBytes(bytes: Array[Byte]): Option[Message[_]] = {
    metaMessageFactory.unpack(new MsgReader(bytes)) match {
      case Success(meta) => handleReceivedMessage(receiveMessage(meta.factoryClassName))
      case Failure(e) =>
        log.error(logName, s"Couldn't parse [${bytes.mkString(",")}] as a MetaMessage.", e)
        abortRecv
    }
  }

  private def handleReceivedMessage(m: Try[Message[_]]): Option[Message[_]] = m match {
    case Success(msg) => m.toOption
    case Failure(e) =>
      log.error(logName, s"Couldn't receive or parse main message, failed with: ", e)
      abortRecv
  }

  private def abortRecv: Option[Message[_]] = {
    while (socket.hasReceiveMore) {
      socket.recv(0)
    }
    None
  }

  private def makeFactory(name: String): MessageFactory[_ <: Message[_]] = {
    Try {
      Class.forName(name).newInstance.asInstanceOf[MessageFactory[_ <: Message[_]]]
    }.getOrElse(new RawMessageFactory)
  }

  private def receiveMessage(factoryName: String): Try[Message[_]] = {
    if (!factoryCache.contains(factoryName)) {
      factoryCache = factoryCache + ((factoryName -> makeFactory(factoryName)))
    }
    factoryCache(factoryName).unpack(new MsgReader(socket.recv(0)))
  }

  override def close {
    super.close
    log.info(logName, s"Closing connection to $url")
    socket.close
  }
}

object RequestResponseConnection {
  val ACK = ComponentRunner.ACK.toSeq
  val NACK = ComponentRunner.NACK.toSeq
}
