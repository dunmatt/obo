package com.github.dunmatt.obo.core

import javax.jmdns.{ JmDNS, ServiceInfo }
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegularJvmComponent() extends Component {
  private val llog = LoggerFactory.getLogger(classOf[RegularJvmComponent])
  private val socket = ZMQ.context(1).socket(ZMQ.REP)
  private val port = socket.bindToRandomPort("tcp://*")
  private var running = false

  JmDNS.create.registerService(ServiceInfo.create( Constants.DNSSD_SERVICE_TYPE
                                                 , Constants.RPC_SERVICE_DNSSD_NAME
                                                 , port
                                                 , "The main Obo RPC interface, send your semantic queries here!"))

  def handleMessage(m: Message) = Unit

  def sendMessage(m: Message, dest: OboIdentifier): Future[Message] = Future(Message.NULL_MESSAGE)

  final protected[core] def processMessageQueue: Unit = {
    log.info("Starting to process message queue.")
    running = true
    while (running) {
      try {
        val msg = socket.recv(ZMQ.NOBLOCK) match {
          case null => Thread.sleep(100)
          case msg => log.debug(s"Received $msg")
        }
      } catch {
        case e: Throwable => 
          log.error("Encountered an error while recv-ing: ", e)
      }
      // TODO: validate, decode, validate, and process the message
    }
  }

  final protected[core] def halt: Unit = {
    log.info("Halting...")
    running = false
  }

  final val log = LoggerFactory.getLogger(getClass)
}
