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
    protected abstract suspend fun insertInternal(frameDb: FrameDb): Long

    @Query("DELETE FROM frames WHERE id = :id")
    protected abstract suspend fun deleteInternal(id: Long)

    @Query("DELETE FROM frames")
    abstract suspend fun deleteAll()

    @Query("UPDATE frames SET frame_index = frame_index + 1 WHERE frame_index >= :initialIndex")
    abstract suspend fun incrementIndexes(initialIndex: Long)

    @Query("UPDATE frames SET frame_index = frame_index - 1 WHERE frame_index >= :initialIndex")
    abstract suspend fun decrementIndexes(initialIndex: Long)

    @Query("SELECT COUNT(*) FROM frames")
    abstract suspend fun count(): Long

    @Transaction
    open suspend fun insertNewFrame(frameDb: FrameDb): Long {
        incrementIndexes(frameDb.index)
        return insertInternal(frameDb)
    }

    @Transaction
    open suspend fun deleteFrame(id: Long, index: Long) {
        deleteInternal(id)
        decrementIndexes(index)
    }

    suspend fun updateFrame(frameDb: FrameDb) {
        insertNewFrame(frameDb)
    }

    @Query("SELECT * FROM frames ORDER BY frame_index")
    abstract fun framePages(): PagingSource<Int, FrameDb>
}