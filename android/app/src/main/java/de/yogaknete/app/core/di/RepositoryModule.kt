package de.yogaknete.app.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.yogaknete.app.data.repository.UserProfileRepositoryImpl
import de.yogaknete.app.data.repository.StudioRepositoryImpl
import de.yogaknete.app.domain.repository.UserProfileRepository
import de.yogaknete.app.domain.repository.StudioRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        userProfileRepositoryImpl: UserProfileRepositoryImpl
    ): UserProfileRepository
    
    @Binds
    @Singleton
    abstract fun bindStudioRepository(
        studioRepositoryImpl: StudioRepositoryImpl
    ): StudioRepository
}
