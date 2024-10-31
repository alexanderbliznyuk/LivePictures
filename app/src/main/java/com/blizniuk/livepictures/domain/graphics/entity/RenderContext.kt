package com.blizniuk.livepictures.domain.graphics.entity

import android.content.Context

class RenderContext(
    private val context: Context,
) {

    private val displayMetrics = context.resources.displayMetrics
    private val storage: Array<Any?> = Array(32) { null }

    fun <T> get(key: Int, factory: () -> T): T {
        if (storage[key] == null) {
            storage[key] = factory()
        }

        @Suppress("UNCHECKED_CAST")
        return storage[key] as T
    }

    fun convertToPx(dp: Float): Float {
        return dp * displayMetrics.density
    }


    companion object {
        private var keyIndex: Int = 0
        fun newKey(): Int {
            return keyIndex++
        }
    }
}