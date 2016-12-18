package com.github.dunmatt.obo.android

import android.content.Context
import android.net.nsd.{ NsdManager, NsdServiceInfo }
import com.github.dunmatt.obo.android.serial.AndroidSerialPortFactory
import com.github.dunmatt.obo.core.{ ComponentRunner, Constants }
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ
import scala.util.{ Failure, Success }

class AndroidComponentRunner(componentName: String)(implicit context: Context) extends ComponentRunner {
  protected val log = LoggerFactory.getLogger(getClass)
  protected val zctx = ZMQ.context(1)
  protected val socket = zctx.socket(ZMQ.REP)
  protected val port = socket.bindToRandomPort("tcp://*")
  protected val nsdManager = context.getSystemService(classOf[NsdManager])  // TODO: can this fail?  Should it be wrapped in a monad?
  protected val component = constructComponent(componentName)
  component match {
    case Success(c) =>
      val ncf =  new NsdConnectionFactory(nsdManager)
      c.connectionFactory = ncf
      c.serialPortFactory = new AndroidSerialPortFactory(context, zctx)
      nsdManager.discoverServices(Constants.DNSSD_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, ncf)
    case Failure(e) => log.error(s"Couldn't construct a $componentName", e)
  }
  protected var listeningForData = true
  def listening = listeningForData

  def mainLoop: Unit = component.foreach { c =>
    advertizeComponent(componentName)
    mainLoop(c)
  }

  protected def advertizeComponent(name: String): Unit = {
    val info = new NsdServiceInfo
    info.setServiceName(Constants.RPC_SERVICE_DNSSD_NAME)
    info.setServiceType(Constants.DNSSD_SERVICE_TYPE)
    info.setAttribute(Constants.COMPONENT_NAME_KEY, name)
    info.setPort(port)
    nsdManager.registerService(info, NsdManager.PROTOCOL_DNS_SD, registrationListener)
  }

  def stop: Unit = {
    listeningForData = false
    new Thread (new Runnable {
      override def run: Unit = {
        nsdManager.unregisterService(registrationListener)
        // component.foreach(_.serialPortFactory.closeEverything)
        component.foreach (_.connectionFactory match {
          case listener: NsdManager.DiscoveryListener => nsdManager.stopServiceDiscovery(listener)
          case _ => log.error("The component's connection factory no longer implements DiscoveryListener, fix AndroidComponentRunner!")
        })
        socket.close
      }
    }).start
  }

  private val registrationListener = new NsdManager.RegistrationListener {
    override def onServiceRegistered(info: NsdServiceInfo): Unit = {
      // TODO: consider storing the service name since the assigned name might not equal the requested one
      log.info(s"Successfully registered ${info.getServiceName} with DNS-SD.")
    }
    override def onRegistrationFailed(info: NsdServiceInfo, err: Int): Unit = {
      log.warn(s"Failed to register ${info.getServiceName} on account of an error (code $err), other components won't be able to see this one.")
    }
    override def onServiceUnregistered(info: NsdServiceInfo): Unit = {
      log.info(s"Successfully unregistered ${info.getServiceName}, other components will no longer see this one.")
    }
    override def onUnregistrationFailed(info: NsdServiceInfo, err: Int): Unit = {
      log.warn(s"Failed to unregister ${info.getServiceName} due to error code $err... probably doesn't matter... probably.")
    }
  }
}
