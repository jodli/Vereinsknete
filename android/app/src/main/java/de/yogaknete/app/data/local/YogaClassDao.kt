package de.yogaknete.app.data.local

import androidx.room.*
import de.yogaknete.app.domain.model.YogaClass
import de.yogaknete.app.domain.model.ClassStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

@Dao
interface YogaClassDao {
    
    @Query("SELECT * FROM yoga_classes ORDER BY startTime DESC")
    fun getAllClasses(): Flow<List<YogaClass>>
    
    @Query("SELECT * FROM yoga_classes WHERE startTime >= :startDate AND startTime <= :endDate ORDER BY startTime ASC")
    fun getClassesInRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<YogaClass>>
    
    @Query("SELECT * FROM yoga_classes WHERE studioId = :studioId ORDER BY startTime DESC")
    fun getClassesByStudio(studioId: Long): Flow<List<YogaClass>>
    
    @Query("SELECT * FROM yoga_classes WHERE status = :status ORDER BY startTime ASC")
    fun getClassesByStatus(status: ClassStatus): Flow<List<YogaClass>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(yogaClass: YogaClass): Long
    
    @Update
    suspend fun updateClass(yogaClass: YogaClass)
    
    @Delete
    suspend fun deleteClass(yogaClass: YogaClass)
    
    @Query("UPDATE yoga_classes SET status = :status WHERE id = :id")
    suspend fun updateClassStatus(id: Long, status: ClassStatus)

    @Query("UPDATE yoga_classes SET status = :status WHERE id IN (:ids)")
    suspend fun updateClassesStatus(ids: List<Long>, status: ClassStatus)
    
    @Query("SELECT * FROM yoga_classes WHERE id = :id")
    suspend fun getClassById(id: Long): YogaClass?
    
    @Query("""
        SELECT * FROM yoga_classes 
        WHERE studioId = :studioId 
        AND CAST(strftime('%m', datetime(startTime)) AS INTEGER) = :month 
        AND CAST(strftime('%Y', datetime(startTime)) AS INTEGER) = :year
        ORDER BY startTime ASC
    """)
    suspend fun getClassesForStudioInMonth(studioId: Long, month: Int, year: Int): List<YogaClass>
    
    @Query("SELECT * FROM yoga_classes")
    suspend fun getAllClassesOnce(): List<YogaClass>
    
    @Query("DELETE FROM yoga_classes")
    suspend fun deleteAllClasses()
}
