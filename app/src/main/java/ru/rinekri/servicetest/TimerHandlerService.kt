package ru.rinekri.servicetest

import android.app.IntentService
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT

class TimerHandlerService : IntentService(TAG) {
  companion object {
    private const val TAG = "TimerHandlerService"
  }

  private var uiHandler: Handler? = null

  override fun onCreate() {
    super.onCreate()
    Log.e(TAG, "onCreate")
    uiHandler = Handler()
    "$TAG: onCreate".showToast()
  }

  // NOTE: Нужно очищать ресурсы: потоки, ресурсы и т.д.
  // Иначе будут утечки - например, бесконечный цикл будет выполняться дальше.
  override fun onDestroy() {
    super.onDestroy()
    Log.e(TAG, "onDestroy")
    "$TAG: onDestroy".showToast()
  }

  override fun onHandleIntent(intent: Intent) {
    Log.e(TAG, "onHandleIntent")

    var second = 0
    while (true) {
      if (second == 0) {
        "$TAG: invoked"
      } else {
        "$TAG: $second seconds elapsed"
      }.also {
        it.showToast()
        Log.e(TAG, it)
      }
      second += 1
      Thread.sleep(1000L)
    }
  }

  private fun String.showToast(length: Int = LENGTH_SHORT) {
    uiHandler?.post { Toast.makeText(applicationContext, this, length).show() }
  }
}