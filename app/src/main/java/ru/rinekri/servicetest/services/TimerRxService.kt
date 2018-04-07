package ru.rinekri.servicetest.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.rinekri.servicetest.utils.showToast
import java.util.concurrent.TimeUnit

class TimerRxService : Service() {
  companion object {
    private const val TAG = "TimerRxService"
    private const val TOP_PERIOD = 2L

    fun newIntent(context: Context) = Intent(context, TimerRxService::class.java)
  }

  private var compositeDisposable = CompositeDisposable()

  override fun onCreate() {
    Log.e(TAG, "onCreate")
    "$TAG: onCreate".showToast(applicationContext)
  }

  //NOTE: Нужно очищать ресурсы: потоки, ресурсы и т.д.
  override fun onDestroy() {
    compositeDisposable.clear()
    Log.e(TAG, "onDestroy")
    "$TAG: onDestroy".showToast(applicationContext)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.e(TAG, "onStartCommand")

    Observable.interval(TOP_PERIOD, TimeUnit.SECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { period ->
        val msg = if (period == 0L) {
          "$TAG $startId: invoked"
        } else {
          "$TAG $startId: ${period * TOP_PERIOD} seconds elapsed"
        }
        msg.showToast(applicationContext)
        Log.e(TAG, msg)
      }
      .also { compositeDisposable.add(it) }
    return START_NOT_STICKY
  }

  override fun onBind(intent: Intent?) = null
}