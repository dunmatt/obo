package com.github.dunmatt.obo.core

case class OboIdentifier(raw: String) {
  def refersToServiceNamed(name: String): Boolean = raw == name  // TODO: do something smarter here...
}
