package com.blizniuk.livepictures.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.blizniuk.livepictures.data.settings.AppSettingsDb
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Upsert
    suspend fun updateSettings(appSettings: AppSettingsDb)

    @Query("SELECT * FROM app_settings LIMIT 1")
    fun appSettingsFlow(): Flow<AppSettingsDb?>

    @Query("SELECT * FROM app_settings LIMIT 1")
    suspend fun appSettings(): AppSettingsDb?
}