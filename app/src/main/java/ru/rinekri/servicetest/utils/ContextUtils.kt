package ru.rinekri.servicetest.utils

import android.content.Context
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast

fun String.showToast(context: Context, length: Int = Toast.LENGTH_SHORT) {
  Toast.makeText(context, this, length).show()
}

fun String.showSnack(container: View, length: Int = Snackbar.LENGTH_LONG) {
  Snackbar.make(container, this, length).show()
}