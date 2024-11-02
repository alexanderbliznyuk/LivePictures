package com.blizniuk.livepictures.ui.home

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.blizniuk.livepictures.R
import com.blizniuk.livepictures.databinding.ActivityMainBinding
import com.blizniuk.livepictures.domain.graphics.ToolId
import com.blizniuk.livepictures.ui.framelist.FrameListFragment
import com.blizniuk.livepictures.ui.home.state.CanvasMode
import com.blizniuk.livepictures.ui.home.viewmodel.CoordinatorViewModel
import com.blizniuk.livepictures.util.RoundCornersOutlineProvider
import com.blizniuk.livepictures.util.repeatOnStart
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: CoordinatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycle.addObserver(viewModel)

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.apply {
            setContentView(root)
            ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            val cornerRadius = resources.getDimension(R.dimen.canvas_round_corners_radius)
            canvasBackground.clipToOutline = true
            canvasBackground.outlineProvider = RoundCornersOutlineProvider(cornerRadius)

            canvasView.clipToOutline = true
            canvasView.outlineProvider = RoundCornersOutlineProvider(cornerRadius)


            initTools()
            initLoader()
            initCanvasView()
            initFrameCounter()
            initTopCommands()
            initPlayPause()
        }
    }


    private fun initTools() {
        binding.apply {
            val toolsButtons = listOf(pencil, erase, shapePicker, colorPicker)
            pencil.tag = ToolId.Pencil
            erase.tag = ToolId.Erase
            shapePicker.tag = ToolId.ShapePicker
            colorPicker.tag = ToolId.ColorPicker

            val clickListener: (View) -> Unit = { view -> viewModel.selectTool(view.tag as ToolId) }
            toolsButtons.forEach { it.setOnClickListener(clickListener) }

            repeatOnStart {
                viewModel.selectedTool
                    .collect { tool ->
                        toolsButtons.forEach { view ->
                            view.isActivated = view.tag == tool
                        }
                    }
            }
        }
    }

    private fun initLoader() {
        binding.apply {
            repeatOnStart {
                viewModel.loader.collect {
                    loaderContent.isVisible = it != null
                }
            }
        }
    }

    private var canvasJob: Job? = null
    private fun initCanvasView() {
        canvasJob?.cancel()
        canvasJob = repeatOnStart {
            binding.apply {
                launch {
                    viewModel.currentFrame.collect { frame ->
                        canvasView.frameBuilder = frame
                    }
                }

                launch {
                    viewModel.previousFrame.collect { frame ->
                        canvasView.previousFrame = frame
                    }
                }
            }
        }
    }

    private fun stopUpdatingCanvas() {
        canvasJob?.cancel()
        canvasJob = null
    }

    private fun initFrameCounter() {
        binding.apply {
            prevFrame.setOnClickListener { viewModel.prevFrame() }
            nextFrame.setOnClickListener { viewModel.nextFrame() }
            repeatOnStart {
                viewModel.counterState.collect { state ->
                    prevFrame.isEnabled = state.prevEnabled
                    nextFrame.isEnabled = state.nextEnabled
                    totalFrames.text = state.total
                    currentFrameIndex.text = state.currentIndex
                }
            }
        }
    }

    private fun initTopCommands() {
        binding.apply {
            deleteFrame.setOnClickListener { viewModel.deleteCurrentFrame() }
            newFrame.setOnClickListener { viewModel.newFrame() }

            frameList.setOnClickListener {
                viewModel.saveChanges()
                FrameListFragment.newInstance(
                    canvasWidth = canvasView.width,
                    canvasHeight = canvasView.height
                )
                    .show(supportFragmentManager, "frame_list_dialog")
            }
        }
    }

    private fun initPlayPause() {
        binding.apply {
            play.setOnClickListener { viewModel.toggleCanvasMode(canvasView.animationFrame) }

            repeatOnStart {
                launch {
                    viewModel.canvasMode.collect { mode ->
                        updateUi(mode)
                        when (mode) {
                            CanvasMode.Draw -> {
                                stopAnimation()
                                initCanvasView()
                            }

                            CanvasMode.Animation -> {
                                startAnimation()
                                stopUpdatingCanvas()
                            }
                        }
                    }
                }
            }
        }
    }

    private var animationJob: Job? = null
    private fun startAnimation() {
        animationJob?.cancel()
        animationJob = lifecycleScope.launch {
            viewModel.startAnimation().collect { frame ->
                binding.apply {
                    canvasView.animationFrame = frame
                    currentFrameIndex.text = frame.index.toString()
                }
            }
        }
    }

    private fun stopAnimation() {
        if (animationJob != null) {
            animationJob?.cancel()
            animationJob = null

            binding.apply {
                (canvasView.animationFrame)?.let { viewModel.selectActiveFrame(it) }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        stopAnimation()
//        viewModel.setDrawMode()
    }

    private fun updateUi(mode: CanvasMode) {
        binding.apply {
            val icon = when (mode) {
                CanvasMode.Draw -> R.drawable.ic_play
                CanvasMode.Animation -> R.drawable.ic_pause
            }
            play.setImageResource(icon)

            canvasView.mode = mode

            val isAnimation = mode == CanvasMode.Animation
            topToolPanel.isVisible = !isAnimation
            bottomToolPanel.isVisible = !isAnimation
            prevFrame.isVisible = !isAnimation
            nextFrame.isVisible = !isAnimation
        }
    }
}