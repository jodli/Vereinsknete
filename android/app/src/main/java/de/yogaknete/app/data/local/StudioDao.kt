package de.yogaknete.app.data.local

import androidx.room.*
import de.yogaknete.app.domain.model.Studio
import kotlinx.coroutines.flow.Flow

@Dao
interface StudioDao {
    
    @Query("SELECT * FROM studios ORDER BY name ASC")
    fun getAllStudios(): Flow<List<Studio>>
    
    @Query("SELECT * FROM studios WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveStudios(): Flow<List<Studio>>
    
    @Query("SELECT * FROM studios WHERE id = :id")
    suspend fun getStudioById(id: Long): Studio?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudio(studio: Studio): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudios(studios: List<Studio>)
    
    @Update
    suspend fun updateStudio(studio: Studio)
    
    @Delete
    suspend fun deleteStudio(studio: Studio)
    
    @Query("UPDATE studios SET isActive = 0 WHERE id = :id")
    suspend fun deactivateStudio(id: Long)
    
    @Query("SELECT COUNT(*) FROM studios WHERE isActive = 1")
    suspend fun getActiveStudioCount(): Int
    
    @Query("SELECT * FROM studios")
    suspend fun getAllStudiosOnce(): List<Studio>
    
    @Query("DELETE FROM studios")
    suspend fun deleteAllStudios()
}
