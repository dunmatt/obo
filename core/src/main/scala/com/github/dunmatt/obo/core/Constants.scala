package com.github.dunmatt.obo.core

object Constants {
  val BROADCAST_PORT_KEY = "broadcast_port"
  val COMPONENT_ID_KEY = "component_id"
  val COMPONENT_NAME_KEY = "component_name"
  val DNSSD_SERVICE_TYPE = "_obo._tcp."
  // val LOG_PORT_KEY = "log_port"
  val JMDNS_SERVICE_TYPE = DNSSD_SERVICE_TYPE + "local."
  val RPC_SERVICE_DNSSD_NAME = "obo-rpc"
}
