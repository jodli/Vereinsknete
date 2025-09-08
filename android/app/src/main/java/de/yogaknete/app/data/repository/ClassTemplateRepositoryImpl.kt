package de.yogaknete.app.data.repository

import de.yogaknete.app.data.local.dao.ClassTemplateDao
import de.yogaknete.app.data.local.entities.ClassTemplate
import de.yogaknete.app.domain.repository.ClassTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek
import javax.inject.Inject

class ClassTemplateRepositoryImpl @Inject constructor(
    private val classTemplateDao: ClassTemplateDao
) : ClassTemplateRepository {
    
    override fun getAllActiveTemplates(): Flow<List<ClassTemplate>> {
        return classTemplateDao.getAllActiveTemplates()
    }
    
    override fun getAllTemplates(): Flow<List<ClassTemplate>> {
        return classTemplateDao.getAllTemplates()
    }
    
    override fun getTemplatesByStudio(studioId: Long): Flow<List<ClassTemplate>> {
        return classTemplateDao.getTemplatesByStudio(studioId)
    }
    
    override suspend fun getTemplatesByDayOfWeek(dayOfWeek: DayOfWeek): List<ClassTemplate> {
        return classTemplateDao.getTemplatesByDayOfWeek(dayOfWeek)
    }
    
    override suspend fun getTemplateById(id: Long): ClassTemplate? {
        return classTemplateDao.getTemplateById(id)
    }
    
    override suspend fun createTemplate(template: ClassTemplate): Long {
        return classTemplateDao.insertTemplate(template)
    }
    
    override suspend fun updateTemplate(template: ClassTemplate) {
        classTemplateDao.updateTemplate(template)
    }
    
    override suspend fun deleteTemplate(template: ClassTemplate) {
        classTemplateDao.deleteTemplate(template)
    }
    
    override suspend fun setTemplateActive(id: Long, isActive: Boolean) {
        classTemplateDao.setTemplateActive(id, isActive)
    }
    
    override suspend fun getAllTemplatesOnce(): List<ClassTemplate> {
        return classTemplateDao.getAllTemplatesOnce()
    }
    
    override suspend fun insertTemplate(template: ClassTemplate): Long {
        return classTemplateDao.insertTemplate(template)
    }
    
    override suspend fun deleteAllTemplates() {
        classTemplateDao.deleteAllTemplates()
    }
}
