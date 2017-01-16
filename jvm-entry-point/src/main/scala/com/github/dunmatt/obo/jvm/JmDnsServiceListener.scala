package com.github.dunmatt.obo.jvm

import com.github.dunmatt.obo.core.{ Component, ComponentMetadata, Constants }
import java.net.URL
import java.util.UUID
import javax.jmdns.{ JmDNS, ServiceEvent, ServiceInfo, ServiceListener }

class JmDnsServiceListener(dnssd: JmDNS, component: Component) extends ServiceListener {
  private val logName = classOf[JmDnsServiceListener].getName

  protected def serviceInfoToMetadata(info: ServiceInfo): Option[ComponentMetadata] = {
    for {
      id <- Option(info.getPropertyString(Constants.COMPONENT_ID_KEY))
      cls <- Option(info.getPropertyString(Constants.COMPONENT_NAME_KEY))
      bp <- Option(info.getPropertyString(Constants.BROADCAST_PORT_KEY))
    } yield {
      val url = new URL(Constants.PROTOCOL_NAME, info.getHostAddress, info.getPort, "")
      val broadcastUrl = new URL(Constants.PROTOCOL_NAME, info.getHostAddress, bp.toInt, "")
      new ComponentMetadata(UUID.fromString(id), cls, url, broadcastUrl)
    }
  }

  override def serviceAdded(x: ServiceEvent) = component.log.debug(logName, s"Heard about $x.")
  
  override def serviceRemoved(x: ServiceEvent) = {
    component.log.debug(logName, s"$x is no longer available.")
    serviceInfoToMetadata(x.getInfo).foreach { meta =>
      component.forgetComponent(meta)
    }
  }

  override def serviceResolved(x: ServiceEvent) = {
    val info = x.getInfo
    component.log.debug(logName, s"resolved ${info.getType == Constants.JMDNS_SERVICE_TYPE} $info")
    if (info.getType == Constants.JMDNS_SERVICE_TYPE) {
      serviceInfoToMetadata(info).foreach { meta =>
        component.addDiscoveredComponent(meta)
      }
    }
  }
}
