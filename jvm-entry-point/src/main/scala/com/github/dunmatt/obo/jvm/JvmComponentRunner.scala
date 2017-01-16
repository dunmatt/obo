package com.github.dunmatt.obo.jvm

import com.github.dunmatt.obo.core.{ Component, ComponentRunner, Constants }
import com.github.dunmatt.obo.jvm.serial.RxtxSerialPortFactory
import javax.jmdns.{ JmDNS, ServiceInfo }
import org.zeromq.ZMQ
import scala.collection.JavaConversions._
import scala.util.{ Failure, Success, Try }

class JvmComponentRunner(component: Class[_]) extends ComponentRunner {
  protected val logName = classOf[JvmComponentRunner].getName
  protected val dnssd = JmDNS.create
  protected var listeningForData = false

  protected def advertizeComponent(c: Component): Unit = {
    val info = ServiceInfo.create( Constants.DNSSD_SERVICE_TYPE
                                 // , Constants.RPC_SERVICE_DNSSD_NAME
                                 , s"${c.instanceId}.${Constants.RPC_SERVICE_DNSSD_NAME}"
                                 , servicePort
                                 , "The main Obo RPC interface, send your semantic queries here!")
    // why this method is called setText is beyond me... probably beyond explanation in the mortal realm
    info.setText(Map( Constants.COMPONENT_NAME_KEY -> c.name
                    , Constants.COMPONENT_ID_KEY   -> c.instanceId.toString
                    , Constants.BROADCAST_PORT_KEY -> broadcastPort.toString
                    ))
    c.log.info(logName, s"Now advertizing $info via DNS-SD")
    dnssd.registerService(info)
  }

  def go: Unit = constructComponent(component) match {
    case Success(c) =>
      val dnssdListener = new JmDnsServiceListener(dnssd, c)
      dnssd.addServiceListener(Constants.JMDNS_SERVICE_TYPE, dnssdListener)
      c.setSerialPortFactory(new RxtxSerialPortFactory)
      advertizeComponent(c)
      listeningForData = true
      mainLoop(c)
      dnssd.removeServiceListener(Constants.JMDNS_SERVICE_TYPE, dnssdListener)
    case Failure(e) =>
      throw e
  }

  override def stop: Unit = {
    listeningForData = false
    dnssd.unregisterAllServices
    super.stop
  }
}
