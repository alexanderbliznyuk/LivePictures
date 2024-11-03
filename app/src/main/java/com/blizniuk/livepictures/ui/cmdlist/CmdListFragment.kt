package com.blizniuk.livepictures.ui.cmdlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.blizniuk.livepictures.R
import com.blizniuk.livepictures.databinding.FragmentCmdListBinding
import com.blizniuk.livepictures.ui.home.viewmodel.CoordinatorViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CmdListFragment : BottomSheetDialogFragment() {
    private var binding: FragmentCmdListBinding? = null
    private val viewmodel by activityViewModels<CoordinatorViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCmdListBinding.inflate(inflater, container, false)
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
            val adapter = CmdListAdapter(context = requireContext())
            cmdRecycler.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            cmdRecycler.adapter = adapter

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        viewmodel.currentFrame.collect {
                            val frameBuilder = it ?: return@collect
                            val cmds = frameBuilder.drawCommands.map { AdapterItem.Cmd(it) }
                            if (cmds.isNotEmpty()) {
                                adapter.submitList(cmds)
                            } else {
                                adapter.submitList(listOf(AdapterItem.Empty))
                            }

                            adapter.itemClick = { cmd -> frameBuilder.cmdToEdit = cmd }
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
        fun newInstance(): CmdListFragment {
            return CmdListFragment()
        }
    }
}