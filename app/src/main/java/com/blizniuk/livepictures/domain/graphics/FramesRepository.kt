package com.blizniuk.livepictures.domain.graphics

import com.blizniuk.livepictures.domain.graphics.entity.Frame

interface FramesRepository {
    suspend fun newFrame(): Frame
    suspend fun updateFrame(frame: Frame)
    suspend fun deleteFrame(frame: Frame)
    suspend fun deleteAllFrames()



}