package com.blizniuk.livepictures.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.blizniuk.livepictures.data.graphics.FrameDb
import com.blizniuk.livepictures.data.settings.AppSettingsDb

@Database(entities = [AppSettingsDb::class, FrameDb::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun framesDao(): FrameDao
}