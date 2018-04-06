package ru.rinekri.servicetest.utils

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import ru.rinekri.servicetest.FullscreenActivity
import ru.rinekri.servicetest.R

object NotificationUtils {
  private const val CHANNEL_ID = "ServiceTestNotificationChannelId"

  fun create(context: Service): Notification {
    createNotificationChannel(context)
    val startActivityIntent = FullscreenActivity.newIntent(context, true)
    return NotificationCompat.Builder(context, CHANNEL_ID)
      .setContentTitle("Service Test title")
      .setContentText("Service Test text")
      .setAutoCancel(true)
      .setSmallIcon(R.drawable.ic_copyright_24dp)
      .setStyle(NotificationCompat.BigTextStyle())
      .addAction(R.drawable.ic_copyright_24dp, "Закрыть", startActivityIntent.toPendingIntent(context))
      .build()
  }

  @SuppressLint("NewApi")
  private fun createNotificationChannel(context: Service) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Title",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        lightColor = ContextCompat.getColor(context, R.color.colorOrangeDark)
        description = "Description"
      }
      context.notificationManager().createNotificationChannel(channel)
    }
  }

  private fun Intent.toPendingIntent(context: Service): PendingIntent {
    return PendingIntent.getActivity(context, 0, this, PendingIntent.FLAG_UPDATE_CURRENT)
  }

  private fun Context.notificationManager(): NotificationManager {
    return getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  }
}