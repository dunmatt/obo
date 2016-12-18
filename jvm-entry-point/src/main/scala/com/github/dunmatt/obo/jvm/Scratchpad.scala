// package com.github.dunmatt.obo.jvm

// import javax.jmdns._
// import org.apache.jena.rdf.model.ModelFactory
// import org.zeromq.ZMQ
// // import scala.concurrent.ExecutionContext.Implicits.global
// // import scala.concurrent.Future


// object Thinker extends App {
//   val m = ModelFactory.createDefaultModel
//   val url = Thread.currentThread.getContextClassLoader.getResource("com/github/dunmatt/obo/core/scratchpad.ttl")
//   m.read(url.toString, "TURTLE")
// }

// object Server extends App {
//   val socket = ZMQ.context(1).socket(ZMQ.REP)
//   val port = socket.bindToRandomPort("tcp://*")

//   JmDNS.create.registerService(ServiceInfo.create( Constants.DNSSD_SERVICE_TYPE
//                                                  , Constants.RPC_SERVICE_DNSSD_NAME
//                                                  , port
//                                                  , "The main Obo RPC interface, send your semantic queries here!"))

//   while (true) {
//     val req = socket.recv(0)
//     println(s"Got $req")
//     socket.send(req, 0)
//   }
// }

// // NOTE: sometimes this doesn't like to reconnect... why?
// object Client extends App {
//   val socket = ZMQ.context(1).socket(ZMQ.REQ)

//   JmDNS.create.addServiceListener(Constants.DNSSD_SERVICE_TYPE, new ServiceListener {
//     def serviceAdded(x: ServiceEvent) = println(s"ADDING $x")
//     def serviceRemoved(x: ServiceEvent) = println(s"REMOVING $x")  // this should probably do something....
//     def serviceResolved(x: ServiceEvent) = {
//       val url = x.getInfo.getURLs("tcp").head
//       socket.connect(url)
//       println(s"Connected to $url")

//       while (true) {
//         socket.send("Poke".getBytes)
//         println(s"Got back: ${ new String(socket.recv(0)) }")
//         Thread.sleep(500)
//       }
//     }
//   })

//   Thread.sleep(1000)
// }
