package com.github.dunmatt.obo.android.entrypoint

import android.app.{ Notification, PendingIntent, Service }
import android.content.{ Context, Intent }
import android.os.IBinder
import com.github.dunmatt.obo.core.Constants
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._

class AndroidComponentService extends Service {
  implicit val context: Context = this
  protected val log = LoggerFactory.getLogger(getClass)
  protected val activeRunners = new ConcurrentHashMap[String, AndroidComponentRunner]

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = {
    Option(intent).foreach { i =>
      val name = i.getStringExtra(Constants.COMPONENT_NAME_KEY)
      log.info(s"Launching a $name...")

      new Thread(new Runnable {
        override def run: Unit = {
          val runner = new AndroidComponentRunner(name)
          activeRunners.put(name, runner)
          runner.mainLoop
          log.info("Done with main loop")
        }
      }, name.substring(name.lastIndexOf('.') + 1)).start
    }
    Service.START_REDELIVER_INTENT
  }

  // TODO: are we sure we want to reject bind requests?
  override def onBind(intent: Intent) = null  // this null tells Android to reject bind requests

  override def onCreate: Unit = {
    promoteServiceToForeground
  }

  override def onDestroy: Unit = {
    log.info("Stopping all component runners...")
    activeRunners.values.foreach(_.stop)
    activeRunners.clear
  }

  protected def promoteServiceToForeground: Unit = {
    val returnToMain = new Intent(context, classOf[MainActivity])
    val pending = PendingIntent.getActivity(context, 0, returnToMain, 0)
    val notification = new Notification.Builder(context)
                                       .setContentTitle("Obo Component Launcher")
                                       .setContentText("Touch me to launch more components.")
                                       .setSmallIcon(R.drawable.scala_android)
                                       .setCategory(Notification.CATEGORY_SERVICE)
                                       .setContentIntent(pending)
                                       .build
    // TODO: figure out what that the int (5, below) is for and replace it with something useful
    startForeground(5, notification)
  }
}

