package com.github.dunmatt.obo.jvm

import com.github.dunmatt.obo.core.{ Component, ComponentRunner, Constants }
import com.github.dunmatt.obo.jvm.serial.RxtxSerialPortFactory
import javax.jmdns.{ JmDNS, ServiceInfo }
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ
import scala.collection.JavaConversions._
import scala.util.{ Failure, Success, Try }

class JvmComponentRunner(component: Class[_]) extends ComponentRunner {
  protected val log = LoggerFactory.getLogger(getClass)
  protected val socket = ZMQ.context(1).socket(ZMQ.REP)
  protected val port = socket.bindToRandomPort("tcp://*")
  protected val dnssd = JmDNS.create
  protected var listeningForData = false

  protected def advertizeComponent(name: String): Unit = {
    val info = ServiceInfo.create( Constants.DNSSD_SERVICE_TYPE
                                 , Constants.RPC_SERVICE_DNSSD_NAME
                                 , port
                                 , "The main Obo RPC interface, send your semantic queries here!")
    info.setText(Map((Constants.COMPONENT_NAME_KEY, name)))  // why this method is called setText is beyond me... probably beyond explanation in the mortal realm
    dnssd.registerService(info)
    log.info(s"Advertizing service $info")
  }

  def go: Unit = constructComponent(component).map { c =>
    c.connectionFactory = new JmDnsConnectionFactory(dnssd)
    c.serialPortFactory = new RxtxSerialPortFactory
    advertizeComponent(c.name)
    listeningForData = true
    mainLoop(c)
  }

  def halt: Unit = {
    listeningForData = false
    dnssd.unregisterAllServices
    log.info("Halting...")
    socket.close
  }
}
