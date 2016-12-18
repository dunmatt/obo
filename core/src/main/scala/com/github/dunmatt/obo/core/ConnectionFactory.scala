package com.github.dunmatt.obo.core

import scala.concurrent.Future

trait ConnectionFactory {
  def connectTo(id: OboIdentifier): Future[Connection]
}
