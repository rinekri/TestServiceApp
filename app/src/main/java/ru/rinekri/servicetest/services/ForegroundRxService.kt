package ru.rinekri.servicetest.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.rinekri.servicetest.utils.NotificationUtils
import ru.rinekri.servicetest.utils.showToast
import java.util.concurrent.TimeUnit

class ForegroundRxService : Service() {
  companion object {
    private const val TAG = "ForegroundRxService"
    private const val TOP_PERIOD = 2L
    private const val FOREGROUND_NOTIFICATION_ID = 1
    private const val EXTRA_START_BACKGROUND_SERVICE = "$TAG.start_background_service"

    fun newIntent(context: Context, startBackgroundService: Boolean = false): Intent {
      return Intent(context, ForegroundRxService::class.java).apply {
        putExtra(EXTRA_START_BACKGROUND_SERVICE, startBackgroundService)
      }
    }
  }

  private var compositeDisposable = CompositeDisposable()

  override fun onCreate() {
    Log.e(TAG, "onCreate")
    "$TAG: onCreate".showToast(applicationContext)
  }

  override fun onDestroy() {
    compositeDisposable.clear()
    Log.e(TAG, "onDestroy")
    "$TAG: onDestroy".showToast(applicationContext)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.e(TAG, "onStartCommand")
    startForeground(FOREGROUND_NOTIFICATION_ID, NotificationUtils.create(this))

    Observable.interval(2, TimeUnit.SECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { period ->
        val msg = if (period == 0L) {
          "$TAG $startId: invoked"
        } else {
          "$TAG $startId: ${period * TOP_PERIOD} seconds elapsed"
        }
        msg.showToast(applicationContext)
        Log.e(TAG, msg)
        if (period == 20L && intent?.extras?.getBoolean(EXTRA_START_BACKGROUND_SERVICE) == true) {
          startService(TimerHandlerService.newIntent(applicationContext, true))
        }
      }
      .also { compositeDisposable.add(it) }
    return START_NOT_STICKY
  }

  override fun onBind(intent: Intent?) = null
}