package com.blizniuk.livepictures.ui.cmdlist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.blizniuk.livepictures.R
import com.blizniuk.livepictures.databinding.ListItemCmdBinding
import com.blizniuk.livepictures.databinding.ListItemCmdEmptyBinding
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import com.blizniuk.livepictures.domain.graphics.entity.cmd.CircleShapeCmd
import com.blizniuk.livepictures.domain.graphics.entity.cmd.DrawCmd
import com.blizniuk.livepictures.domain.graphics.entity.cmd.ErasePathCmd
import com.blizniuk.livepictures.domain.graphics.entity.cmd.FreePathCmd
import com.blizniuk.livepictures.domain.graphics.entity.cmd.RectShapeCmd
import com.blizniuk.livepictures.util.RoundCornersOutlineProvider

class CmdListAdapter(
    context: Context
) : ListAdapter<AdapterItem, ViewHolder>(CmdItemCallback) {

    private val renderContext = RenderContext(context)
    private val roundCornersOutlineProvider = RoundCornersOutlineProvider(
        cornerRadius = context.resources.getDimension(R.dimen.selection_border_corners_radius)
    )
    var itemClick: ((DrawCmd) -> Unit)? = null
    var selectedCmdPosition: Int = -1
        set(value) {
            val prevPos = field
            field = value
            if (prevPos >= 0) notifyItemChanged(prevPos)
            if (value >= 0) notifyItemChanged(value)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.list_item_cmd -> {
                val binding = ListItemCmdBinding.inflate(inflater, parent, false)
                binding.apply {
                    preview.renderContext = renderContext
                    preview.outlineProvider = roundCornersOutlineProvider
                    preview.clipToOutline = true

                    canvasBackground.outlineProvider = roundCornersOutlineProvider
                    canvasBackground.clipToOutline = true

                    binding.root.setOnClickListener { view ->
                        val position = (view.tag as? Int)
                        if (position != null) {
                            selectedCmdPosition = position
                        }

                        val cmd = preview.cmd
                        if (cmd != null) {
                            itemClick?.invoke(cmd)
                        }
                    }
                }
                DrawCmdHolder(binding)
            }

            else -> {
                val binding = ListItemCmdEmptyBinding.inflate(inflater, parent, false)
                EmptyCmdHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is DrawCmdHolder) {
            val item = getItem(position) as AdapterItem.Cmd
            val cmd = item.drawCmd

            holder.binding.apply {
                root.tag = position
                preview.cmd = cmd
                selectionBorder.isVisible = position == selectedCmdPosition
                text.setText(getCmdName(cmd))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AdapterItem.Cmd -> R.layout.list_item_cmd
            is AdapterItem.Empty -> R.layout.list_item_cmd_empty
        }
    }

    private fun getCmdName(cmd: DrawCmd): Int {
        return when (cmd) {
            is ErasePathCmd -> R.string.tooltip_tool_erase
            is FreePathCmd -> R.string.tooltip_tool_pencil
            is CircleShapeCmd -> R.string.tooltip_shape_circle
            is RectShapeCmd -> R.string.tooltip_shape_square
        }
    }
}

sealed class AdapterItem {
    data class Cmd(val drawCmd: DrawCmd) : AdapterItem()
    data object Empty : AdapterItem()
}

class DrawCmdHolder(val binding: ListItemCmdBinding) : ViewHolder(binding.root)
class EmptyCmdHolder(val binding: ListItemCmdEmptyBinding) : ViewHolder(binding.root)

object CmdItemCallback : DiffUtil.ItemCallback<AdapterItem>() {
    override fun areItemsTheSame(oldItem: AdapterItem, newItem: AdapterItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: AdapterItem, newItem: AdapterItem): Boolean {
        return oldItem == newItem
    }
}