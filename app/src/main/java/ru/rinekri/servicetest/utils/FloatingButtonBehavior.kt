package ru.rinekri.servicetest.utils

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.util.AttributeSet
import android.view.View

class FloatingButtonBehavior : CoordinatorLayout.Behavior<View> {

  constructor() : super()
  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

  companion object {
    private val FAST_OUT_SLOW_IN_INTERPOLATOR = FastOutLinearInInterpolator()
  }

  override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
    if (dependency is Snackbar.SnackbarLayout) {
      updateFabTranslationForSnackbar(child, dependency)
    }
    return false
  }

  private fun updateFabTranslationForSnackbar(child: View, dependency: View) {
    val translationY = Math.min(0F, dependency.translationY - dependency.height)
    child.translationY = translationY
  }

  override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
    return dependency is Snackbar.SnackbarLayout
  }

  override fun onDependentViewRemoved(parent: CoordinatorLayout, child: View, dependency: View) {
    if (dependency is Snackbar.SnackbarLayout && child.translationY != 0.0f) {
      ViewCompat.animate(child)
        .translationY(0.0f)
        .scaleX(1.0f)
        .scaleY(1.0f)
        .alpha(1.0f)
        .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
        .setListener(null as ViewPropertyAnimatorListener?)
    }
  }
}