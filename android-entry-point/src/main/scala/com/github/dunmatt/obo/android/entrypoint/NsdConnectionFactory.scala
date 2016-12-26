package com.github.dunmatt.obo.android.entrypoint

import android.content.Context
import android.net.nsd.{ NsdManager, NsdServiceInfo }
import com.github.dunmatt.obo.core.{ Connection, ConnectionFactory, Constants, OboIdentifier, RequestResponseConnection }
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ
import scala.concurrent.{ Future, Promise }
import scala.util.Try

class NsdConnectionFactory(nsdManager: NsdManager) extends ConnectionFactory
                                                   with NsdManager.DiscoveryListener {
  private val log = LoggerFactory.getLogger(getClass)
  private var pendingConnections = Set.empty[(OboIdentifier, Promise[Connection])]
  private var services = Set.empty[NsdServiceInfo]

  def connectTo(oi: OboIdentifier): Future[Connection] = {
    log.info(s"Looking for $oi")
    val p = Promise[Connection]
    pendingConnections = pendingConnections + ((oi, p))
    alertPendingConnections
    p.future
  }

  protected def connect(info: NsdServiceInfo, p: Promise[Connection]): Unit = {
    log.info(s"Connecting to ${info.getServiceName} (${info.getHost})")
    val url = s"tcp:/${info.getHost}:${info.getPort}"  // not a typo, the second slash comes from NSD (for some reason)
    val conn = Try(new RequestResponseConnection(url))
    p.complete(conn)
  }

  override def onDiscoveryStarted(regType: String): Unit = {
    log.info("NSD based Obo service discovery started.")
  }

  protected def satisfiesIdentifier(oi: OboIdentifier, info: NsdServiceInfo): Boolean = {
    Option(info.getAttributes.get(Constants.COMPONENT_NAME_KEY)).map { rawName =>
      oi.refersToServiceNamed(new String(rawName))
    }.getOrElse(false)
  }

  protected def alertPendingConnections: Unit = {
    pendingConnections = pendingConnections.filter { case (oi, p) =>
      val service = services.find(s => satisfiesIdentifier(oi, s))
      service.isEmpty
    }
  }

  override def onServiceFound(service: NsdServiceInfo): Unit = {
    if (service.getServiceType == Constants.DNSSD_SERVICE_TYPE) {
      nsdManager.resolveService(service, new NsdManager.ResolveListener {
        override def onServiceResolved(info: NsdServiceInfo): Unit = {
          log.info(s"found $info")
          services = services + info
          alertPendingConnections
        }
        override def onResolveFailed(info: NsdServiceInfo, err: Int): Unit = {
          log.error(s"Couldn't resolve service, error code $err.")
        }
      })
    }
  }

  override def onServiceLost(service: NsdServiceInfo): Unit = {
    services = services - service
  }

  override def onDiscoveryStopped(servType: String): Unit = {
    log.info("NSD service discovery stopped.")
  }

  override def onStartDiscoveryFailed(servType: String, err: Int): Unit = {
    log.error(s"Discovery failed due with error code: $err")
    nsdManager.stopServiceDiscovery(this)
  }

  override def onStopDiscoveryFailed(servType: String, err: Int): Unit = {
    log.error(s"Discovery failed due with error code: $err")
  }
}

