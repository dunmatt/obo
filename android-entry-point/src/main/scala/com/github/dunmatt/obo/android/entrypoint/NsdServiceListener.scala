package com.github.dunmatt.obo.android.entrypoint

import android.net.nsd.{ NsdManager, NsdServiceInfo }
import com.github.dunmatt.obo.core.{ Component, ComponentMetadata, Constants }
import java.net.URL
import java.util.UUID

class NsdServiceListener(nsdManager: NsdManager, component: Component) extends NsdManager.DiscoveryListener {
  private val logName = classOf[NsdServiceListener].getName

  override def onDiscoveryStarted(regType: String): Unit = {
    component.log.info(logName, "NSD based Obo service discovery started.")
  }

  protected def serviceInfoToMetadata(info: NsdServiceInfo): Option[ComponentMetadata] = {
    for {
      id <- Option(info.getAttributes.get(Constants.COMPONENT_ID_KEY)).map(new String(_))
      cls <- Option(info.getAttributes.get(Constants.COMPONENT_NAME_KEY)).map(new String(_))
      bp <- Option(info.getAttributes.get(Constants.BROADCAST_PORT_KEY)).map(new String(_).toInt)
    } yield {
      val url = new URL(Constants.PROTOCOL_NAME, info.getHost.getHostAddress, info.getPort, "")
      val broadcastUrl = new URL(Constants.PROTOCOL_NAME, info.getHost.getHostAddress, bp, "")
      new ComponentMetadata(UUID.fromString(id), cls, url, broadcastUrl)
    }
  }

  override def onServiceFound(service: NsdServiceInfo): Unit = {
    if (service.getServiceType == Constants.DNSSD_SERVICE_TYPE) {
      nsdManager.resolveService(service, new NsdManager.ResolveListener {
        override def onServiceResolved(info: NsdServiceInfo): Unit = {
          component.log.debug(logName, s"found $info")
          serviceInfoToMetadata(info).foreach { meta =>
            component.addDiscoveredComponent(meta)
          }
        }
        override def onResolveFailed(info: NsdServiceInfo, err: Int): Unit = {
          component.log.error(logName, s"Couldn't resolve service, error code $err.")
        }
      })
    }
  }

  override def onServiceLost(service: NsdServiceInfo): Unit = {
    serviceInfoToMetadata(service).foreach { meta =>
      component.forgetComponent(meta)
    }
  }

  override def onDiscoveryStopped(servType: String): Unit = {
    component.log.info(logName, "NSD service discovery stopped.")
  }

  override def onStartDiscoveryFailed(servType: String, err: Int): Unit = {
    component.log.error(logName, s"Discovery failed due with error code: $err")
    nsdManager.stopServiceDiscovery(this)
  }

  override def onStopDiscoveryFailed(servType: String, err: Int): Unit = {
    component.log.error(logName, s"Discovery failed due with error code: $err")
  }
}

