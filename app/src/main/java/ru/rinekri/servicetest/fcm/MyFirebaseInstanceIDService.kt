package ru.rinekri.servicetest.fcm

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceIdService
import ru.rinekri.servicetest.services.TimerRxService

class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {
  companion object {
    private const val TAG = "MyFirebaseInstanceIDService"
  }

  override fun onTokenRefresh() {
    Log.e(TAG, "onTokenRefresh")
    startService(TimerRxService.newIntent(applicationContext, true))
  }
}