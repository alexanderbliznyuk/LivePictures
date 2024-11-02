package com.blizniuk.livepictures.data.db

import android.database.Cursor
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.blizniuk.livepictures.data.graphics.FrameDb
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT COUNT(*) FROM frames")
    abstract fun framesCount(): Flow<Long>

    @Query("SELECT * FROM frames WHERE id = :id LIMIT 1")
    abstract suspend fun getFrameById(id: Long): FrameDb?

    @Query("SELECT frame_index FROM frames WHERE id = :id LIMIT 1")
    abstract suspend fun getFrameIndexById(id: Long): Long?

    @Query("SELECT * FROM frames ORDER BY frame_index DESC LIMIT 1")
    abstract suspend fun getLastFrame(): FrameDb?

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

    @Query("SELECT * FROM frames WHERE frame_index <= :index ORDER BY frame_index DESC LIMIT 1")
    abstract suspend fun getFrameByIndex(index: Long): FrameDb?

    @Query("SELECT * FROM frames WHERE frame_index > :index ORDER BY frame_index LIMIT 1")
    abstract suspend fun getNextFrame(index: Long): FrameDb?

    @Query("SELECT * FROM frames WHERE frame_index < :index ORDER BY frame_index DESC LIMIT 1")
    abstract suspend fun getPrevFrame(index: Long): FrameDb?

    suspend fun updateFrame(frameDb: FrameDb) {
        insertInternal(frameDb)
    }

    @Query("SELECT * FROM frames ORDER BY frame_index")
    abstract fun framePages(): PagingSource<Int, FrameDb>

    @Query("SELECT * FROM frames ORDER BY frame_index")
    abstract fun getFramesCursor(): Cursor
}