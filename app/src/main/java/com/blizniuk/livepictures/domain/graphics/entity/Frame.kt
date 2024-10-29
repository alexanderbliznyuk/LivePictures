package com.blizniuk.livepictures.domain.graphics.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
data class Frame(
    @Transient val id: Long = 0,
    @SerialName("draw_cmds") val drawCmds: List<DrawCmd>,
    @SerialName("duration_ms") val durationMs: Long,
    @Transient val index: Long = 0,
)