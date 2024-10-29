package com.blizniuk.livepictures.domain.graphics.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Point(
    @SerialName("x") val x: Float,
    @SerialName("y") val y: Float,
)