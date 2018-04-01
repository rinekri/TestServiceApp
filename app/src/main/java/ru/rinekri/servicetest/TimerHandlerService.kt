package ru.rinekri.servicetest

import android.app.IntentService
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.widget.Toast

//NOTE: Пока выполняется onHandleIntent, сервис не будет убит
class TimerHandlerService : IntentService("TimerRxService") {
  private var uiHandler: Handler? = null

  companion object {
    private const val TAG = "TimerHandlerService"
    const val EXTRA_STOP = "$TAG.stop_service"
  }

  override fun onCreate() {
    super.onCreate()
    Log.d(TAG, "onCreate")
    uiHandler = Handler()
    "STARTED SERVICE: onCreate".showToast()
  }

  override fun onDestroy() {
    super.onDestroy()
    Log.d(TAG, "onDestroy")
    "STARTED SERVICE: onDestroy".showToast()
  }

  override fun onHandleIntent(intent: Intent) {
    Log.d(TAG, "onHandleIntent")

    var second = 0
    while (true) {
      if (second == 0) {
        "STARTED SERVICE: INVOKED".showToast()
      } else {
        "STARTED SERVICE: $second second elapsed".showToast()
      }
      second += 1
      Thread.sleep(1000L)
    }
  }

  private fun String.showToast(length: Int = Toast.LENGTH_SHORT) {
    uiHandler?.post { Toast.makeText(applicationContext, this, length).show() }
  }
}