package ru.rinekri.servicetest.services

import android.app.job.JobInfo
import android.app.job.JobInfo.NETWORK_TYPE_NONE
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.rinekri.servicetest.utils.showToast
import java.util.concurrent.TimeUnit


class TimerRxJobService : JobService() {
  companion object {
    private const val TAG = "TimerRxJobService"
    private const val TOP_PERIOD = 2L

    fun newJobInfo(context: Context): JobInfo {
      return JobInfo.Builder(1, ComponentName(context, TimerRxJobService::class.java))
        .setMinimumLatency(1000)   // Wait at least 1 second
        .setOverrideDeadline(5000) // But no longer than 5 seconds
        .setRequiredNetworkType(NETWORK_TYPE_NONE)
        .build()
    }
  }

  private var compositeDisposable = CompositeDisposable()

  override fun onCreate() {
    Log.e(TAG, "onCreate")
    "$TAG: onCreate".showToast(applicationContext)
  }

  override fun onDestroy() {
    Log.e(TAG, "onDestroy")
    compositeDisposable.clear()
    "$TAG: onDestroy".showToast(applicationContext)
  }

  override fun onStartJob(params: JobParameters): Boolean {
    "$TAG: onStartJob".showToast(applicationContext)

    Observable.interval(TOP_PERIOD, TimeUnit.SECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { period ->
        val seconds = period * TOP_PERIOD
        val msg = if (seconds == 0L) {
          "$TAG ${params.jobId}: invoked"
        } else {
          //NOIE: Важно сообщить об окончании работы
          if (seconds == 10L) jobFinished(params, false)
          "$TAG ${params.jobId}: $seconds seconds elapsed"
        }
        msg.showToast(applicationContext)
        Log.e(TAG, msg)
      }
      .also { compositeDisposable.add(it) }
    return true
  }

  override fun onStopJob(params: JobParameters): Boolean {
    compositeDisposable.clear()
    "$TAG: onStopJob".showToast(applicationContext)
    return true
  }
}