package com.blizniuk.livepictures.ui.home.state

import androidx.annotation.StringRes

data class LoaderUI(
    @StringRes val textId: Int = 0,
    val cancelAction: (() -> Unit)? = null,
)