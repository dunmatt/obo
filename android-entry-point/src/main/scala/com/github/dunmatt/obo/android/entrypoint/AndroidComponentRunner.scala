package com.github.dunmatt.obo.android.entrypoint

import android.content.Context
import android.net.nsd.{ NsdManager, NsdServiceInfo }
import com.github.dunmatt.obo.android.core.AndroidComponent
import com.github.dunmatt.obo.android.entrypoint.serial.AndroidSerialPortFactory
import com.github.dunmatt.obo.core.{ Component, ComponentRunner, Constants }
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ
import scala.util.{ Failure, Success }

class AndroidComponentRunner(componentName: String)(implicit context: Context) extends ComponentRunner {
  private val logName = classOf[AndroidComponentRunner].getName
  protected val backupLog = LoggerFactory.getLogger(getClass)
  protected val nsdManager = context.getSystemService(classOf[NsdManager])
  protected val component = constructComponent(componentName)
  component match {
    case Success(c) =>
      val ncf =  new NsdConnectionFactory(nsdManager, c.log)
      c.connectionFactory = ncf
      c.serialPortFactory = new AndroidSerialPortFactory(context, zctx)
      nsdManager.discoverServices(Constants.DNSSD_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, ncf)
      c match {
        case ac: AndroidComponent =>
          ac.context = context
        case _ => Unit
      }
    case Failure(e) => backupLog.error(s"Couldn't construct a $componentName", e)
  }
  protected var listeningForData = true

  def mainLoop: Unit = component.foreach { c =>
    advertizeComponent(c)
    mainLoop(c)
  }

  protected def advertizeComponent(c: Component): Unit = {
    val info = new NsdServiceInfo
    info.setServiceName(Constants.RPC_SERVICE_DNSSD_NAME)
    info.setServiceType(Constants.DNSSD_SERVICE_TYPE)
    info.setAttribute(Constants.COMPONENT_NAME_KEY, c.name)
    info.setAttribute(Constants.LOG_PORT_KEY, c.log.loggingPort.toString)
    info.setPort(servicePort)
    c.log.info(logName, s"Now advertizing $info via DNS-SD")
    nsdManager.registerService(info, NsdManager.PROTOCOL_DNS_SD, registrationListener)
  }

  override def stop: Unit = {
    listeningForData = false
    nsdManager.unregisterService(registrationListener)
    component.foreach { c =>
      c.connectionFactory match {
        case listener: NsdManager.DiscoveryListener => nsdManager.stopServiceDiscovery(listener)
        case _ => c.log.error(logName, "The component's connection factory no longer implements DiscoveryListener, fix AndroidComponentRunner!")
      }
    }
    super.stop
  }

  private val registrationListener = new NsdManager.RegistrationListener {
    override def onServiceRegistered(info: NsdServiceInfo): Unit = component match {
      case Success(c) => c.log.info(logName, s"Successfully registered ${info.getServiceName} with DNS-SD.")
      case _ => backupLog.info(s"Successfully registered ${info.getServiceName} with DNS-SD.")
    }
    override def onRegistrationFailed(info: NsdServiceInfo, err: Int): Unit = component match {
      case Success(c) => c.log.warn(logName, s"Failed to register ${info.getServiceName} on account of an error (code $err), other components won't be able to see this one.")
      case _ => backupLog.warn(s"Failed to register ${info.getServiceName} on account of an error (code $err), other components won't be able to see this one.")
    }
    override def onServiceUnregistered(info: NsdServiceInfo): Unit = component match {
      case Success(c) => c.log.info(logName, s"Successfully unregistered ${info.getServiceName}, other components will no longer see this one.")
      case _ => backupLog.info(s"Successfully unregistered ${info.getServiceName}, other components will no longer see this one.")
    }
    override def onUnregistrationFailed(info: NsdServiceInfo, err: Int): Unit = component match {
      case Success(c) => c.log.warn(logName, s"Failed to unregister ${info.getServiceName} due to error code $err... probably doesn't matter... probably.")
      case _ => backupLog.warn(s"Failed to unregister ${info.getServiceName} due to error code $err... probably doesn't matter... probably.")
    }
  }
}
