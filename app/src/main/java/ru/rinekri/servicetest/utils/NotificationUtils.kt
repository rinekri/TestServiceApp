package ru.rinekri.servicetest.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import ru.rinekri.servicetest.R

object NotificationUtils {
  private const val CHANNEL_ID = "ServiceTestNotificationChannelId"

  @SuppressLint("NewApi")
  fun create(context: Service): Notification {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val primaryChannel = NotificationChannel(CHANNEL_ID,
        "Title",
        NotificationManager.IMPORTANCE_HIGH).apply {
        lightColor = ContextCompat.getColor(context, R.color.colorOrangeDark)
        description = "Description"
      }
      context.notificationManager().createNotificationChannel(primaryChannel)
    }

    return NotificationCompat.Builder(context, CHANNEL_ID)
      .setContentTitle("Service Test title")
      .setContentText("Service Test text")
      .setSmallIcon(R.mipmap.ic_launcher)
      .setAutoCancel(true)
      .setStyle(NotificationCompat.BigTextStyle())
      .build()
  }

  private fun Context.notificationManager() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}