package de.yogaknete.app.data.repository

import de.yogaknete.app.data.local.StudioDao
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.repository.StudioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudioRepositoryImpl @Inject constructor(
    private val studioDao: StudioDao
) : StudioRepository {
    
    override fun getAllStudios(): Flow<List<Studio>> {
        return studioDao.getAllStudios()
    }
    
    override fun getAllActiveStudios(): Flow<List<Studio>> {
        return studioDao.getAllActiveStudios()
    }
    
    override suspend fun getStudioById(id: Long): Studio? {
        return studioDao.getStudioById(id)
    }
    
    override suspend fun saveStudio(studio: Studio): Long {
        return studioDao.insertStudio(studio)
    }
    
    override suspend fun saveStudios(studios: List<Studio>) {
        studioDao.insertStudios(studios)
    }
    
    override suspend fun updateStudio(studio: Studio) {
        studioDao.updateStudio(studio)
    }
    
    override suspend fun deleteStudio(studio: Studio) {
        studioDao.deleteStudio(studio)
    }
    
    override suspend fun deactivateStudio(id: Long) {
        studioDao.deactivateStudio(id)
    }
    
    override suspend fun getActiveStudioCount(): Int {
        return studioDao.getActiveStudioCount()
    }
    
    override suspend fun getAllStudiosOnce(): List<Studio> {
        return studioDao.getAllStudiosOnce()
    }
    
    override suspend fun insertStudio(studio: Studio): Long {
        return studioDao.insertStudio(studio)
    }
    
    override suspend fun deleteAllStudios() {
        studioDao.deleteAllStudios()
    }
}
