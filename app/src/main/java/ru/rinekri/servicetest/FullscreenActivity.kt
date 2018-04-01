package ru.rinekri.servicetest

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_SHORT
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_fullscreen.*

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
    private const val AUTO_HIDE_DELAY_MILLIS = 3000

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
    fullscreen_content_controls.visibility = View.VISIBLE
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
    supportActionBar?.setDisplayHomeAsUpEnabled(false)

    activityIsVisible = true

    // Set up the user interaction to manually show or hide the system UI.
    fullscreen_content.setOnClickListener { toggle() }

    val timerServiceIntent = Intent(this, TimerRxService::class.java)
    // Upon interacting with UI controls, delay any scheduled hide()
    // operations to prevent the jarring behavior of controls going away
    // while interacting with the UI.
    start_timer_button.setOnTouchListener(delayHideTouchListener)
    start_timer_button.setOnClickListener {
      startService(timerServiceIntent)
    }
    stop_timer_button.setOnTouchListener(delayHideTouchListener)
    stop_timer_button.setOnClickListener {
      stopService(timerServiceIntent).also {
        Snackbar.make(fullscreen_container, "Service was stopped: $it", LENGTH_SHORT).show()
      }
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    // Trigger the initial hide() shortly after the activity has been
    // created, to briefly hint to the user that UI controls
    // are available.
    delayedHide(100)
  }

  private fun toggle() {
    if (activityIsVisible) {
      hide()
    } else {
      show()
    }
  }

  private fun hide() {
    // Hide UI first
    supportActionBar?.hide()
    fullscreen_content_controls.visibility = View.GONE
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

    // Schedule a runnable to display UI elements after a delay
    hideHandler.removeCallbacks(hidePart2Runnable)
    hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY)
  }

  /**
   * Schedules a call to hide() in [delayMillis], canceling any
   * previously scheduled calls.
   */
  private fun delayedHide(delayMillis: Int) {
    hideHandler.removeCallbacks(hideRunnable)
    hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
  }
}