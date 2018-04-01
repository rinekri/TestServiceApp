package ru.rinekri.servicetest

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class TimerRxService : Service() {
  companion object {
    private const val TAG = "TimerRxService"
    const val EXTRA_STOP = "$TAG.stop_service"
  }

  private var compositeDisposable = CompositeDisposable()

  override fun onCreate() {
    super.onCreate()
    Log.d(TAG, "onCreate")
    "STARTED SERVICE: created".showToast()
  }

  override fun onDestroy() {
    compositeDisposable.clear()
    super.onDestroy()
    Log.d(TAG, "onDestroy")
    "STARTED SERVICE: destroyed".showToast()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.d(TAG, "onStartCommand")

    Observable.interval(1, TimeUnit.SECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { second ->
        if (second == 0L) {
          "STARTED SERVICE: INVOKED".showToast()
        } else {
          "STARTED SERVICE: $second second elapsed".showToast()
        }
      }
      .also { compositeDisposable.add(it) }
    return START_NOT_STICKY
  }

  override fun onBind(intent: Intent?): IBinder {
    throw UnsupportedOperationException("not implemented")
  }

  private fun String.showToast(length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(applicationContext, this, length).show()
  }
}