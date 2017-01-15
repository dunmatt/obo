package com.github.dunmatt.obo.core

import java.net.URL

trait TopicSubscriber {
  def connectTo(component: ComponentMetadata): Unit
  def subscribeTo(topic: RuntimeResourceName): Unit
  def unsubscribeFrom(topic: RuntimeResourceName): Unit
}
