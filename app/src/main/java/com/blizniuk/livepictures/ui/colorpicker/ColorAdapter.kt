package com.blizniuk.livepictures.ui.colorpicker

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.blizniuk.livepictures.databinding.ListItemColorBinding

class ColorAdapter : ListAdapter<Int, ColorViewHolder>(ColorDiffCallback) {

    var itemClickListener: ((Int) -> Unit)? = null

    var selectedColor: Int? = null
        set(value) {
            val prevIndex = currentList.indexOf(field)
            field = value
            val currentIndex = currentList.indexOf(value)
            if (prevIndex != -1) {
                notifyItemChanged(prevIndex)
            }
            if (currentIndex != -1) {
                notifyItemChanged(currentIndex)
            }
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemColorBinding.inflate(inflater, parent, false)
        binding.apply {
            root.setOnClickListener {
                val color = root.tag as? Int
                selectedColor = color
                if (color != null) {
                    itemClickListener?.invoke(color)
                }
            }
        }

        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.binding.apply {
            val color = getItem(position)
            colorView.imageTintList = ColorStateList.valueOf(color)
            root.isActivated = color == selectedColor
            root.tag = color
        }
    }
}

object ColorDiffCallback : DiffUtil.ItemCallback<Int>() {
    override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
        return oldItem == newItem
    }

}

class ColorViewHolder(
    val binding: ListItemColorBinding
) : ViewHolder(binding.root)