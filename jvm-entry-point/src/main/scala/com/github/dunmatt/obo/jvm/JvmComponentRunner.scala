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
                                 , Constants.RPC_SERVICE_DNSSD_NAME
                                 , servicePort
                                 , "The main Obo RPC interface, send your semantic queries here!")
    // why this method is called setText is beyond me... probably beyond explanation in the mortal realm
    info.setText(Map( Constants.COMPONENT_NAME_KEY -> c.name
                    // , Constants.LOG_PORT_KEY       -> c.log.loggingPort.toString
                    ))
    c.log.info(logName, s"Now advertizing $info via DNS-SD")
    dnssd.registerService(info)
  }

  def go: Unit = constructComponent(component).map { c =>
    c.setConnectionFactory(new JmDnsConnectionFactory(dnssd, c.log))
    c.setSerialPortFactory(new RxtxSerialPortFactory)
    advertizeComponent(c)
    listeningForData = true
    mainLoop(c)
  }

  override def stop: Unit = {
    listeningForData = false
    dnssd.unregisterAllServices
    super.stop
  }
}
