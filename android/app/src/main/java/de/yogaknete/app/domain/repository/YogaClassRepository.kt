package de.yogaknete.app.domain.repository

import de.yogaknete.app.domain.model.YogaClass
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface YogaClassRepository {
    fun getClassesForWeek(startDate: LocalDate, endDate: LocalDate): Flow<List<YogaClass>>
    fun getAllClasses(): Flow<List<YogaClass>>
    suspend fun addClass(yogaClass: YogaClass): Long
    suspend fun updateClass(yogaClass: YogaClass)
    suspend fun deleteClass(yogaClass: YogaClass)
    suspend fun getClassById(id: Long): YogaClass?
}
