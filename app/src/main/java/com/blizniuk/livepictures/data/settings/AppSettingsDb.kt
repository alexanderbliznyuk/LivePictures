package com.blizniuk.livepictures.data.settings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blizniuk.livepictures.domain.settings.AppSettings

@Entity(tableName = "app_settings")
data class AppSettingsDb(
    @PrimaryKey @ColumnInfo("settings_id") val settingId: String = "settings_id",
    @ColumnInfo("default_frame_duration") override val defaultFrameDurationMs: Long,
    @ColumnInfo("path_thickness_level") override val pathThicknessLevel: Float,
    @ColumnInfo("erase_tool_thickness_level") override val eraseToolThicknessLevel: Float,
    @ColumnInfo("selected_color") override val selectedColor: Int,
    @ColumnInfo("current_frame_id") override val currentFrameId: Long,
    @ColumnInfo("playback_speed_factor") override val playbackSpeedFactor: Float,
) : AppSettings