package com.blizniuk.livepictures.data.graphics

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "frames")
data class FrameDb(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("id") val id: Long = 0,
    @ColumnInfo("frame_index") val index: Long,
    @ColumnInfo("serialized_frame") val data: String
)