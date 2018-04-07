package ru.rinekri.servicetest.services

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import ru.rinekri.servicetest.utils.showToast

class TimerHandlerService : IntentService(TAG) {
  companion object {
    private const val TAG = "TimerHandlerService"
    private const val EXTRA_WITH_LOOP = "$TAG.with_loop"

    fun newIntent(context: Context, withLoop: Boolean): Intent {
      return Intent(context, TimerHandlerService::class.java).apply {
        putExtra(EXTRA_WITH_LOOP, withLoop)
      }
    }
  }

  private var uiHandler: Handler? = null

  override fun onCreate() {
    super.onCreate()
    Log.e(TAG, "onCreate")
    uiHandler = Handler()
    "$TAG: onCreate".showToast(applicationContext)
  }

  // NOTE: Нужно очищать ресурсы: потоки, ресурсы и т.д.
  // Иначе будут утечки - например, бесконечный цикл будет выполняться дальше.
  override fun onDestroy() {
    super.onDestroy()
    Log.e(TAG, "onDestroy")
    "$TAG: onDestroy".showToast(applicationContext)
  }

  override fun onHandleIntent(intent: Intent) {
    Log.e(TAG, "onHandleIntent")

    if (intent.extras.getBoolean(EXTRA_WITH_LOOP)) {
      var second = 0
      while (true) {
        if (second == 0) {
          "$TAG: invoked"
        } else {
          "$TAG: $second seconds elapsed"
        }.also {
          Thread.sleep(2000L)
          uiHandler?.post { it.showToast(applicationContext) }
          Log.e(TAG, it)
        }
        second += 2
      }
    } else {
      uiHandler?.post { "onHandleIntent".showToast(applicationContext) }
    }
  }
}