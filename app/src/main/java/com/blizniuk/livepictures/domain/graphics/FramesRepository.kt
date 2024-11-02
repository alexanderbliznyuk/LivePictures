package com.blizniuk.livepictures.domain.graphics

import androidx.paging.PagingData
import com.blizniuk.livepictures.domain.graphics.entity.AnimationFrameInfo
import com.blizniuk.livepictures.domain.graphics.entity.Frame
import kotlinx.coroutines.flow.Flow

interface FramesRepository {
    suspend fun newFrame(): Frame
    suspend fun updateFrame(frame: Frame)
    suspend fun deleteFrame(frame: Frame): Frame
    suspend fun deleteAllFrames(): Frame
    suspend fun getFrameById(id: Long): Frame?
    suspend fun getFrameIndexById(id: Long): Long?

    suspend fun getPreviousFrame(frame: Frame): Frame?
    suspend fun getNextFrame(frame: Frame): Frame?
    suspend fun getLastFrame(): Frame

    fun animateFrames(): Flow<Frame>

    fun frames(initialId: Long): Flow<PagingData<Frame>>
    fun framesCount(): Flow<Long>
}