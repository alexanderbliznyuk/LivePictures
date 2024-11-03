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
        update(getSettingsInternal().copy(defaultFrameDurationMs = value))
    }

    override suspend fun setPathThicknessLevel(value: Float) {
        update(getSettingsInternal().copy(pathThicknessLevel = value))
    }

    override suspend fun setEraseToolThicknessLevel(value: Float) {
        update(getSettingsInternal().copy(eraseToolThicknessLevel = value))
    }

    override suspend fun setSelectedColor(value: Int) {
        update(getSettingsInternal().copy(selectedColor = value))
    }

    override suspend fun setCurrentFrameId(value: Long) {
        update(getSettingsInternal().copy(currentFrameId = value))
    }

    override suspend fun playbackSpeedFactor(value: Float) {
        update(getSettingsInternal().copy(playbackSpeedFactor = value))
    }

    override suspend fun getSetting(): AppSettings {
        return getSettingsInternal()
    }

    private suspend fun getSettingsInternal(): AppSettingsDb {
        return settingsDao.appSettings() ?: DefaultSettings
    }

    private suspend fun update(appSettings: AppSettingsDb) {
        settingsDao.updateSettings(appSettings)
    }

    private companion object {
        val DefaultSettings = AppSettingsDb(
            defaultFrameDurationMs = 250,
            pathThicknessLevel = 2F,
            eraseToolThicknessLevel = 10F,
            selectedColor = Color.BLUE,
            currentFrameId = -1,
            playbackSpeedFactor = 1F
        )
    }
}