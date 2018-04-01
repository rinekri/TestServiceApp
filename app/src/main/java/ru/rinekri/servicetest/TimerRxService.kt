package ru.rinekri.servicetest

import android.app.Service
import android.content.Intent
import android.util.Log
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class TimerRxService : Service() {
  companion object {
    private const val TAG = "TimerRxService"
  }

  private var compositeDisposable = CompositeDisposable()

  override fun onCreate() {
    super.onCreate()
    Log.e(TAG, "onCreate")
    "$TAG: created".showToast()
  }

  //NOTE: Нужно очищать ресурсы: потоки, ресурсы и т.д.
  override fun onDestroy() {
    compositeDisposable.clear()
    super.onDestroy()
    Log.e(TAG, "onDestroy")
    "$TAG: destroyed".showToast()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.e(TAG, "onStartCommand")

    Observable.interval(1, TimeUnit.SECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { second ->
        val msg = if (second == 0L) {
          "$TAG $startId: invoked"
        } else {
          "$TAG $startId: $second seconds elapsed"
        }
        msg.showToast()
        Log.e(TAG, msg)
      }
      .also { compositeDisposable.add(it) }
    return START_NOT_STICKY
  }

  override fun onBind(intent: Intent?) = null

  private fun String.showToast(length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(applicationContext, this, length).show()
  }
}