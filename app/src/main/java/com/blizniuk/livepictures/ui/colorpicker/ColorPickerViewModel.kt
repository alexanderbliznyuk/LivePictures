package com.blizniuk.livepictures.ui.colorpicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blizniuk.livepictures.domain.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ColorPickerViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
): ViewModel() {

    fun updateColor(color: Int) {
        viewModelScope.launch {
            settingsRepository.setSelectedColor(color)
        }
    }
}