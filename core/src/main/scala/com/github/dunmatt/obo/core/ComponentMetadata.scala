package com.github.dunmatt.obo.core

import java.net.URL
import java.util.UUID

class ComponentMetadata(val id: UUID, val className: String, val url: URL, val broadcastUrl: URL) {
  var capabilities: Option[ComponentCapabilities] = None
}
