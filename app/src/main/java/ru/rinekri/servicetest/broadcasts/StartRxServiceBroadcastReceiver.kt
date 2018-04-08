package ru.rinekri.servicetest.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import ru.rinekri.servicetest.BuildConfig
import ru.rinekri.servicetest.services.TimerRxService
import ru.rinekri.servicetest.utils.showToast

/**
 * Если отправить broadcast-intent такому ресиверу из normal-priority FCM сообщения,
 * когда приложение в бэкграунде, выбросится exception
 */
class StartRxServiceBroadcastReceiver : BroadcastReceiver() {
  companion object {
    private const val TAG = "StartRxServiceBroadcastReceiver"
    private const val EXTRA_SAMPLE_DATA = "$TAG.sample_data"
    private const val ACTION_FROM_NOTIFICATION = "${BuildConfig.APPLICATION_ID}.FROM_NOTIFICATION"

    fun newExplicitIntent(context: Context): Intent {
      return Intent(context, StartRxServiceBroadcastReceiver::class.java).apply {
        putExtra(EXTRA_SAMPLE_DATA, "this is sample data")
      }
    }

    fun newImplicitIntent() = Intent(ACTION_FROM_NOTIFICATION)
  }

  override fun onReceive(context: Context, intent: Intent) {
    Log.e(TAG, "onReceive")
    "$TAG onReceive".showToast(context)
    context.startService(TimerRxService.newIntent(context, false))
  }
}