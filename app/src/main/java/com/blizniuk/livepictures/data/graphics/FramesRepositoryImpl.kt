package com.blizniuk.livepictures.data.graphics

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.blizniuk.livepictures.data.db.FrameDao
import com.blizniuk.livepictures.domain.graphics.FramesRepository
import com.blizniuk.livepictures.domain.graphics.entity.Frame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FramesRepositoryImpl(
    private val frameDao: FrameDao,
    private val json: Json,
) : FramesRepository {
    override suspend fun newFrame(): Frame {

    }

    override suspend fun updateFrame(frame: Frame) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteFrame(frame: Frame) {
        TODO("Not yet implemented")
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