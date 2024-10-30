package com.blizniuk.livepictures.data.graphics

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.blizniuk.livepictures.data.db.FrameDao
import com.blizniuk.livepictures.domain.graphics.FramesRepository
import com.blizniuk.livepictures.domain.graphics.entity.Frame
import com.blizniuk.livepictures.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FramesRepositoryImpl(
    private val frameDao: FrameDao,
    private val json: Json,
    private val settingsRepository: SettingsRepository
) : FramesRepository {
    override suspend fun newFrame(): Frame {
        val lastIndex = frameDao.count()
        val frame = FrameDb(index = lastIndex + 1, data = "")
        val id = frameDao.insertNewFrame(frame)
        return Frame(
            id = id,
            drawCmds = emptyList(),
            durationMs = settingsRepository.currentAppSettings().first().defaultFrameDurationMs,
            index = lastIndex,
        )
    }

    override suspend fun updateFrame(frame: Frame) {
        frameDao.updateFrame(mapFromFrame(frame))
    }

    override suspend fun deleteFrame(frame: Frame) {
        frameDao.deleteFrame(frame.id, frame.index)
    }

    override suspend fun deleteAllFrames() {
        frameDao.deleteAll()
    }

    fun framePages(): Flow<PagingData<Frame>> {
        return Pager(PagingConfig(pageSize = 20)) {
            frameDao.framePages()
        }
            .flow
            .map { it.map { frame -> mapFromDb(frame) } }
    }

    private fun mapFromDb(frameDb: FrameDb): Frame {
        val frame = json.decodeFromString<Frame>(frameDb.data)
        return frame.copy(id = frame.id, index = frame.index)
    }

    private fun mapFromFrame(frame: Frame): FrameDb {
        val encoded = json.encodeToString(frame)
        return FrameDb(
            id = frame.id,
            index = frame.index,
            data = encoded
        )
    }
}