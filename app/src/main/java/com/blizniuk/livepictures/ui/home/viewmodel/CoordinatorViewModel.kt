package com.blizniuk.livepictures.ui.home.viewmodel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blizniuk.livepictures.domain.graphics.FramesRepository
import com.blizniuk.livepictures.domain.graphics.ToolData
import com.blizniuk.livepictures.domain.graphics.ToolId
import com.blizniuk.livepictures.domain.graphics.entity.Frame
import com.blizniuk.livepictures.domain.graphics.entity.FrameBuilder
import com.blizniuk.livepictures.domain.settings.SettingsRepository
import com.blizniuk.livepictures.ui.home.state.FrameCounterState
import com.blizniuk.livepictures.ui.home.state.LoaderUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoordinatorViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val framesRepository: FramesRepository,
) : ViewModel(), DefaultLifecycleObserver {

    val selectedTool = MutableStateFlow(ToolId.Pencil)
    val loader: MutableStateFlow<LoaderUI?> = MutableStateFlow(LoaderUI(""))

    val currentFrame: StateFlow<FrameBuilder?> = settingsRepository.currentAppSettings().map {
        framesRepository.getFrameById(it.currentFrameId)?.toBuilder()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val previousFrame: Flow<Frame?> = currentFrame.map { builder ->
        if (builder == null) return@map null
        framesRepository.getPreviousFrame(builder.build())
    }

    val counterState: Flow<FrameCounterState> =
        combine(framesRepository.framesCount(), currentFrame) { total, frame ->
            val frameIndex = frame?.index
            val prevEnabled = frameIndex != null && frameIndex > 1
            val nextEnabled = frameIndex != null && frameIndex < total
            FrameCounterState(
                total = total.toString(),
                currentIndex = frameIndex?.toString() ?: "-",
                prevEnabled = prevEnabled,
                nextEnabled = nextEnabled
            )
        }

    private var toolData: StateFlow<ToolData?> = selectedTool.mapNotNull {
        when (it) {
            ToolId.Pencil -> {
                val color = settingsRepository.getSetting().selectedColor
                val thickness = settingsRepository.getSetting().pathThicknessLevel
                ToolData.Pencil(thickness, color)
            }

            ToolId.Erase -> {
                val thickness = settingsRepository.getSetting().eraseToolThicknessLevel
                ToolData.Erase(thicknessLevel = thickness)
            }

            ToolId.Toolbox -> null
            ToolId.ColorPicker -> null
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            //Ensure we have last frame
            framesRepository.getLastFrame()
            dismissLoader()

            toolData.filterNotNull().collect { tool ->
                currentFrame.value?.setToolData(tool)
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        viewModelScope.launch {
            saveCurrentFrame()
        }
    }

    fun selectTool(tool: ToolId) {
        selectedTool.value = tool
    }

    fun newFrame() {
        viewModelScope.launch {
            saveCurrentFrame()
            framesRepository.newFrame()
        }
    }

    fun deleteCurrentFrame() {
        viewModelScope.launch {
            val frame = currentFrame.value?.build()
            if (frame != null) {
                framesRepository.deleteFrame(frame)
            }
        }
    }

    fun deleteAllFrames() {
        viewModelScope.launch {
            framesRepository.deleteAllFrames()
        }
    }

    fun prevFrame() {
        viewModelScope.launch {
            val frame = saveCurrentFrame()
            if (frame != null) {
                val prevFrame = framesRepository.getPreviousFrame(frame)
                if (prevFrame != null) {
                    settingsRepository.setCurrentFrameId(prevFrame.id)
                }
            }
        }
    }

    fun nextFrame() {
        viewModelScope.launch {
            val frame = saveCurrentFrame()
            if (frame != null) {
                val nextFrame = framesRepository.getNextFrame(frame)
                if (nextFrame != null) {
                    settingsRepository.setCurrentFrameId(nextFrame.id)
                }
            }
        }
    }

    fun saveChanges() {
        viewModelScope.launch {
            saveCurrentFrame()
        }
    }


    private suspend fun saveCurrentFrame(): Frame? {
        val currentBuilder = currentFrame.value
        val frame = currentBuilder?.build()
        if (frame != null && currentBuilder.isChanged()) {
            framesRepository.updateFrame(frame)
        }

        return frame
    }

    private fun showLoader(text: String) {
        loader.value = LoaderUI(text)
    }

    private fun dismissLoader() {
        loader.value = null
    }


    private fun Frame.toBuilder(): FrameBuilder {
        val builder = FrameBuilder(this)
        builder.setToolData(toolData.value)
        return builder
    }
}










