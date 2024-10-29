package com.blizniuk.livepictures.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.blizniuk.livepictures.data.graphics.FrameDb

@Dao
abstract class FrameDao {

    @Upsert
    abstract suspend fun insertInternal(frameDb: FrameDb)

    @Query("DELETE FROM frames WHERE id = :id")
    abstract suspend fun deleteInternal(id: Long)

    @Query("DELETE FROM frames")
    abstract suspend fun deleteAll()

    @Query("UPDATE frames SET frame_index = frame_index + 1 WHERE frame_index >= :initialIndex")
    abstract suspend fun incrementIndexes(initialIndex: Long)

    @Query("UPDATE frames SET frame_index = frame_index - 1 WHERE frame_index >= :initialIndex")
    abstract suspend fun decrementIndexes(initialIndex: Long)

    @Transaction
    suspend fun insertNewFrame(frameDb: FrameDb) {
        incrementIndexes(frameDb.index)
        insertInternal(frameDb)
    }

    @Transaction
    suspend fun deleteFrame(frameDb: FrameDb) {
        deleteInternal(frameDb.id)
        decrementIndexes(frameDb.index)
    }

    @Query("SELECT * FROM frames ORDER BY frame_index")
    abstract fun framePages(): PagingSource<Long, FrameDb>
}