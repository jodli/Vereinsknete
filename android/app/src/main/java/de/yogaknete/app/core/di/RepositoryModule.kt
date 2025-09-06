package de.yogaknete.app.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.yogaknete.app.data.repository.UserProfileRepositoryImpl
import de.yogaknete.app.data.repository.StudioRepositoryImpl
import de.yogaknete.app.data.repository.ClassTemplateRepositoryImpl
import de.yogaknete.app.data.repository.YogaClassRepositoryImpl
import de.yogaknete.app.data.repository.InvoiceRepositoryImpl
import de.yogaknete.app.domain.repository.UserProfileRepository
import de.yogaknete.app.domain.repository.StudioRepository
import de.yogaknete.app.domain.repository.ClassTemplateRepository
import de.yogaknete.app.domain.repository.YogaClassRepository
import de.yogaknete.app.domain.repository.InvoiceRepository
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
    
    @Binds
    @Singleton
    abstract fun bindClassTemplateRepository(
        classTemplateRepositoryImpl: ClassTemplateRepositoryImpl
    ): ClassTemplateRepository
    
    @Binds
    @Singleton
    abstract fun bindYogaClassRepository(
        yogaClassRepositoryImpl: YogaClassRepositoryImpl
    ): YogaClassRepository
    
    @Binds
    @Singleton
    abstract fun bindInvoiceRepository(
        invoiceRepositoryImpl: InvoiceRepositoryImpl
    ): InvoiceRepository
}
