package ru.rinekri.servicetest

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Rect
import android.os.*
import android.support.design.widget.BottomSheetDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum
import com.nightonke.boommenu.BoomButtons.HamButton
import com.nightonke.boommenu.Piece.PiecePlaceEnum
import kotlinx.android.synthetic.main.activity_fullscreen.*
import kotlinx.android.synthetic.main.layout_bound_service.view.*
import kotlinx.android.synthetic.main.layout_started_service.view.*
import ru.rinekri.servicetest.services.BoundService
import ru.rinekri.servicetest.services.ForegroundRxService
import ru.rinekri.servicetest.services.TimerHandlerService
import ru.rinekri.servicetest.services.TimerRxService
import ru.rinekri.servicetest.utils.showSnack
import ru.rinekri.servicetest.utils.showToast

class FullscreenActivity : AppCompatActivity() {
  companion object {
    private const val TAG = "FullscreenActivity"
    private const val UI_ANIMATION_DELAY = 300L
    private const val EXTRA_CLOSE_FOREGROUND_SERVICE = "$TAG.close_foreground_service"

    fun newIntent(context: Context, closeForegroundService: Boolean): Intent {
      return Intent(context, FullscreenActivity::class.java).apply {
        putExtra(EXTRA_CLOSE_FOREGROUND_SERVICE, closeForegroundService)
      }
    }
  }

  private val hideHandler = Handler()
  private val hideRunnable = Runnable { hide() }
  private val hidePart2Runnable = Runnable {
    fullscreen_content.systemUiVisibility =
      View.SYSTEM_UI_FLAG_LOW_PROFILE or
      View.SYSTEM_UI_FLAG_FULLSCREEN or
      View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
      View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
      View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
      View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
  }
  private val showPart2Runnable = Runnable {
    supportActionBar?.show()
    fullscreen_fab.visibility = View.VISIBLE
  }
  private var activityIsVisible: Boolean = false
  private var boundService: BoundService? = null
  private var stratedServiceInvoked: Boolean = false

  private val serviceConnection = object : ServiceConnection {
    override fun onServiceConnected(cName: ComponentName, service: IBinder) {
      val binder = service as BoundService.BoundServiceBinder
      boundService = binder.service
      "onBoundServiceConnected".showToast(this@FullscreenActivity)
    }

    override fun onServiceDisconnected(cName: ComponentName) {
      boundService = null
      "onBoundServiceDisconnected".showToast(this@FullscreenActivity)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_fullscreen)
    initContentViews()
    initManageStartedServicesViews()
    initManageBoundServicesViews()
    initManageScheduledServicesViews()
    initKillProcessViews()
    destroyForegroundServiceIfNeed()
  }

  override fun onStart() {
    super.onStart()
    bindBoundService()
    "$TAG onStart".showToast(this)
  }

  override fun onStop() {
    super.onStop()
    unbindBoundService()
    if (!stratedServiceInvoked) {
      "$TAG onStop".showToast(this)
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    delayedHide(100)
  }

  //region Services
  private fun initManageStartedServicesViews() {
    HamButton.Builder()
      .normalTextRes(R.string.manage_started_service_title)
      .normalImageRes(R.drawable.ic_add_alert_24dp)
      .imagePadding(Rect(40, 40, 40, 40))
      .listener {
        BottomSheetDialog(this).apply dialog@{

          val manageStartedServiceView = layoutInflater.inflate(R.layout.layout_started_service, null).apply dialogView@{

            val rxServiceIntent = TimerRxService.newIntent(context)
            val foregroundServiceIntent = ForegroundRxService.newIntent(context)

            initRxServiceViews(this, this@dialog, rxServiceIntent)
            initHandlerServiceViews(this, this@dialog)
            initForegroundWrongServiceViews(this, this@dialog, rxServiceIntent)
            initForegroundCorrectServiceViews(this, this@dialog, foregroundServiceIntent)
          }
          setContentView(manageStartedServiceView)
        }.show()
      }
      .apply {
        fullscreen_fab.addBuilder(this)
      }
  }

  private fun initRxServiceViews(view: View, bottomSheetDialog: BottomSheetDialog, rxServiceIntent: Intent) {
    view.start_rx_service_button.setOnClickListener {
      bottomSheetDialog.dismiss()
      startService(rxServiceIntent)
      stratedServiceInvoked = true
    }
    view.stop_rx_service_button.setOnClickListener {
      stopService(rxServiceIntent).also {
        bottomSheetDialog.dismiss()
        "Rx service was stopped: $it".showSnack(this@FullscreenActivity.fullscreen_container)
        stratedServiceInvoked = false
      }
    }
    view.start_background_wrong_service.setOnClickListener {
      val wrongRxServiceIntent = TimerRxService.newIntent(this, true)
      bottomSheetDialog.dismiss()
      startService(wrongRxServiceIntent)
      stratedServiceInvoked = true
    }
  }

  private fun initHandlerServiceViews(view: View, bottomSheetDialog: BottomSheetDialog) {
    val handlerWithLoopServiceIntent = TimerHandlerService.newIntent(this, true)
    val handlerNormalServiceIntent = TimerHandlerService.newIntent(this, false)

    view.start_handler_service_with_loop_button.setOnClickListener {
      bottomSheetDialog.dismiss()
      startService(handlerWithLoopServiceIntent)
      stratedServiceInvoked = true
    }
    view.start_handler_service_normal_button.setOnClickListener {
      bottomSheetDialog.dismiss()
      startService(handlerNormalServiceIntent)
      stratedServiceInvoked = true
    }
    view.stop_handler_service_button.setOnClickListener {
      stopService(handlerWithLoopServiceIntent).also {
        bottomSheetDialog.dismiss()
        "Handler service was stopped: $it".showSnack(this@FullscreenActivity.fullscreen_container)
        stratedServiceInvoked = true
      }
    }
  }

  private fun initForegroundWrongServiceViews(view: View, bottomSheetDialog: BottomSheetDialog, rxServiceIntent: Intent) {
    view.start_foreground_wrong_service_button.setOnClickListener {
      bottomSheetDialog.dismiss()
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(rxServiceIntent)
      } else {
        startService(rxServiceIntent)
      }
      stratedServiceInvoked = true
    }
    view.stop_foreground_wrong_service_button.setOnClickListener {
      stopService(rxServiceIntent).also {
        bottomSheetDialog.dismiss()
        "Foreground wrong rx service was stopped: $it".showSnack(this@FullscreenActivity.fullscreen_container)
        stratedServiceInvoked = false
      }
    }
  }

