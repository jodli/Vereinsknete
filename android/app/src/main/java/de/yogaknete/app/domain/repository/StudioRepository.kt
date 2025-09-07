package de.yogaknete.app.domain.repository

import de.yogaknete.app.domain.model.Studio
import kotlinx.coroutines.flow.Flow

interface StudioRepository {
    
    fun getAllStudios(): Flow<List<Studio>>
    
    fun getAllActiveStudios(): Flow<List<Studio>>
    
    suspend fun getStudioById(id: Long): Studio?
    
    suspend fun saveStudio(studio: Studio): Long
    
    suspend fun saveStudios(studios: List<Studio>)
    
    suspend fun updateStudio(studio: Studio)
    
    suspend fun deleteStudio(studio: Studio)
    
    suspend fun deactivateStudio(id: Long)
    
    suspend fun getActiveStudioCount(): Int
}
