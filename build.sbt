import com.eny.plugin.spi.Imports.{ mapExport, SpiKeys }

lazy val commonSettings = Seq( organization := "com.github.dunmatt"
                             , platformTarget := "android-25"
                             , version := "0.0.2-SNAPSHOT"
                             , scalaVersion := "2.11.8"
                             , SpiKeys.spiPaths := Nil
                             , SpiKeys.traits := Seq("com.github.dunmatt.obo.core.Component")
                             )

lazy val root = (project in file("."))
  .aggregate(core, androidEntryPoint, jvmEntryPoint)
  .settings(commonSettings: _*)  // these settings are to keep SpiPlugin from doing every interface

lazy val utils = (project in file("utils"))
  .settings(commonSettings: _*)
  .settings(exportJars := true)

lazy val messages = (project in file("messages"))
  .dependsOn(utils)
  .settings(commonSettings: _*)
  .settings(exportJars := true)

lazy val core = (project in file("core"))
  .dependsOn(messages, utils)
  .settings(commonSettings: _*)
  .settings(exportJars := true)

lazy val components = (project in file("components"))
  .dependsOn(core, messages, utils)
  .settings(commonSettings: _*)
  // .settings(android.Plugin.androidBuildAar: _*)
  .settings(exportJars := true)

lazy val androidComponents = (project in file("android-components"))
  .dependsOn(core, messages, utils)
  .enablePlugins(AndroidLib)
  .settings(commonSettings: _*)
  .settings(exportJars := true)

lazy val jvmComponents = (project in file("jvm-components"))
  .dependsOn(core, messages, utils)
  .settings(commonSettings: _*)
  .settings(exportJars := true)

lazy val androidEntryPoint = (project in file("android-entry-point"))
  .dependsOn(core, components, androidComponents, utils)
  .enablePlugins(AndroidApp, SpiPlugin)
  .settings(commonSettings: _*)
  .settings(proguardOptions in Android += "-keep class * extends com.github.dunmatt.obo.core.Component" )  // this from http://scala-on-android.taig.io/proguard/
  .settings(resourceGenerators in Compile += Def.task{
    // This task copies the list of Components to the appropriate place to ensure
    // it gets included in an accessible place in the APK
    val res = collectResources.value._1  // item _1 here is for assets, item _2 is for resources.  See the output of sbt "show androidEntryPoint/android:collectResources"
    mapExport.value.toSeq.map { name =>
      IO.move(target.value / name, res / name)
      res / name
    }
  }.taskValue)

// TODO: this currently depends on rxtx from maven central, it should probably instead depend on a local property
lazy val jvmEntryPoint = (project in file("jvm-entry-point"))
  .dependsOn(core, components, jvmComponents, utils)
  .settings(commonSettings: _*)
