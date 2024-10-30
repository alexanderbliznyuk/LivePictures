package com.blizniuk.livepictures.domain.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun currentAppSettings(): Flow<AppSettings>

    suspend fun setDefaultFrameDurationMs(value: Long)
    suspend fun setPathThicknessLevel(value: Float)
    suspend fun setEraseToolThicknessLevel(value: Float)
    suspend fun setSelectedColor(value: Int)
    suspend fun setCurrentFrameId(value: Long)
}