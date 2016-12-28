package com.github.dunmatt.obo.android.core

import android.app.{ Activity, Notification, NotificationManager, PendingIntent }
import android.content.{ ComponentName, Context, Intent }
import android.support.v4.app.TaskStackBuilder
import com.github.dunmatt.obo.core.Component
import com.github.dunmatt.obo.android.components.R

trait AndroidComponent extends Component {
  var context: Context = null  // this will be populated by the component runner before the component is started
  val uiActivityClass: Option[Class[_ <: Activity]] = None

  def addUiNotification: Unit = uiActivityClass.foreach { cls =>
    val stackBuilder = TaskStackBuilder.create(context)
    stackBuilder.addParentStack(cls)
    stackBuilder.addNextIntent(new Intent(context, cls))
    val pending = stackBuilder.getPendingIntent(shortId, PendingIntent.FLAG_UPDATE_CURRENT)
    val notification = new Notification.Builder(context)
                                       .setContentTitle(name)
                                       .setContentText("Go to the component UI")
                                       .setSmallIcon(R.drawable.scala_android)
                                       .setContentIntent(pending)
                                       .build
    Option(context.getSystemService(Context.NOTIFICATION_SERVICE)) match {
      case Some(mgr: NotificationManager) =>
        mgr.notify(shortId, notification)
      case Some(_) => log.error("This should be impossible, getSystemService returned a different type than was requested")
      case None => log.error("Couldn't get the notification service... are you running an ancient version of android?")
    }
  }

  override def onHalt: Unit = {
    super.onHalt
    log.info("Stopping UI Notification")
    Option(context.getSystemService(Context.NOTIFICATION_SERVICE)) match {
      case Some(mgr: NotificationManager) if uiActivityClass.nonEmpty => mgr.cancel(shortId)
      case _ => Unit
    }
  }
}
