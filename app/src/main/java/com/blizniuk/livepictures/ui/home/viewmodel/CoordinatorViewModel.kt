package com.blizniuk.livepictures.ui.home.viewmodel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blizniuk.livepictures.R
import com.blizniuk.livepictures.domain.graphics.FramesRepository
import com.blizniuk.livepictures.domain.graphics.ToolData
import com.blizniuk.livepictures.domain.graphics.ToolId
import com.blizniuk.livepictures.domain.graphics.entity.Frame
import com.blizniuk.livepictures.domain.graphics.entity.FrameBuilder
import com.blizniuk.livepictures.domain.settings.SettingsRepository
import com.blizniuk.livepictures.ui.home.state.CanvasMode
import com.blizniuk.livepictures.ui.home.state.FrameCounterState
import com.blizniuk.livepictures.ui.home.state.LoaderUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoordinatorViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val framesRepository: FramesRepository,
) : ViewModel(), DefaultLifecycleObserver {


    val toasts = MutableSharedFlow<Int>(
        extraBufferCapacity = 2,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val selectedTool = MutableStateFlow(ToolId.Pencil)
    private val _loader: MutableStateFlow<LoaderUI?> = MutableStateFlow(LoaderUI(""))
    val loader = _loader
        .debounce(200L)

    val canvasMode: MutableStateFlow<CanvasMode> = MutableStateFlow(CanvasMode.Draw)

    private val settingsShared = settingsRepository.currentAppSettings()
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    val currentFrame: StateFlow<FrameBuilder?> = settingsShared
        .map { it.currentFrameId }
        .distinctUntilChanged()
        .map { framesRepository.getFrameById(it)?.toBuilder() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val selectedColor: Flow<Int> = settingsShared.map { it.selectedColor }

    val previousFrame: Flow<Frame?> = currentFrame.map { builder ->
        if (builder == null) return@map null
        framesRepository.getPreviousFrame(builder.build())
    }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

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
            .shareIn(viewModelScope, SharingStarted.Eagerly, 1)


    private val toolData: StateFlow<ToolData?>

    init {
        val drawSettings = settingsShared
            .map {
                DrawSettings(
                    penThicknessLevel = it.pathThicknessLevel,
                    eraseThicknessLevel = it.eraseToolThicknessLevel,
                    color = it.selectedColor
                )
            }
            .distinctUntilChanged()

        toolData = combine(selectedTool, drawSettings) { tool, settings ->
            when (tool) {
                ToolId.Pencil -> {
                    val color = settings.color
                    val thickness = settings.penThicknessLevel
                    ToolData.Pencil(thickness, color)
                }

                ToolId.Erase -> {
                    val thickness = settings.eraseThicknessLevel
                    ToolData.Erase(thicknessLevel = thickness)
                }

                ToolId.ShapeSquare,
                ToolId.ShapeSquareFilled -> {
                    val isFilled = tool == ToolId.ShapeSquareFilled
                    ToolData.Square(
                        thicknessLevel = settings.penThicknessLevel,
                        color = settings.color,
                        filled = isFilled
                    )
                }

                ToolId.ShapeCircle,
                ToolId.ShapeCircleFilled -> {
                    val isFilled = tool == ToolId.ShapeCircleFilled
                    ToolData.Circle(
                        thicknessLevel = settings.penThicknessLevel,
                        color = settings.color,
                        filled = isFilled
                    )
                }

                ToolId.ShapeTriangle,
                ToolId.ShapeTriangleFilled -> {
                    val isFilled = tool == ToolId.ShapeTriangleFilled
                    ToolData.Triangle(
                        thicknessLevel = settings.penThicknessLevel,
                        color = settings.color,
                        filled = isFilled
                    )
                }
            }
        }
            .filterNotNull()
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    }

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
            toasts.tryEmit(R.string.toast_new_frame_created)
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

    fun copyCurrentFrame() {
        viewModelScope.launch {
            saveCurrentFrame()
            val result = framesRepository.copyCurrentFrame()
            if (result != null) {
                toasts.tryEmit(R.string.toast_frame_duplicated)
            }
        }
    }


    fun saveChanges() {
        viewModelScope.launch {
            saveCurrentFrame()
        }
    }

    fun setDrawMode() {
        canvasMode.value = CanvasMode.Draw
    }

    fun toggleCanvasMode(lastFrame: Frame?) {
        viewModelScope.launch {
            saveCurrentFrame()
            val newMode = when (canvasMode.value) {
                CanvasMode.Draw -> CanvasMode.Animation
                CanvasMode.Animation -> CanvasMode.Draw
            }

//            if (newMode == CanvasMode.Draw && lastFrame != null) {
//                settingsRepository.setCurrentFrameId(lastFrame.id)
//            }

            canvasMode.value = newMode
        }
    }

    fun startAnimation(): Flow<Frame> {
        return framesRepository
            .animateFrames()
    }

    private var generateJob: Job? = null
    fun generateFrames(canvasWidth: Float, canvasHeight: Float, count: Int) {
        generateJob?.cancel()
        generateJob = viewModelScope.launch {
            showLoader("GENERATING")
            framesRepository.autoBuilder(canvasWidth, canvasHeight, count).start()
            dismissLoader()
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
        _loader.value = LoaderUI(text)
    }

    private fun dismissLoader() {
        _loader.value = null
    }


    private fun Frame.toBuilder(): FrameBuilder {
        val builder = FrameBuilder(this)
        builder.setToolData(toolData.value)
        return builder
    }


    private data class DrawSettings(
        val penThicknessLevel: Float,
        val eraseThicknessLevel: Float,
        val color: Int,
    )
}
