package ru.rinekri.servicetest.fcm

import android.app.job.JobScheduler
import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ru.rinekri.servicetest.services.TimerRxJobService


class MyFirebaseMessagingService : FirebaseMessagingService() {
  companion object {
    private const val TAG = "MyFirebaseMessagingService"
  }

  override fun onMessageReceived(rm: RemoteMessage) {
    Log.e(TAG.substring(0..23), "onMessageReceived: $rm")
    // NOTE: Если убрать комментарий и отправить из MW или Postman'a пуш с normal-priority сообщением,
    // то приложение упадет с исключением IllegalStateException.
    // Исправляется использованием Job'ов или вызовом startForegroundService
    // startService(TimerRxService.newIntent(applicationContext, true))
    // startForegroundService(TimerRxService.newIntent(applicationContext, true))

    //Один из вариантов решения проблемы с сервисами
    val jobScheduler = applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    jobScheduler.schedule(TimerRxJobService.newJobInfo(applicationContext))
  }
}