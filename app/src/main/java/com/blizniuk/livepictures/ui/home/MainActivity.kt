package com.blizniuk.livepictures.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.blizniuk.livepictures.R
import com.blizniuk.livepictures.databinding.ActivityMainBinding
import com.blizniuk.livepictures.databinding.PopupMoreOptionsBinding
import com.blizniuk.livepictures.databinding.PopupShapePickerBinding
import com.blizniuk.livepictures.domain.graphics.ToolId
import com.blizniuk.livepictures.ui.cmdlist.CmdListFragment
import com.blizniuk.livepictures.ui.colorpicker.ColorPickerFragment
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
            val toolsButtons = listOf(pencil, erase)
            pencil.tag = ToolId.Pencil
            erase.tag = ToolId.Erase


            toolsButtons.forEach {
                it.setOnClickListener { view ->
                    viewModel.selectTool(view.tag as ToolId)
                }
            }

            val shapesIds = setOf(ToolId.ShapeCircle, ToolId.ShapeTriangle, ToolId.ShapeSquare)
            shapePicker.setOnClickListener {
                showShapePickerPopup(shapePicker)
            }

            colorPicker.setOnClickListener {
                ColorPickerFragment().show(supportFragmentManager, "ColorPicker")
            }

            openCmdList.setOnClickListener {
                CmdListFragment.newInstance().show(supportFragmentManager, "CmdList")
            }

            moreMenu.setOnClickListener {
                showMoreMenuPopup(moreMenu)
            }

            repeatOnStart {
                launch {
                    viewModel.selectedTool
                        .collect { tool ->
                            toolsButtons.forEach { view ->
                                view.isActivated = view.tag == tool
                            }

                            shapePicker.isActivated = tool in shapesIds
                        }
                }

                launch {
                    viewModel.selectedColor.collect { color ->
                        binding.chosenColor.imageTintList = ColorStateList.valueOf(color)
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

    private fun showShapePickerPopup(anchor: View) {
        val binding = PopupShapePickerBinding.inflate(layoutInflater)
        val popUp = PopupWindow(this)
        popUp.contentView = binding.root
        popUp.setBackgroundDrawable(null)
        popUp.width = ViewGroup.LayoutParams.WRAP_CONTENT
        popUp.height = resources.getDimensionPixelSize(R.dimen.shape_picker_popup_height)
        popUp.isFocusable = true
        popUp.elevation = 0F


        binding.apply {
            val currentToolId = viewModel.selectedTool.value
            shapeSquare.tag = ToolId.ShapeSquare
            shapeTriangle.tag = ToolId.ShapeTriangle
            shapeCircle.tag = ToolId.ShapeCircle

            val shapeViews = listOf(shapeSquare, shapeTriangle, shapeCircle)
            shapeViews.forEach { toolView ->
                toolView.isActivated = toolView.tag == currentToolId
                toolView.setOnClickListener {
                    (toolView.tag as? ToolId)?.let { toolId ->
                        viewModel.selectTool(toolId)
                    }

                    popUp.dismiss()
                }
            }
        }


        val popupWidth = resources.getDimensionPixelSize(R.dimen.shape_picker_popup_width)

        val xOffset = -(popupWidth - anchor.width) / 2
        popUp.showAsDropDown(anchor, xOffset, 0, Gravity.CENTER_HORIZONTAL)
    }

    private fun showMoreMenuPopup(anchor: View) {
        val binding = PopupMoreOptionsBinding.inflate(layoutInflater)
        val popUp = PopupWindow(this)
        popUp.contentView = binding.root
        popUp.setBackgroundDrawable(null)
        popUp.width = ViewGroup.LayoutParams.WRAP_CONTENT
        popUp.height = resources.getDimensionPixelSize(R.dimen.more_menu_popup_height)
        popUp.isFocusable = true
        popUp.elevation = 0F


        binding.apply {
            settings.setOnClickListener {

            }

            copyFrame.setOnClickListener {

            }

            exportGif.setOnClickListener {

            }

            generateFrames.setOnClickListener {

            }

            deleteAll.setOnClickListener {

            }
        }

        popUp.showAsDropDown(anchor, 0, 0, Gravity.END or Gravity.TOP)
    }
}