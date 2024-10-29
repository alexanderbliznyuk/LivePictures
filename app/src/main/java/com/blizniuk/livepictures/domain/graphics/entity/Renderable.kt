package com.blizniuk.livepictures.domain.graphics.entity

import android.graphics.Canvas

interface Renderable {
    fun render(canvas: Canvas, renderContext: RenderContext)
}