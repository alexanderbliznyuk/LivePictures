package com.blizniuk.livepictures.viewmodel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blizniuk.livepictures.domain.graphics.FramesRepository
import com.blizniuk.livepictures.domain.graphics.ToolData
import com.blizniuk.livepictures.domain.graphics.ToolId
import com.blizniuk.livepictures.domain.graphics.entity.FrameBuilder
import com.blizniuk.livepictures.domain.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoordinatorViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val framesRepository: FramesRepository,
) : ViewModel(), DefaultLifecycleObserver {

    val selectedTool = MutableStateFlow(ToolId.Pencil)
    val loader: MutableStateFlow<LoaderUI?> = MutableStateFlow(LoaderUI(""))
    val currentFrame: MutableStateFlow<FrameBuilder?> = MutableStateFlow(null)

    private var toolData: Flow<ToolData> = selectedTool.mapNotNull {
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
    }

    init {
        viewModelScope.launch {
            val frame = framesRepository.getLastFrame()
            currentFrame.value = FrameBuilder(frame)
            dismissLoader()

            toolData.collect { tool ->
                currentFrame.value?.setToolData(tool)
            }


        }
    }

    override fun onStop(owner: LifecycleOwner) {
        viewModelScope.launch {
            val frame = currentFrame.value?.build()
            if (frame != null) {
                framesRepository.updateFrame(frame)
            }
        }
    }

    fun selectTool(tool: ToolId) {
        selectedTool.value = tool
    }

    private fun showLoader(text: String) {
        loader.value = LoaderUI(text)
    }

    private fun dismissLoader() {
        loader.value = null
    }
}


data class LoaderUI(
    val text: String
)




