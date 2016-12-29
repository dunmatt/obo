package com.github.dunmatt.obo.jvm

import com.github.dunmatt.obo.core.{ Connection, ConnectionFactory, Constants, OboIdentifier, OboLogger, RequestResponseConnection }
import javax.jmdns.{ JmDNS, ServiceEvent, ServiceInfo, ServiceListener }
import org.zeromq.ZMQ
import scala.concurrent.{ Future, Promise }
import scala.util.Try

// TODO: write a generic wrapper interface for Service Infos and push most of this into ConnectionFactory
class JmDnsConnectionFactory(dnssd: JmDNS, log: OboLogger)(implicit zctx: ZMQ.Context) extends ConnectionFactory with ServiceListener {
  private val logName = classOf[JmDnsConnectionFactory].getName
  private var pendingConnections = Set.empty[(OboIdentifier, Promise[Connection])]
  private var services = Set.empty[ServiceInfo]
  dnssd.addServiceListener(Constants.JMDNS_SERVICE_TYPE, this)

  def connectTo(oi: OboIdentifier): Future[Connection] = {
    val p = Promise[Connection]
    services.find { info => satisfiesIdentifier(oi, info) } match {
      case Some(service) => connect(service, p)
      case None => pendingConnections = pendingConnections + ((oi, p))
    }
    p.future
  }

  protected def connect(info: ServiceInfo, p: Promise[Connection]): Unit = {
    val url = Try(info.getURLs("tcp").head)
    url.foreach { address => log.info(logName, s"Connecting to ${info.getPropertyString(Constants.COMPONENT_NAME_KEY)} @ $address") }
    p.complete(url.map(u => new RequestResponseConnection(u, log)))
  }

  protected def satisfiesIdentifier(oi: OboIdentifier, info: ServiceInfo): Boolean = {
    oi.refersToServiceNamed(info.getPropertyString(Constants.COMPONENT_NAME_KEY))
  }

  override def serviceAdded(x: ServiceEvent) = log.debug(logName, s"Heard about $x.")  // TODO: perhaps print getPropertyString(Constants.COMPONENT_NAME_KEY) instead of the whole object
  
  override def serviceRemoved(x: ServiceEvent) = {
    log.debug(logName, s"$x is no longer available.")
    services = services - x.getInfo
  }

  override def serviceResolved(x: ServiceEvent) = {
    val info = x.getInfo
    log.debug(logName, s"resolved $info")
    if (info.getType == Constants.JMDNS_SERVICE_TYPE) {
      services = services + info
      pendingConnections.find { case (oi, _) => satisfiesIdentifier(oi, info) } match {
        case Some((oi, p)) => connect(info, p)
        case _ => Unit
      }
    }
  }
}
