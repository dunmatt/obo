package com.github.dunmatt.obo.android.core

import android.app.{ Activity, Notification, NotificationManager, PendingIntent }
import android.content.{ Context, Intent }
import android.support.v4.app.TaskStackBuilder
import com.github.dunmatt.obo.core.Component
import com.github.dunmatt.obo.android.components.R

trait AndroidComponent extends Component {
  var context: Context = null  // this will be populated by the component runner before the component is started
  def uiActivityClass: Option[Class[_ <: Activity]] = None

  def addUiNotification: Unit = uiActivityClass.foreach { cls =>
    // val stackBuilder = TaskStackBuilder.create(context)
    // cls.newInstance
    // log.warn(s"$cls")
    // stackBuilder.addParentStack(cls)
    // stackBuilder.addNextIntent(new Intent(context, cls))
    // val pending = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    // // TODO: add a "halt component" action to the dismiss swipe
    val goToUi = new Intent(context, cls)
    val pending = PendingIntent.getActivity(context, 0, goToUi, 0)
    val notification = new Notification.Builder(context)
                                       .setContentTitle(name)
                                       .setContentText("Go to the component UI")
                                       .setSmallIcon(R.drawable.scala_android)
                                       // .setCategory(Notification.CATEGORY_SERVICE)
                                       .setContentIntent(pending)
                                       .build
    Option(context.getSystemService(Context.NOTIFICATION_SERVICE)) match {
      case Some(mgr: NotificationManager) =>
        mgr.notify(instanceId.getLeastSignificantBits.toInt, notification)
      case Some(_) => log.error("This should be impossible, getSystemService returned a different type than was requested")
      case None => log.error("Couldn't get the notification service... are you running an ancient version of android?")
    }
  }
}
