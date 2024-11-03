package com.blizniuk.livepictures.domain.graphics.entity.cmd

import kotlinx.serialization.Serializable

@Serializable
sealed class DrawCmdData {
    abstract fun toDrawCmd(): DrawCmd
}












