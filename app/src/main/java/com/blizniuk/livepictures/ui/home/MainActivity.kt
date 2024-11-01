package com.blizniuk.livepictures.ui.home

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.blizniuk.livepictures.R
import com.blizniuk.livepictures.databinding.ActivityMainBinding
import com.blizniuk.livepictures.domain.graphics.ToolId
import com.blizniuk.livepictures.ui.framelist.FrameListFragment
import com.blizniuk.livepictures.ui.home.viewmodel.CoordinatorViewModel
import com.blizniuk.livepictures.util.RoundCornersOutlineProvider
import com.blizniuk.livepictures.util.repeatOnStart
import dagger.hilt.android.AndroidEntryPoint
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
        }
    }


    private fun initTools() {
        binding.apply {
            val toolsButtons = listOf(pencil, erase, toolbox, colorPicker)
            pencil.tag = ToolId.Pencil
            erase.tag = ToolId.Erase
            toolbox.tag = ToolId.Toolbox
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

    private fun initCanvasView() {
        binding.apply {
            repeatOnStart {
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
}