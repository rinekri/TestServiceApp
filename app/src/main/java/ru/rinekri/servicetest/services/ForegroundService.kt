package ru.rinekri.servicetest.services

import android.app.Service
import android.content.Intent
import android.util.Log
import ru.rinekri.servicetest.utils.NotificationUtils
import ru.rinekri.servicetest.utils.showToast

class ForegroundService : Service() {
  companion object {
    private const val TAG = "ForegroundService"
    private const val FOREGROUND_NOTIFICATION_ID = 1
  }

  override fun onCreate() {
    Log.e(TAG, "onCreate")
    "$TAG: created".showToast(applicationContext)
  }

  override fun onDestroy() {
    Log.e(TAG, "onDestroy")
    "$TAG: destroyed".showToast(applicationContext)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.e(TAG, "onStartCommand")
    startForeground(FOREGROUND_NOTIFICATION_ID, NotificationUtils.create(this))
    return START_NOT_STICKY
  }

  override fun onBind(intent: Intent?) = null
}