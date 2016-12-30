package com.github.dunmatt.obo.core

import scala.concurrent.Future

trait ConnectionFactory {
  def closeEverything: Unit = Unit  // TODO: write me (which means tracking connections)

  def connectTo(id: OboIdentifier): Future[Connection]
}
