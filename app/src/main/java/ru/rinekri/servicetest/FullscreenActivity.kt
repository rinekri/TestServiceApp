package ru.rinekri.servicetest

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Process
import android.support.design.widget.BottomSheetDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_fullscreen.*
import kotlinx.android.synthetic.main.layout_started_service.view.*
import ru.rinekri.servicetest.services.ForegroundService
import ru.rinekri.servicetest.services.TimerHandlerService
import ru.rinekri.servicetest.services.TimerRxService
import ru.rinekri.servicetest.utils.showSnack

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {
  companion object {
    private const val TAG = "FullscreenActivity"
    /**
     * Whether or not the system UI should be auto-hidden after
     * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
     */
    private const val AUTO_HIDE = true

    /**
     * If [AUTO_HIDE] is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private const val AUTO_HIDE_DELAY_MILLIS = 3000L

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
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
    // Delayed removal of status and navigation bar
    // Note that some of these constants are new as of API 16 (Jelly Bean)
    // and API 19 (KitKat). It is safe to use them, as they are inlined
    // at compile-time and do nothing on earlier devices.
    fullscreen_content.systemUiVisibility =
      View.SYSTEM_UI_FLAG_LOW_PROFILE or
      View.SYSTEM_UI_FLAG_FULLSCREEN or
      View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
      View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
      View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
      View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
  }
  private val showPart2Runnable = Runnable {
    // Delayed display of UI elements
    supportActionBar?.show()
    fullscreen_manage_started_service_controls.visibility = View.VISIBLE
    fullscreen_kill_process_controls.visibility = View.VISIBLE
  }
  private var activityIsVisible: Boolean = false

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

  private fun destroyForegroundServiceIfNeed() {
    if (intent.hasExtra(EXTRA_CLOSE_FOREGROUND_SERVICE)) {
      stopService(ForegroundService.newIntent(this))
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    // Trigger the initial hide() shortly after the activity has been
    // created, to briefly hint to the user that UI controls
    // are available.
    delayedHide(100)
  }

  //region Services
  private fun initManageStartedServicesViews() {
    fullscreen_manage_started_service_button.setOnClickListener {

      BottomSheetDialog(this).apply dialog@{

        val manageStartedServiceView = layoutInflater.inflate(R.layout.layout_started_service, null).apply dialogView@{

          val rxServiceIntent = TimerRxService.newIntent(context)
          val foregroundServiceIntent = ForegroundService.newIntent(context)

          initRxServiceViews(this, this@dialog, rxServiceIntent)
          initHandlerServiceViews(this, this@dialog)
          initForegroundWrongServiceViews(this, this@dialog, rxServiceIntent)
          initForegroundCorrectServiceViews(this, this@dialog, foregroundServiceIntent)
        }
        setContentView(manageStartedServiceView)
      }.show()
    }
  }

  private fun initRxServiceViews(view: View, bottomSheetDialog: BottomSheetDialog, rxServiceIntent: Intent) {
    view.fullscreen_start_rx_service_button.setOnClickListener {
      bottomSheetDialog.dismiss()
      startService(rxServiceIntent)
    }
    view.fullscreen_stop_rx_service_button.setOnClickListener {
      stopService(rxServiceIntent).also {
        bottomSheetDialog.dismiss()
        "Rx service was stopped: $it".showSnack(this@FullscreenActivity.fullscreen_container)
      }
    }
  }

  private fun initHandlerServiceViews(view: View, bottomSheetDialog: BottomSheetDialog) {
    val handlerWithLoopServiceIntent = TimerHandlerService.newIntent(this, true)
    val handlerNormalServiceIntent = TimerHandlerService.newIntent(this, false)

    view.fullscreen_start_handler_service_with_loop_button.setOnClickListener {
      bottomSheetDialog.dismiss()
      startService(handlerWithLoopServiceIntent)
    }
    view.fullscreen_start_handler_service_normal_button.setOnClickListener {
      bottomSheetDialog.dismiss()
      startService(handlerNormalServiceIntent)
    }
    view.fullscreen_stop_handler_service_button.setOnClickListener {
      stopService(handlerWithLoopServiceIntent).also {
        bottomSheetDialog.dismiss()
        "Handler service was stopped: $it".showSnack(this@FullscreenActivity.fullscreen_container)
      }
    }
  }

  private fun initForegroundWrongServiceViews(view: View, bottomSheetDialog: BottomSheetDialog, rxServiceIntent: Intent) {
    view.fullscreen_start_foreground_wrong_service_button.setOnClickListener {
      bottomSheetDialog.dismiss()
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(rxServiceIntent)
      } else {
        startService(rxServiceIntent)
      }
    }
    view.fullscreen_stop_foreground_wrong_service_button.setOnClickListener {
      stopService(rxServiceIntent).also {
        bottomSheetDialog.dismiss()
        "Foreground wrong rx service was stopped: $it".showSnack(this@FullscreenActivity.fullscreen_container)
      }
    }
  }

  private fun initForegroundCorrectServiceViews(view: View, bottomSheetDialog: BottomSheetDialog, foregroundServiceIntent: Intent) {
    view.fullscreen_start_foreground_correct_service_button.setOnClickListener {
      bottomSheetDialog.dismiss()
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(foregroundServiceIntent)
      } else {
        startService(foregroundServiceIntent)
      }
    }
    view.fullscreen_stop_foreground_correct_service_button.setOnClickListener {
      stopService(foregroundServiceIntent).also {
        bottomSheetDialog.dismiss()
        "Handler service was stopped: $it".showSnack(this@FullscreenActivity.fullscreen_container)
      }
    }
  }

  private fun initManageBoundServicesViews() {
    fullscreen_manage_scheduled_service_button.setOnClickListener {
      "TODO: Show scheduled services manager".showSnack(fullscreen_container)
    }
  }

  private fun initManageScheduledServicesViews() {
    fullscreen_manage_bound_service_button.setOnClickListener {
      "TODO: Show bound services manager".showSnack(fullscreen_container)
    }
  }

  private fun initContentViews() {
    supportActionBar?.setDisplayHomeAsUpEnabled(false)
    activityIsVisible = true
    fullscreen_content.setOnClickListener { toggleControls() }
  }

  private fun initKillProcessViews() {
    fullscreen_kill_process_button.setOnClickListener {
      Process.killProcess(Process.myPid())
    }
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
    fullscreen_manage_started_service_controls.visibility = View.GONE
    fullscreen_kill_process_controls.visibility = View.GONE
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