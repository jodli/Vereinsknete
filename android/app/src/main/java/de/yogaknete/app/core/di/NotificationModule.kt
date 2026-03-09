package de.yogaknete.app.core.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.yogaknete.app.data.notification.ClassNotificationSchedulerImpl
import de.yogaknete.app.domain.service.ClassNotificationScheduler
import kotlinx.datetime.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindClassNotificationScheduler(
        impl: ClassNotificationSchedulerImpl
    ): ClassNotificationScheduler

    companion object {
        @Provides
        @Singleton
        fun provideWorkManager(
            @ApplicationContext context: Context
        ): WorkManager = WorkManager.getInstance(context)

        @Provides
        @Singleton
        fun provideClock(): Clock = Clock.System
    }
}
