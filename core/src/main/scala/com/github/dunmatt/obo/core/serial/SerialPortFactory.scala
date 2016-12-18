package com.github.dunmatt.obo.core.serial

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SerialPortFactory {
  private var allOpenedPorts = Set.empty[Future[SerialPort]]
  
  def closeEverything: Unit = allOpenedPorts.foreach { _.onSuccess{case p => p.close} }

  def requestSerialPort(req: SerialPortRequest): Future[SerialPort] = {
    val port = buildSerialPort(req)
    allOpenedPorts = allOpenedPorts + port
    port
  }

  protected def buildSerialPort(req: SerialPortRequest): Future[SerialPort]
}
