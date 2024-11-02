package com.blizniuk.livepictures.ui.colorpicker

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.blizniuk.livepictures.databinding.DialogColorPickerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ColorPickerFragment : DialogFragment() {

    private var binding: DialogColorPickerBinding? = null

    private var selectedColor: Int? = null

    private val vm: ColorPickerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogColorPickerBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            val adapter = ColorAdapter()
            adapter.submitList(PredefinedColors)

            updateSelectedColor(colorPicker.getColor())

            adapter.itemClickListener = { color ->
                updateSelectedColor(color)
            }

            colorPicker.onColorChangeListener = { color ->
                updateSelectedColor(color)
            }

            colorList.layoutManager = GridLayoutManager(requireContext(), 5)
            colorList.adapter = adapter
            (colorList.itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false

            cancel.setOnClickListener {
                dismiss()
            }

            ok.setOnClickListener {
                selectedColor?.let { vm.updateColor(it) }
                dismiss()
            }
        }
    }

    private fun updateSelectedColor(color: Int) {
        binding?.apply {
            selectedColorView.imageTintList = ColorStateList.valueOf(color)
            (colorList.adapter as? ColorAdapter)?.selectedColor = color
        }
        selectedColor = color
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private companion object {
        private val PredefinedColors = listOf(
            0xFFFFFECC.toInt(),
            0xFFFF95D5.toInt(),
            0xFFFFD1A9.toInt(),
            0xFFEDCAFF.toInt(),
            0xFFCCF3FF.toInt(),

            0xFFF3ED00.toInt(),
            0xFFF8D3E3.toInt(),
            0xFFFA9A46.toInt(),
            0xFFB18CFE.toInt(),
            0xFF94E4FD.toInt(),

            0xFFA8DB10.toInt(),
            0xFFFB66A4.toInt(),
            0xFFFC7600.toInt(),
            0xFF9747FF.toInt(),
            0xFF00C9FB.toInt(),

            0xFF75BB41.toInt(),
            0xFFDC0057.toInt(),
            0xFFED746C.toInt(),
            0xFF4D21B2.toInt(),
            0xFF73A8FC.toInt(),

            0xFF4E7A25.toInt(),
            0xFF9D234C.toInt(),
            0xFFFF3D00.toInt(),
            0xFF641580.toInt(),
            0xFF1976D2.toInt(),
        )
    }

}