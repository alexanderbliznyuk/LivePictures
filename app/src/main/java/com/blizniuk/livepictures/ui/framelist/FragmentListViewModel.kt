package com.blizniuk.livepictures.ui.framelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.blizniuk.livepictures.domain.graphics.FramesRepository
import com.blizniuk.livepictures.domain.graphics.entity.Frame
import com.blizniuk.livepictures.domain.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FragmentListViewModel @Inject constructor(
    private val framesRepository: FramesRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val data = framesRepository.frames(0)
        .cachedIn(viewModelScope)

    val selectedFrameIndexId = settingsRepository
        .currentAppSettings()
        .mapNotNull {
            framesRepository.getFrameIndexById(it.currentFrameId)
        }

    private var selectFrameJob: Job? = null

    fun selectFrame(frame: Frame) {
        selectFrameJob?.cancel()
        selectFrameJob = viewModelScope.launch {
            settingsRepository.setCurrentFrameId(frame.id)
        }
    }
}