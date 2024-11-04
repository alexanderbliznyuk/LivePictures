package com.blizniuk.livepictures.ui.home.state

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

sealed class ToastUi {
    abstract fun getText(context: Context): String
    abstract val duration: Int

    class PlainRes(
        @StringRes private val resId: Int,
        private val params: Array<Any> = arrayOf(),
        override val duration: Int = Toast.LENGTH_SHORT
    ) :
        ToastUi() {
        override fun getText(context: Context): String {
            return context.getString(resId, *params)
        }
    }
}