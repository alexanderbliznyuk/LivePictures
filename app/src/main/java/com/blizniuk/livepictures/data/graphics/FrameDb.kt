package com.blizniuk.livepictures.data.graphics

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blizniuk.livepictures.domain.graphics.entity.cmd.DrawCmdData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Entity(tableName = "frames")
data class FrameDb(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("id") val id: Long = 0,
    @ColumnInfo("frame_index") val index: Long,
    @ColumnInfo("serialized_frame") val data: String
)


@Serializable
data class FrameData(
    @SerialName("draw_data") val drawCmdData: List<DrawCmdData>,
    @SerialName("duration_ms") val durationMs: Long,
)