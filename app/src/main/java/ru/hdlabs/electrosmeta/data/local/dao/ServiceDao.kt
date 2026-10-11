package ru.hdlabs.electrosmeta.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.hdlabs.electrosmeta.data.local.entities.ServiceEntity

@Dao
interface ServiceDao {
    @Query(
        """
        SELECT * FROM services 
        WHERE archived = 0 AND categoryId = :categoryId 
        ORDER BY sortOrder ASC, id ASC
        """,
    )
    fun observeByCategory(categoryId: Long): Flow<List<ServiceEntity>>

    @Query(
        """
        SELECT * FROM services 
        WHERE archived = 0 
        ORDER BY sortOrder ASC, id ASC
        """,
    )
    fun observeActive(): Flow<List<ServiceEntity>>

    @Query(
        """
        SELECT * FROM services 
        WHERE archived = 0 AND isFavorite = 1 
        ORDER BY sortOrder ASC LIMIT 20
        """,
    )
    fun observeFavorites(): Flow<List<ServiceEntity>>

    @Query(
        """
        SELECT * FROM services 
        WHERE archived = 0 AND name LIKE '%' || :query || '%' 
        ORDER BY name ASC LIMIT 50
        """,
    )
    suspend fun search(query: String): List<ServiceEntity>

    @Query("SELECT * FROM services WHERE id = :id")
    suspend fun getById(id: Long): ServiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ServiceEntity): Long

    @Update
    suspend fun update(entity: ServiceEntity)

    @Query("UPDATE services SET archived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE services SET archived = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("UPDATE services SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("SELECT * FROM services WHERE archived = 0")
    suspend fun getActiveOnce(): List<ServiceEntity>

    @Query("SELECT * FROM services")
    suspend fun getAllOnce(): List<ServiceEntity>

    @Query("DELETE FROM services")
    suspend fun deleteAll()
}
