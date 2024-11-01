package com.blizniuk.livepictures.domain.graphics.entity

import android.graphics.Canvas
import com.blizniuk.livepictures.domain.graphics.entity.cmd.DrawCmd


data class Frame(
    val id: Long = 0,
    val drawCmds: List<DrawCmd>,
    val durationMs: Long,
    val index: Long = 0,
) : Renderable {
    override fun render(canvas: Canvas, renderContext: RenderContext) {
        drawCmds.forEach { it.render(canvas, renderContext) }
    }
}
