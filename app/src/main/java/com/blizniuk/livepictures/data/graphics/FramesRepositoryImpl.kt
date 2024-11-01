package com.blizniuk.livepictures.data.graphics

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.blizniuk.livepictures.data.db.FrameDao
import com.blizniuk.livepictures.domain.graphics.FramesRepository
import com.blizniuk.livepictures.domain.graphics.entity.Frame
import com.blizniuk.livepictures.domain.settings.AppSettings
import com.blizniuk.livepictures.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
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
        val newFrame = Frame(
            id = 0,
            drawCmds = emptyList(),
            durationMs = getAppSettings().defaultFrameDurationMs,
            index = lastIndex + 1
        )

        val id = frameDao.insertNewFrame(mapFromFrame(newFrame))
        settingsRepository.setCurrentFrameId(id)
        return newFrame.copy(id = id)
    }

    override suspend fun updateFrame(frame: Frame) {
        frameDao.updateFrame(mapFromFrame(frame))
    }

    override suspend fun deleteFrame(frame: Frame): Frame {
        frameDao.deleteFrame(frame.id, frame.index)
        val frameDb = frameDao.getFrameByIndex(frame.index)
        val currentFrame = if (frameDb != null) {
            settingsRepository.setCurrentFrameId(frameDb.id)
            mapFromDb(frameDb)
        } else {
            newFrame()
        }

        return currentFrame
    }

    override suspend fun deleteAllFrames(): Frame {
        frameDao.deleteAll()
        return newFrame()
    }

    override suspend fun getFrameById(id: Long): Frame? {
        val frame = frameDao.getFrameById(id) ?: return null
        return mapFromDb(frame)
    }

    override suspend fun getLastFrame(): Frame {
        val id = settingsRepository.getSetting().currentFrameId

        if (id > 0) {
            val frameDb = frameDao.getFrameById(id)
            if (frameDb != null) {
                return mapFromDb(frameDb)
            }
        }

        val newFrame = newFrame()
        settingsRepository.setCurrentFrameId(newFrame.id)
        return newFrame
    }

    override fun framesCount(): Flow<Long> {
        return frameDao.framesCount()
    }

    override suspend fun getPreviousFrame(frame: Frame): Frame? {
         return frameDao.getPrevFrame(frame.index)?.toFrame()
    }

    override suspend fun getNextFrame(frame: Frame): Frame? {
        return frameDao.getNextFrame(frame.index)?.toFrame()
    }

    fun framePages(): Flow<PagingData<Frame>> {
        return Pager(PagingConfig(pageSize = 20)) {
            frameDao.framePages()
        }
            .flow
            .map { it.map { frame -> mapFromDb(frame) } }
    }


    private fun FrameDb.toFrame(): Frame {
        return mapFromDb(this)
    }
    private fun mapFromDb(frameDb: FrameDb): Frame {
        val frameData = json.decodeFromString<FrameData>(frameDb.data)

        return Frame(
            id = frameDb.id,
            drawCmds = frameData.drawCmdData.map { it.toDrawCmd() },
            durationMs = frameData.durationMs,
            index = frameDb.index
        )
    }

    private fun mapFromFrame(frame: Frame): FrameDb {
        val frameData = FrameData(
            drawCmdData = frame.drawCmds.map { it.getDrawData() },
            durationMs = frame.durationMs
        )
        val encoded = json.encodeToString(frameData)
        return FrameDb(
            id = frame.id,
            index = frame.index,
            data = encoded
        )
    }

    private suspend fun getAppSettings(): AppSettings {
        return settingsRepository.getSetting()
    }
}