package com.blizniuk.livepictures.ui.duration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.blizniuk.livepictures.R
import com.blizniuk.livepictures.databinding.FragmentDurationPickerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FrameDurationPickerFragment : BottomSheetDialogFragment() {

    private var binding: FragmentDurationPickerBinding? = null
    private val viewModel: DurationPickerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDurationPickerBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            viewLifecycleOwner.lifecycleScope.launch {

                durationSeekBar.max = (MaxDuration - MinDuration)
                durationSeekBar.setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) {
                            updateLabel(progress + MinDuration)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        val progress = seekBar.progress + MinDuration
                        updateLabel(progress)
                        viewModel.updateFrameDuration(progress.toLong())
                    }
                })

                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    val duration = viewModel.getCurrentFrameDuration()
                    durationSeekBar.progress = duration.toInt() - MinDuration

                    updateLabel(duration.toInt())
                }
            }
        }
    }

    private fun updateLabel(duration: Int) {
        binding?.currentDuration?.text =
            getString(R.string.frame_duration_duration, duration.toString())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        private const val MaxDuration = 2000
        private const val MinDuration = 8

        fun newInstance(): FrameDurationPickerFragment {
            return FrameDurationPickerFragment()
        }
    }
}