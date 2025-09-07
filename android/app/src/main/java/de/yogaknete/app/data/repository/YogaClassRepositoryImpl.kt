package de.yogaknete.app.data.repository

import de.yogaknete.app.data.local.YogaClassDao
import de.yogaknete.app.domain.model.YogaClass
import de.yogaknete.app.domain.repository.YogaClassRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

class YogaClassRepositoryImpl @Inject constructor(
    private val yogaClassDao: YogaClassDao
) : YogaClassRepository {
    
    override fun getClassesForWeek(startDate: LocalDate, endDate: LocalDate): Flow<List<YogaClass>> {
        val startDateTime = LocalDateTime(startDate.year, startDate.monthNumber, startDate.dayOfMonth, 0, 0)
        val endDateTime = LocalDateTime(endDate.year, endDate.monthNumber, endDate.dayOfMonth, 23, 59, 59)
        return yogaClassDao.getClassesInRange(startDateTime, endDateTime)
    }
    
    override fun getAllClasses(): Flow<List<YogaClass>> {
        return yogaClassDao.getAllClasses()
    }
    
    override suspend fun addClass(yogaClass: YogaClass): Long {
        return yogaClassDao.insertClass(yogaClass)
    }
    
    override suspend fun updateClass(yogaClass: YogaClass) {
        yogaClassDao.updateClass(yogaClass)
    }
    
    override suspend fun deleteClass(yogaClass: YogaClass) {
        yogaClassDao.deleteClass(yogaClass)
    }
    
    override suspend fun getClassById(id: Long): YogaClass? {
        return yogaClassDao.getClassById(id)
    }
    
    override suspend fun getClassesForInvoice(studioId: Long, month: Int, year: Int): List<YogaClass> {
        return yogaClassDao.getClassesForStudioInMonth(studioId, month, year)
    }
}
