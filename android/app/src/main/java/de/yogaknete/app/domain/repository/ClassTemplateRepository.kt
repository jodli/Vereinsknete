package de.yogaknete.app.domain.repository

import de.yogaknete.app.data.local.entities.ClassTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek

interface ClassTemplateRepository {
    
    fun getAllActiveTemplates(): Flow<List<ClassTemplate>>
    
    fun getAllTemplates(): Flow<List<ClassTemplate>>
    
    fun getTemplatesByStudio(studioId: Long): Flow<List<ClassTemplate>>
    
    suspend fun getTemplatesByDayOfWeek(dayOfWeek: DayOfWeek): List<ClassTemplate>
    
    suspend fun getTemplateById(id: Long): ClassTemplate?
    
    suspend fun createTemplate(template: ClassTemplate): Long
    
    suspend fun updateTemplate(template: ClassTemplate)
    
    suspend fun deleteTemplate(template: ClassTemplate)
    
    suspend fun setTemplateActive(id: Long, isActive: Boolean)
}
