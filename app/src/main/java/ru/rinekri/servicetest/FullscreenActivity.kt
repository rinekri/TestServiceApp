package ru.rinekri.servicetest

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Process
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_SHORT
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_fullscreen.*
import kotlinx.android.synthetic.main.layout_started_service.view.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {
  companion object {
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
  /**
   * Touch listener to use for in-layout UI controls to delay hiding the
   * system UI. This is to prevent the jarring behavior of controls going away
   * while interacting with activity UI.
   */
  private val delayHideTouchListener = View.OnTouchListener { _, _ ->
    if (AUTO_HIDE) {
      delayedHide(AUTO_HIDE_DELAY_MILLIS)
    }
    false
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_fullscreen)
    initContentViews()
    initManageStartedServicesViews()
    initKillProcessViews()
  }

  private fun initManageStartedServicesViews() {
    fullscreen_manage_started_service_button.setOnTouchListener(delayHideTouchListener)
    fullscreen_manage_started_service_button.setOnClickListener {
      BottomSheetDialog(this).apply {
        val manageStartedServiceView = layoutInflater.inflate(R.layout.layout_started_service, null).apply {

          val rxServiceIntent = Intent(this@FullscreenActivity, TimerRxService::class.java)
          fullscreen_start_rx_service_button.setOnClickListener { startService(rxServiceIntent) }
          fullscreen_stop_rx_service_button.setOnClickListener {
            stopService(rxServiceIntent).also {
              Snackbar.make(this, "Rx service was stopped: $it", LENGTH_SHORT).show()
            }
          }

          val handlerServiceIntent = Intent(this@FullscreenActivity, TimerHandlerService::class.java)

          fullscreen_start_handler_service_button.setOnClickListener { startService(handlerServiceIntent) }
          fullscreen_stop_handler_service_button.setOnClickListener {
            stopService(handlerServiceIntent).also {
              Snackbar.make(this, "Handler service was stopped: $it", LENGTH_SHORT).show()
            }
          }

          fullscreen_start_foreground_wrong_service_button.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              startForegroundService(rxServiceIntent)
            } else {
              startService(rxServiceIntent)
            }
          }
          fullscreen_stop_foreground_wrong_service_button.setOnClickListener {
            stopService(rxServiceIntent).also {
              Snackbar.make(this, "Foreground rx service was stopped: $it", LENGTH_SHORT).show()
            }
          }
        }
        setContentView(manageStartedServiceView)
      }.show()
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    // Trigger the initial hide() shortly after the activity has been
    // created, to briefly hint to the user that UI controls
    // are available.
    delayedHide(100)
  }

  private fun initContentViews() {
    supportActionBar?.setDisplayHomeAsUpEnabled(false)
    activityIsVisible = true
    fullscreen_content.setOnClickListener { toggleControls() }
  }

  private fun initKillProcessViews() {
    fullscreen_kill_process_button.setOnTouchListener(delayHideTouchListener)
    fullscreen_kill_process_button.setOnClickListener {
      Process.killProcess(Process.myPid())
    }
  }

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
}