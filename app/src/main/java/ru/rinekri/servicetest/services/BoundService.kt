package ru.rinekri.servicetest.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.util.Log
import ru.rinekri.servicetest.utils.showToast

class BoundService : Service() {
  companion object {
    private const val TAG = "BoundService"

    fun newIntent(context: Context) = Intent(context, BoundService::class.java)
  }

  var message: String = "Message from bound service"
  private val binder: BoundServiceBinder = BoundServiceBinder()

  override fun onCreate() {
    Log.e(TAG, "onCreate")
    "$TAG: onCreate".showToast(applicationContext)
  }

  override fun onDestroy() {
    Log.e(TAG, "onDestroy")
    "$TAG: onDestroy".showToast(applicationContext)
  }

  override fun onBind(intent: Intent?): Binder {
    Log.e(TAG, "onBind")
    "$TAG: onBind".showToast(applicationContext)
    return binder
  }

  override fun onUnbind(intent: Intent?): Boolean {
    Log.e(TAG, "onUnbind")
    "$TAG: onUnbind".showToast(applicationContext)
    return false
  }

  inner class BoundServiceBinder : Binder() {
    val service: BoundService
      get() = this@BoundService
  }
}