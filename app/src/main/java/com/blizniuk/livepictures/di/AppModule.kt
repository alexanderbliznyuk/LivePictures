package com.blizniuk.livepictures.di

import android.content.Context
import androidx.room.Room
import com.blizniuk.livepictures.data.db.AppDatabase
import com.blizniuk.livepictures.data.graphics.FramesRepositoryImpl
import com.blizniuk.livepictures.data.settings.SettingsRepositoryImpl
import com.blizniuk.livepictures.domain.graphics.FramesRepository
import com.blizniuk.livepictures.domain.settings.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, "live_pictures.db"
        ).build()
    }

    @Singleton
    @Provides
    fun providesSettingsRepository(appDatabase: AppDatabase): SettingsRepository {
        return SettingsRepositoryImpl(
            settingsDao = appDatabase.settingsDao()
        )
    }

    @Singleton
    @Provides
    fun provideJson(): Json {
        return Json {
            classDiscriminator = "type"
        }
    }


    @Singleton
    @Provides
    fun providesFramesRepository(
        appDatabase: AppDatabase,
        json: Json,
        settingsRepository: SettingsRepository
    ): FramesRepository {
        return FramesRepositoryImpl(
            frameDao = appDatabase.framesDao(),
            json = json,
            settingsRepository = settingsRepository
        )
    }
}
