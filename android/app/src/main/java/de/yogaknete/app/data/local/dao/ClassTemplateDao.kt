package de.yogaknete.app.data.local.dao

import androidx.room.*
import de.yogaknete.app.data.local.entities.ClassTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek

@Dao
interface ClassTemplateDao {
    
    @Query("SELECT * FROM class_templates WHERE isActive = 1 ORDER BY name")
    fun getAllActiveTemplates(): Flow<List<ClassTemplate>>
    
    @Query("SELECT * FROM class_templates ORDER BY name")
    fun getAllTemplates(): Flow<List<ClassTemplate>>
    
    @Query("SELECT * FROM class_templates WHERE studioId = :studioId AND isActive = 1")
    fun getTemplatesByStudio(studioId: Long): Flow<List<ClassTemplate>>
    
    @Query("SELECT * FROM class_templates WHERE dayOfWeek = :dayOfWeek AND isActive = 1")
    suspend fun getTemplatesByDayOfWeek(dayOfWeek: DayOfWeek): List<ClassTemplate>
    
    @Query("SELECT * FROM class_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): ClassTemplate?
    
    @Insert
    suspend fun insertTemplate(template: ClassTemplate): Long
    
    @Update
    suspend fun updateTemplate(template: ClassTemplate)
    
    @Delete
    suspend fun deleteTemplate(template: ClassTemplate)
    
    @Query("UPDATE class_templates SET isActive = :isActive WHERE id = :id")
    suspend fun setTemplateActive(id: Long, isActive: Boolean)
}
