package com.blizniuk.livepictures

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.blizniuk.livepictures.databinding.ActivityMainBinding
import com.blizniuk.livepictures.domain.graphics.ToolId
import com.blizniuk.livepictures.util.RoundCornersOutlineProvider
import com.blizniuk.livepictures.util.repeatOnStart
import com.blizniuk.livepictures.viewmodel.CoordinatorViewModel
import dagger.hilt.android.AndroidEntryPoint

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

            val cornerRadius = resources.getDimension(R.dimen.round_corners_radius)
            canvasBackground.clipToOutline = true
            canvasBackground.outlineProvider = RoundCornersOutlineProvider(cornerRadius)

            canvasView.clipToOutline = true
            canvasView.outlineProvider = RoundCornersOutlineProvider(cornerRadius)


            initTools()
            initLoader()
            initCanvasView()
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
                viewModel.currentFrame.collect { frame ->
                    canvasView.frameBuilder = frame
                }
            }
        }
    }
}