package ru.hdlabs.electrosmeta.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.hdlabs.electrosmeta.data.local.EstimateStatus
import ru.hdlabs.electrosmeta.data.local.entities.EstimateEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateLineEntity

data class EstimateWithLines(
    val estimate: EstimateEntity,
    val lines: List<EstimateLineEntity>,
)

@Dao
interface EstimateDao {
    @Query("SELECT * FROM estimates ORDER BY updatedAtMillis DESC")
    fun observeAll(): Flow<List<EstimateEntity>>

    @Query(
        """
        SELECT * FROM estimates 
        WHERE title LIKE '%' || :query || '%' 
           OR clientName LIKE '%' || :query || '%'
           OR clientAddress LIKE '%' || :query || '%'
        ORDER BY updatedAtMillis DESC
        """,
    )
    fun search(query: String): Flow<List<EstimateEntity>>

    @Query("SELECT * FROM estimates WHERE id = :id")
    suspend fun getById(id: Long): EstimateEntity?

    @Query("SELECT * FROM estimate_lines WHERE estimateId = :estimateId ORDER BY sortOrder ASC, id ASC")
    suspend fun getLines(estimateId: Long): List<EstimateLineEntity>

    @Transaction
    suspend fun getWithLines(id: Long): EstimateWithLines? {
        val estimate = getById(id) ?: return null
        return EstimateWithLines(estimate, getLines(id))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstimate(entity: EstimateEntity): Long

    @Update
    suspend fun updateEstimate(entity: EstimateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLine(entity: EstimateLineEntity): Long

    @Update
    suspend fun updateLine(entity: EstimateLineEntity)

    @Query("DELETE FROM estimate_lines WHERE id = :lineId")
    suspend fun deleteLine(lineId: Long)

    @Query("DELETE FROM estimates WHERE id = :id")
    suspend fun deleteEstimate(id: Long)

    @Query("SELECT * FROM estimates WHERE status = :status ORDER BY updatedAtMillis DESC LIMIT 1")
    suspend fun latestByStatus(status: EstimateStatus): EstimateEntity?

    @Query("SELECT * FROM estimates")
    suspend fun getAllOnce(): List<EstimateEntity>

    @Query("SELECT * FROM estimate_lines")
    suspend fun getAllLinesOnce(): List<EstimateLineEntity>
}
