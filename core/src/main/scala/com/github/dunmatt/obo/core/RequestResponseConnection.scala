package com.github.dunmatt.obo.core

import com.github.dunmatt.obo.core.msgpack.MsgReader
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success, Try }
import zmq.ZMQ.ZMQ_SNDMORE

class RequestResponseConnection(url: String) extends Connection {
  import RequestResponseConnection._
  private val log = LoggerFactory.getLogger(getClass)
  private val socket = ZMQ.context(1).socket(ZMQ.REQ)  // TODO: should this context be an implicit param?
  private val metaMessageFactory = new MetaMessageFactory
  private var factoryCache = Map.empty[String, MessageFactory[_ <: Message[_]]]
  socket.connect(url)
  log.debug(s"Connected to $url")
  
  // TODO: add a timeout
  def send(msg: Message[_]): Future[Option[Message[_]]] = {
    Future {
      socket.synchronized {
        // TODO: these two logs should be debug
        log.info(s"""About to send $msg (${msg.getBytes.mkString(", ")})""")
        socket.send(MetaMessage(msg.factory.getName).getBytes, ZMQ_SNDMORE)
        socket.send(msg.getBytes, 0)
        log.info(s"Sent $msg")
        // TODO: put a timeout here
        val reply = socket.recv(0)
        reply.toSeq match {  // TODO: possible null ref here, fix it!!
          case ACK => None  // this is fine, there was just no response
          case NACK => log.warn("Got a NACK back, connection may be in a weird state."); None
          case _ => handleReceivedMetaBytes(reply)
        }
      }
    }
  }

  private def handleReceivedMetaBytes(bytes: Array[Byte]): Option[Message[_]] = {
    metaMessageFactory.unpack(new MsgReader(bytes)) match {
      case Success(meta) => handleReceivedMessage(receiveMessage(meta.factoryClassName))
      case Failure(e) =>
        log.error(s"Couldn't parse [${bytes.mkString(",")}] as a MetaMessage.", e)
        abortRecv
    }
  }

  private def handleReceivedMessage(m: Try[Message[_]]): Option[Message[_]] = m match {
    case Success(msg) => m.toOption
    case Failure(e) =>
      log.error(s"Couldn't receive or parse main message, failed with: ", e)
      abortRecv
  }

  private def abortRecv: Option[Message[_]] = {
    while (socket.hasReceiveMore) {
      socket.recv
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
}

object RequestResponseConnection {
  val ACK = ComponentRunner.ACK.toSeq
  val NACK = ComponentRunner.NACK.toSeq
}
