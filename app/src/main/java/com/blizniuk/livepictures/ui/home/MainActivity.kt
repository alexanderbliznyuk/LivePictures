package com.blizniuk.livepictures.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.lifecycle.lifecycleScope
import com.blizniuk.livepictures.R
import com.blizniuk.livepictures.databinding.ActivityMainBinding
import com.blizniuk.livepictures.databinding.DialogFrameBuilderBinding
import com.blizniuk.livepictures.databinding.PopupMoreOptionsBinding
import com.blizniuk.livepictures.databinding.PopupShapePickerBinding
import com.blizniuk.livepictures.domain.graphics.ToolId
import com.blizniuk.livepictures.ui.cmdlist.CmdListFragment
import com.blizniuk.livepictures.ui.colorpicker.ColorPickerFragment
import com.blizniuk.livepictures.ui.duration.FrameDurationPickerFragment
import com.blizniuk.livepictures.ui.framelist.FrameListFragment
import com.blizniuk.livepictures.ui.home.state.CanvasMode
import com.blizniuk.livepictures.ui.home.viewmodel.CoordinatorViewModel
import com.blizniuk.livepictures.util.RoundCornersOutlineProvider
import com.blizniuk.livepictures.util.repeatOnStart
import com.blizniuk.livepictures.view.CanvasView.OnCmdEditModeChangeListener
import com.blizniuk.livepictures.view.CanvasView.OnUndoRedoChangeListener
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
                loaderContent.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
                )
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

            val shapesIds = setOf(
                ToolId.ShapeCircle,
                ToolId.ShapeCircleFilled,
                ToolId.ShapeTriangle,
                ToolId.ShapeTriangleFilled,
                ToolId.ShapeSquare,
                ToolId.ShapeSquareFilled,
            )
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

            penThicknessPicker.onThicknessChanged = {
                viewModel.setPathThickness(it)
            }

            eraseThicknessPicker.onThicknessChanged = {
                viewModel.setEraseThickness(it)
            }

            repeatOnStart {
                launch {
                    viewModel.selectedTool
                        .collect { tool ->
                            toolsButtons.forEach { view ->
                                view.isActivated = view.tag == tool
                            }

                            shapePicker.isActivated = tool in shapesIds
                            penThicknessPicker.isVisible =
                                tool == ToolId.Pencil || tool in shapesIds
                            eraseThicknessPicker.isVisible = tool == ToolId.Erase
                        }
                }

                launch {
                    viewModel.selectedColor.collect { color ->
                        chosenColor.imageTintList = ColorStateList.valueOf(color)
                    }
                }

                launch {
                    viewModel.pathThickness.collect { thickness ->
                        penThicknessPicker.setThickness(thickness)
                    }
                }

                launch {
                    viewModel.eraseThickness.collect { thickness ->
                        eraseThicknessPicker.setThickness(thickness)
                    }
                }
            }
        }
    }


    private fun initLoader() {
        binding.apply {
            repeatOnStart {
                launch {
                    viewModel.loader.collect { loaderUi ->
                        loaderContent.isVisible = loaderUi != null
                        loaderUi?.apply {
                            loaderText.text = if (this.textId != 0) {
                                getString(textId)
                            } else {
                                null
                            }

                            loaderCancel.isVisible = loaderUi.cancelAction != null
                            loaderCancel.setOnClickListener {
                                cancelAction?.invoke()
                            }
                        }
                    }
                }

                launch {
                    viewModel.toasts.collect { ui ->
                        val text = ui.getText(this@MainActivity)
                        Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun initCanvasView() {
        binding.apply {
            confirmChanges.setOnClickListener { canvasView.confirmChanges() }
            discardChanges.setOnClickListener { canvasView.discardChanges() }
            deleteCmd.setOnClickListener { canvasView.deleteCmd() }

            prevFrame.setOnClickListener { viewModel.prevFrame() }
            nextFrame.setOnClickListener { viewModel.nextFrame() }

            canvasView.onCmdEditModeChangeListener = OnCmdEditModeChangeListener { isInEditMode ->
                topToolPanel.isVisible = !isInEditMode
                bottomToolPanel.isVisible = !isInEditMode
                play.isVisible = !isInEditMode

                prevFrame.isVisible = !isInEditMode
                nextFrame.isVisible = !isInEditMode
                toolConfigPane.isVisible = !isInEditMode

                editCmdPanel.isVisible = isInEditMode
            }

            canvasView.onCmdUndoRedoChangeListener = OnUndoRedoChangeListener { canUndo, canRedo ->
                undo.isEnabled = canUndo
                redo.isEnabled = canRedo
            }
        }

        startUpdatingCanvas()
    }

    private var canvasUpdatesJob: Job? = null
    private fun startUpdatingCanvas() {
        canvasUpdatesJob?.cancel()
        binding.apply {
            canvasUpdatesJob = repeatOnStart {
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

                launch {
                    viewModel.counterState.collect { state ->
                        prevFrame.isEnabled = state.prevEnabled
                        nextFrame.isEnabled = state.nextEnabled
                        totalFrames.text = state.total
                        currentFrameIndex.text = state.currentIndex
                        currentFrameIndexHiddenWidth.text = state.total
                    }
                }
            }
        }
    }


    private fun stopUpdatingCanvas() {
        canvasUpdatesJob?.cancel()
        canvasUpdatesJob = null
    }

    private fun initTopCommands() {
        binding.apply {
            undo.setOnClickListener { canvasView.undo() }
            redo.setOnClickListener { canvasView.redo() }

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
                                startUpdatingCanvas()
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
        }
    }

    override fun onStop() {
        super.onStop()
        stopAnimation()
        viewModel.setDrawMode()
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
            toolConfigPane.isVisible = !isAnimation
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

            shapeSquareFilled.tag = ToolId.ShapeSquareFilled
            shapeTriangleFilled.tag = ToolId.ShapeTriangleFilled
            shapeCircleFilled.tag = ToolId.ShapeCircleFilled

            val shapeViews = listOf(
                shapeSquare,
                shapeTriangle,
                shapeCircle,
                shapeSquareFilled,
                shapeTriangleFilled,
                shapeCircleFilled
            )

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
        val popupBinding = PopupMoreOptionsBinding.inflate(layoutInflater)
        val popUp = PopupWindow(this)
        popUp.contentView = popupBinding.root
        popUp.setBackgroundDrawable(null)
        popUp.width = ViewGroup.LayoutParams.WRAP_CONTENT
        popUp.height = resources.getDimensionPixelSize(R.dimen.more_menu_popup_height)
        popUp.isFocusable = true
        popUp.elevation = 0F

        popupBinding.apply {
            animationSpeed.setOnClickListener {
                FrameDurationPickerFragment.newInstance()
                    .show(supportFragmentManager, "Duration Picker")

                popUp.dismiss()
            }

            copyFrame.setOnClickListener {
                viewModel.copyCurrentFrame()
                popUp.dismiss()
            }

            exportGif.setOnClickListener {
                viewModel.exportGif(binding.canvasView.width, binding.canvasView.height)
                popUp.dismiss()
            }

            generateFrames.setOnClickListener {
                showFrameGeneratorDialog()
                popUp.dismiss()
            }

            deleteAll.setOnClickListener {
                viewModel.deleteAllFrames()
                popUp.dismiss()
            }
        }

        popUp.showAsDropDown(anchor, 0, 0, Gravity.END or Gravity.TOP)
    }

    private fun showFrameGeneratorDialog() {
        val dialogBinding = DialogFrameBuilderBinding.inflate(layoutInflater)

        dialogBinding.apply {
            val dialog = AlertDialog.Builder(this@MainActivity)
                .setView(root)
                .setTitle(R.string.more_generate)
                .setMessage(R.string.dialog_frame_builder_message)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    handleDialogInput(countInput.text.toString())
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .create()

            countInput.setOnFocusChangeListener { _, _ ->
                countInput.postDelayed(250) {
                    val service = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
                    service?.showSoftInput(countInput, InputMethodManager.SHOW_IMPLICIT)
                }
            }
            countInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    handleDialogInput(countInput.text.toString())
                    dialog.dismiss()
                    true
                } else {
                    false
                }
            }
            countInput.requestFocus()
            dialog.show()
        }
    }

    private fun handleDialogInput(text: String) {
        val count = text.toIntOrNull()
        if (count == null || count <= 0) {
            Toast.makeText(
                this,
                R.string.dialog_frame_builder_input_error,
                Toast.LENGTH_LONG
            ).show()
        } else {
            viewModel.generateFrames(
                binding.canvasView.width.toFloat(),
                binding.canvasView.height.toFloat(),
                count
            )
        }
    }
}