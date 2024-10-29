package com.blizniuk.livepictures.data.settings

import android.graphics.Color
import com.blizniuk.livepictures.data.db.SettingsDao
import com.blizniuk.livepictures.domain.settings.AppSettings
import com.blizniuk.livepictures.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class SettingsRepositoryImpl(
    private val settingsDao: SettingsDao,
) : SettingsRepository {

    override fun currentAppSettings(): Flow<AppSettings> {
        return settingsDao.appSettingsFlow()
            .map { it ?: DefaultSettings }
    }

    override suspend fun setDefaultFrameDurationMs(value: Long) {
        update(getSettings().copy(defaultFrameDurationMs = value))
    }

    override suspend fun setPathThicknessLevel(value: Float) {
        update(getSettings().copy(pathThicknessLevel = value))
    }

    override suspend fun setEraseToolThicknessLevel(value: Float) {
        update(getSettings().copy(eraseToolThicknessLevel = value))
    }

    override suspend fun setSelectedColor(value: Int) {
        update(getSettings().copy(selectedColor = value))
    }

    override suspend fun setCurrentFrameId(value: Long) {
        update(getSettings().copy(currentFrameId = value))
    }

    private suspend fun getSettings(): AppSettingsDb {
        return settingsDao.appSettings() ?: DefaultSettings
    }

    private suspend fun update(appSettings: AppSettingsDb) {
        settingsDao.updateSettings(appSettings)
    }

    private companion object {
        val DefaultSettings = AppSettingsDb(
            defaultFrameDurationMs = 100,
            pathThicknessLevel = 1F,
            eraseToolThicknessLevel = 4F,
            selectedColor = Color.BLUE,
            currentFrameId = 1
        )
    }
}