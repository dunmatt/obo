lazy val commonSettings = Seq( organization := "com.github.dunmatt",
                               version := "0.0.1",
                               scalaVersion := "2.11.8")

resolvers += Resolver.mavenLocal

val managedDependencies = Seq(
  "com.squants" %% "squants" % "0.6.2",
  "org.apache.jena" % "jena-core" % "3.1.0",
  "org.jmdns" % "jmdns" % "3.5.1",
  "org.msgpack" %% "msgpack-scala" % "0.6.11",
  // "org.scala-lang" % "scala-reflect" % "2.11.8",
  "org.slf4j" % "slf4j-api" % "1.7.21",
  "org.slf4j" % "slf4j-log4j12" % "1.7.21",
  "org.zeromq" % "jeromq" % "0.3.5"
)

lazy val root = (project in file(".")).settings( commonSettings: _*)
                                      .settings(
  name := "Obo Core"
  , libraryDependencies ++= managedDependencies
)
