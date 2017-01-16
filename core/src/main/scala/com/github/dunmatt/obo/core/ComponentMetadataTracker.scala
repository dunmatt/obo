package com.github.dunmatt.obo.core

import java.util.UUID
import org.zeromq.ZMQ
import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

private[core] trait ComponentMetadataTracker {
  val log: OboLogger
  implicit private[core] var zctx: ZMQ.Context = null  // this is populated by the runner
  private[core] val metadataCache = TrieMap.empty[UUID, ComponentMetadata]
  private val logName = classOf[ComponentMetadataTracker].getName

  def addDiscoveredComponent(info: ComponentMetadata): Unit = {
    if (!metadataCache.contains(info.id)) {
      metadataCache += (info.id -> info)
    }
    requestCapabilities(info)
  }

  def requestCapabilities(info: ComponentMetadata): Unit = {
    log.debug(logName, s"Requesting capabilities from $info")
    val conn = new RequestResponseConnection(info.url, log)
    conn.send(new ComponentCapabilitiesRequest).onComplete { res =>
      conn.close
      res match {
        case Success(Some(cc: ComponentCapabilities)) =>
          log.debug(logName, s"Got some capabilities!!  $cc")
          info.capabilities = Some(cc)
          onComponentDiscovered(info)
        case Success(x) => log.error(logName, s"Unexpected return message: $x")
        case Failure(e) => log.error(logName, s"Failed to get component capabilities due to $e", e)
      }
    }
  }

  def getComponentsThatPublish(topic: RuntimeResourceName): Iterable[ComponentMetadata] = {
    metadataCache.values.filter(_.capabilities.exists(_.topics.contains(topic)))
  }

  private[core] def onComponentDiscovered(info: ComponentMetadata): Unit = Unit

  // TODO: consider adding "delete by address", in case the full metadata isn't available
  def forgetComponent(id: UUID): Unit = metadataCache -= id
  def forgetComponent(info: ComponentMetadata): Unit = metadataCache -= info.id
}
