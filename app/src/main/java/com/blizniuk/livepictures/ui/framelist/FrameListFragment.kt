package com.blizniuk.livepictures.ui.framelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.blizniuk.livepictures.R
import com.blizniuk.livepictures.databinding.FragmentFrameListBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class FrameListFragment : BottomSheetDialogFragment() {
    private var binding: FragmentFrameListBinding? = null
    private val viewmodel: FragmentListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFrameListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.apply {
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            behavior.isDraggable = true
            behavior.isFitToContents = true
            behavior.peekHeight = resources.getDimensionPixelSize(R.dimen.frame_list_peek_height)
        }

        binding?.apply {
            val adapter = FramesAdapter(
                canvasWidth = requireArguments().getInt(ExtraCanvasWidth),
                canvasHeight = requireArguments().getInt(ExtraCanvasHeight),
                context = requireContext()
            )
            adapter.itemClick = { viewmodel.selectFrame(it) }

            framesRecycler.layoutManager = GridLayoutManager(requireContext(), 3)
            framesRecycler.adapter = adapter
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        viewmodel.data.collect {
                            adapter.submitData(it)
                        }
                    }

                    launch {
                        viewmodel.selectedItemId.collect {
                            //frames indices start from 1
                            adapter.selectedIndex = it - 1
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        private const val ExtraCanvasWidth = "extra_canvas_width"
        private const val ExtraCanvasHeight = "extra_canvas_height"

        fun newInstance(canvasWidth: Int, canvasHeight: Int): FrameListFragment {
            return FrameListFragment().apply {
                arguments = bundleOf(
                    ExtraCanvasWidth to canvasWidth,
                    ExtraCanvasHeight to canvasHeight
                )
            }
        }
    }
}