enablePlugins(AndroidApp)
useSupportVectors

versionCode := Some(1)

instrumentTestRunner :=
  "android.support.test.runner.AndroidJUnitRunner"

platformTarget := "android-25"

javacOptions in Compile ++= "-source" :: "1.7" :: "-target" :: "1.7" :: Nil

resolvers += "Jitpack" at "https://jitpack.io/"

libraryDependencies ++=
  "com.android.support" % "appcompat-v7" % "24.0.0" ::
  "com.android.support.test" % "runner" % "0.5" % "androidTest" ::
  "com.android.support.test.espresso" % "espresso-core" % "2.2.2" % "androidTest" ::
  // "com.github.dunmatt" %% "core" % "0.0.2-SNAPSHOT" ::
  // "com.google.guava" % "guava" % "20.0" ::
  "com.github.felHR85" % "UsbSerial" % "4.5" ::
  "com.squants"  %% "squants"  % "0.6.2" ::
  // "org.apache.jena" % "jena-core" % "3.1.0" ::
  "org.slf4j" % "slf4j-android" % "1.7.21" ::
  "org.zeromq" % "jeromq" % "0.3.5" ::
  // "org.ow2.asm" % "asm" % "5.1" ::
  // "org.ow2.asm" % "asm-commons" % "5.1" ::
  // "org.ow2.asm" % "asm-util" % "5.1" ::
  // // "org.clapper" %% "classutil" % "1.0.13" ::
  // "org.clapper" %% "grizzled-scala" % "4.0.0" ::
  // "org.clapper" %% "grizzled-slf4j" % "1.3.0" ::
  Nil
