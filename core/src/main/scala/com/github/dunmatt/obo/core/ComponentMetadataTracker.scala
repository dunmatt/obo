package com.github.dunmatt.obo.core

import java.util.concurrent.ConcurrentHashMap
import java.util.UUID
import org.zeromq.ZMQ
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

private[core] trait ComponentMetadataTracker {
  val log: OboLogger
  implicit private[core] var zctx: ZMQ.Context = null  // this is populated by the runner
  private val logName = classOf[ComponentMetadataTracker].getName
  private[core] val metadataCache = new ConcurrentHashMap[UUID, ComponentMetadata]

  def addDiscoveredComponent(info: ComponentMetadata): Unit = {
    if (!metadataCache.containsKey(info.id)) {
      metadataCache.put(info.id, info)
    }
    requestCapabilities(info)
  }

  def requestCapabilities(info: ComponentMetadata): Unit = {
    val conn = new RequestResponseConnection(info.url, log)
    conn.send(new ComponentCapabilitiesRequest).onComplete { res =>
      conn.close
      res match {
        case Success(Some(cc: ComponentCapabilities)) =>
          info.setCapabilities(cc)
          // TODO: check if these capabilities satisfy a pending connection or need to be subscribed to
        case Success(x) => log.error(logName, s"Unexpected return message: $x")
        case Failure(e) => log.error(logName, s"Failed to get component capabilities due to $e", e)
      }
    }
  }

  // TODO: consider adding "delete by address", in case the full metadata isn't available
  def forgetComponent(id: UUID): Unit = metadataCache.remove(id)
  def forgetComponent(info: ComponentMetadata): Unit = metadataCache.remove(info.id)
}
