
libraryDependencies ++= Seq(
  "commons-cli" % "commons-cli" % "1.3.1",
  "org.apache.jena" % "jena-core" % "3.1.0",
  "org.jmdns" % "jmdns" % "3.5.1",
  // "org.msgpack" %% "msgpack-scala" % "0.6.11",  // TODO: do we really want to do this??
  // "org.scala-lang" % "scala-reflect" % "2.11.8",
  // "org.rxtx" % "rxtx" % "2.1.7",  // TODO: this really shouldn't be here, it should instead be from a local.properties file
  "org.slf4j" % "slf4j-log4j12" % "1.7.21"
)

// TODO: move this path into a property somewhere, or perhaps an environmental variable
val rxtx = file("/usr/share/java/rxtx/RXTXcomm.jar")
unmanagedClasspath in Compile += rxtx
unmanagedClasspath in Runtime += rxtx
