package com.blizniuk.livepictures.domain.graphics.entity.cmd

import com.blizniuk.livepictures.domain.graphics.entity.Point
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class DrawCmdData {
    open fun toDrawCmd(): DrawCmd {
        TODO()
    }
}












