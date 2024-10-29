package com.blizniuk.livepictures.domain.settings


interface AppSettings {
    val defaultFrameDurationMs: Long
    val pathThicknessLevel: Float
    val eraseToolThicknessLevel: Float
    val selectedColor: Int
    val currentFrameId: Long
}