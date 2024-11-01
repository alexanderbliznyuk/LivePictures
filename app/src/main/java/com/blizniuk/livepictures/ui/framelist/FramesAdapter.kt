package com.blizniuk.livepictures.ui.framelist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.blizniuk.livepictures.R
import com.blizniuk.livepictures.databinding.ListItemFrameBinding
import com.blizniuk.livepictures.domain.graphics.entity.Frame
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import com.blizniuk.livepictures.util.RoundCornersOutlineProvider

class FramesAdapter(
    private val canvasWidth: Int,
    private val canvasHeight: Int,
    context: Context
) : PagingDataAdapter<Frame, FrameViewHolder>(FramesItemCallback) {

    private val renderContext = RenderContext(context)
    private val roundCornersOutlineProvider = RoundCornersOutlineProvider(
        cornerRadius = context.resources.getDimension(R.dimen.selection_border_corners_radius)
    )

    var itemClick: ((Frame) -> Unit)? = null

    var selectedIndex: Long = -1
        set(value) {
            val prevValue = field
            field = value

            if (prevValue >= 0) {
                notifyItemChanged(prevValue.toInt())
            }
            notifyItemChanged(value.toInt())
        }

    override fun onBindViewHolder(holder: FrameViewHolder, position: Int) {
        val item: Frame? = getItem(position)
        holder.binding.apply {
            root.tag = item
            progress.isVisible = item == null
            preview.frame = item
            selectionBorder.isVisible = position.toLong() == selectedIndex

            text.text =
                root.context.getString(R.string.frame_list_item_title, (position + 1).toString())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FrameViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemFrameBinding.inflate(inflater, parent, false)
        binding.apply {
            preview.renderContext = renderContext
            preview.canvasWidth = canvasWidth
            preview.canvasHeight = canvasHeight
            preview.outlineProvider = roundCornersOutlineProvider
            preview.clipToOutline = true

            canvasBackground.outlineProvider = roundCornersOutlineProvider
            canvasBackground.clipToOutline = true

            binding.root.setOnClickListener { view ->
                val frame = view.tag as? Frame
                if (frame != null) {
                    itemClick?.invoke(frame)
                }
            }
        }

        return FrameViewHolder(binding)
    }
}


object FramesItemCallback : DiffUtil.ItemCallback<Frame>() {
    override fun areItemsTheSame(oldItem: Frame, newItem: Frame): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Frame, newItem: Frame): Boolean {
        return oldItem.id == newItem.id
    }
}

class FrameViewHolder(
    val binding: ListItemFrameBinding
) : ViewHolder(binding.root)