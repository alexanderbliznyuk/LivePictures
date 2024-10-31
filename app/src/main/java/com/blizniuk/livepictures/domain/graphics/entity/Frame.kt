package com.blizniuk.livepictures.domain.graphics.entity

import com.blizniuk.livepictures.domain.graphics.entity.cmd.DrawCmd


data class Frame(
    val id: Long = 0,
    val drawCmds: List<DrawCmd>,
    val durationMs: Long,
    val index: Long = 0,
)