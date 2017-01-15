package com.github.dunmatt.obo.jvm

import com.github.dunmatt.obo.core.{ Component, ComponentMetadata, ConnectionFactory, Constants }
import java.net.URL
import java.util.UUID
import javax.jmdns.{ JmDNS, ServiceEvent, ServiceInfo, ServiceListener }

// TODO: write a generic wrapper interface for Service Infos and push most of this into ConnectionFactory
class JmDnsConnectionFactory(dnssd: JmDNS, component: Component) extends ConnectionFactory with ServiceListener {
  private val logName = classOf[JmDnsConnectionFactory].getName
  // private var pendingConnections = Set.empty[(OboIdentifier, Promise[Connection])]
  // private var services = Set.empty[ServiceInfo]
  dnssd.addServiceListener(Constants.JMDNS_SERVICE_TYPE, this)

  // def connectTo(oi: OboIdentifier): Future[Connection] = {
  //   val p = Promise[Connection]
  //   services.find { info => satisfiesIdentifier(oi, info) } match {
  //     case Some(service) => connect(service, p)
  //     case None => pendingConnections = pendingConnections + ((oi, p))
  //   }
  //   p.future
  // }

  // protected def connect(info: ServiceInfo, p: Promise[Connection]): Unit = {
  //   val url = Try(info.getURLs("tcp").head)
  //   url.foreach { address => component.log.info(logName, s"Connecting to ${info.getPropertyString(Constants.COMPONENT_NAME_KEY)} @ $address") }
  //   p.complete(url.map(u => new RequestResponseConnection(u, component.log)))
  // }

  // protected def satisfiesIdentifier(oi: OboIdentifier, info: ServiceInfo): Boolean = {
  //   oi.refersToServiceNamed(info.getPropertyString(Constants.COMPONENT_NAME_KEY))
  // }

  protected def serviceInfoToMetadata(info: ServiceInfo): Option[ComponentMetadata] = {
    for {
      id <- Option(info.getPropertyString(Constants.COMPONENT_ID_KEY))
      cls <- Option(info.getPropertyString(Constants.COMPONENT_NAME_KEY))
      bp <- Option(info.getPropertyString(Constants.BROADCAST_PORT_KEY))
    } yield {
      val url = new URL("tcp", info.getHostAddress, info.getPort, "")
      val broadcastUrl = new URL("tcp", info.getHostAddress, bp.toInt, "")
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
    component.log.debug(logName, s"resolved $info")
    if (info.getType == Constants.JMDNS_SERVICE_TYPE) {
      serviceInfoToMetadata(x.getInfo).foreach { meta =>
        component.addDiscoveredComponent(meta)
      }
      // pendingConnections.find { case (oi, _) => satisfiesIdentifier(oi, info) } match {
      //   case Some((oi, p)) => connect(info, p)
      //   case _ => Unit
      // }
    }
  }
}
