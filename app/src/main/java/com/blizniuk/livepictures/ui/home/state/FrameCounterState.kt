package com.blizniuk.livepictures.ui.home.state

data class FrameCounterState(
    val total: String,
    val currentIndex: String,
    val prevEnabled: Boolean,
    val nextEnabled: Boolean,
)