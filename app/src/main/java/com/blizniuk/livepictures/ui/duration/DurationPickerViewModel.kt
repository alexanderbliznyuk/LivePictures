package com.blizniuk.livepictures.ui.duration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blizniuk.livepictures.domain.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DurationPickerViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {


    suspend fun getCurrentFrameDuration(): Long {
        return settingsRepository.getSetting().defaultFrameDurationMs
    }

    fun updateFrameDuration(durationMs: Long) {
        viewModelScope.launch {
            settingsRepository.setDefaultFrameDurationMs(durationMs)
        }
    }
}