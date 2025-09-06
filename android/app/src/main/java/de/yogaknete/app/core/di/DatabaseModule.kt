package de.yogaknete.app.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.yogaknete.app.data.local.*
import de.yogaknete.app.data.local.dao.ClassTemplateDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration() // For development - consider proper migration for production
        .build()
    }
    
    @Provides
    fun provideUserProfileDao(database: AppDatabase): UserProfileDao {
        return database.userProfileDao()
    }
    
    @Provides
    fun provideStudioDao(database: AppDatabase): StudioDao {
        return database.studioDao()
    }
    
    @Provides
    fun provideYogaClassDao(database: AppDatabase): YogaClassDao {
        return database.yogaClassDao()
    }
    
    @Provides
    fun provideClassTemplateDao(database: AppDatabase): ClassTemplateDao {
        return database.classTemplateDao()
    }
    
    @Provides
    fun provideInvoiceDao(database: AppDatabase): InvoiceDao {
        return database.invoiceDao()
    }
}
