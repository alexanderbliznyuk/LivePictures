package com.blizniuk.livepictures.data.graphics

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blizniuk.livepictures.domain.graphics.entity.cmd.DrawCmdData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Entity(tableName = "frames")
data class FrameDb(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(ColumnIdKey) val id: Long = 0,
    @ColumnInfo(FrameIndexdKey) val index: Long,
    @ColumnInfo(SerializedDataKey) val data: String
) {

    companion object {
        const val ColumnIdKey = "id"
        const val FrameIndexdKey = "frame_index"
        const val SerializedDataKey = "serialized_frame_data"
    }

}



@Serializable
data class FrameData(
    @SerialName("draw_data") val drawCmdData: List<DrawCmdData>,
    @SerialName("duration_ms") val durationMs: Long,
)