  private fun initForegroundCorrectServiceViews(view: View, bottomSheetDialog: BottomSheetDialog, foregroundServiceIntent: Intent) {
    view.start_foreground_correct_service_button.setOnClickListener {
      bottomSheetDialog.dismiss()
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(foregroundServiceIntent)
      } else {
        startService(foregroundServiceIntent)
      }
    }
    view.stop_foreground_correct_service_button.setOnClickListener {
      stopService(foregroundServiceIntent).also {
        bottomSheetDialog.dismiss()
        "Handler service was stopped: $it".showSnack(this@FullscreenActivity.fullscreen_container)
        stratedServiceInvoked = false
      }
    }
  }

  private fun initManageBoundServicesViews() {
    HamButton.Builder()
      .normalTextRes(R.string.manage_bound_service_title)
      .normalImageRes(R.drawable.ic_broken_image_24dp)
      .imagePadding(Rect(40, 40, 40, 40))
      .listener {
        BottomSheetDialog(this).apply dialog@{
          val manageStartedServiceView = layoutInflater.inflate(R.layout.layout_bound_service, null).apply dialogView@{
            bind_bound_service_button.setOnClickListener {
              this@dialog.dismiss()
              bindBoundService()
            }
            unbind_bound_service_button.setOnClickListener {
              this@dialog.dismiss()
              unbindBoundService()
            }
            show_message_from_bound_service_button.setOnClickListener {
              this@dialog.dismiss()
              boundService?.message?.showSnack(this@FullscreenActivity.fullscreen_container)
                ?: "Problem with showing message".showToast(this@FullscreenActivity)
            }
          }
          setContentView(manageStartedServiceView)
        }.show()
      }
      .apply {
        fullscreen_fab.addBuilder(this)
      }
  }

  private fun initManageScheduledServicesViews() {
    HamButton.Builder()
      .normalTextRes(R.string.manage_scheduled_service_title)
      .normalImageRes(R.drawable.ic_add_alarm_24dp)
      .imagePadding(Rect(40, 40, 40, 40))
      .listener {
        "TODO: Show Scheduled services manager".showSnack(fullscreen_container)
      }
      .apply {
        fullscreen_fab.addBuilder(this)
      }
  }

  private fun initContentViews() {
    supportActionBar?.setDisplayHomeAsUpEnabled(false)
    activityIsVisible = true
    fullscreen_content.setOnClickListener { toggleControls() }
    fullscreen_fab.piecePlaceEnum = PiecePlaceEnum.HAM_4
    fullscreen_fab.buttonPlaceEnum = ButtonPlaceEnum.HAM_4
  }

  private fun initKillProcessViews() {
    HamButton.Builder()
      .normalTextRes(R.string.kill_process_button)
      .normalImageRes(R.drawable.ic_delete_24dp)
      .imagePadding(Rect(40, 40, 40, 40))
      .listener {
        Process.killProcess(Process.myPid())
      }
      .apply {
        fullscreen_fab.addBuilder(this)
      }
  }

  private fun destroyForegroundServiceIfNeed() {
    if (intent.hasExtra(EXTRA_CLOSE_FOREGROUND_SERVICE)) {
      stopService(ForegroundRxService.newIntent(this))
    }
  }

  private fun bindBoundService() {
    bindService(BoundService.newIntent(this), serviceConnection, BIND_AUTO_CREATE)
  }

  private fun unbindBoundService() {
    boundService?.let { unbindService(serviceConnection); boundService = null }
  }
  //endregion

  //region Fullscreen
  private fun toggleControls() {
    if (activityIsVisible) {
      hide()
    } else {
      show()
    }
  }

  private fun hide() {
    // Hide UI first
    supportActionBar?.hide()
    fullscreen_fab.visibility = View.GONE
    activityIsVisible = false

    // Schedule a runnable to remove the status and navigation bar after a delay
    hideHandler.removeCallbacks(showPart2Runnable)
    hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY)
  }

  private fun show() {
    // Show the system bar
    fullscreen_content.systemUiVisibility =
      View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
      View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    activityIsVisible = true

    hideHandler.removeCallbacks(hidePart2Runnable)
    hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY)
  }

  /**
   * Schedules a call to hide() in [delayMillis], canceling any
   * previously scheduled calls.
   */
  private fun delayedHide(delayMillis: Long) {
    hideHandler.removeCallbacks(hideRunnable)
    hideHandler.postDelayed(hideRunnable, delayMillis)
  }
  //endregion
}