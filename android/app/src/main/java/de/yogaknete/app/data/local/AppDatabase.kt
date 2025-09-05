package de.yogaknete.app.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import de.yogaknete.app.domain.model.UserProfile
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.model.YogaClass

@Database(
    entities = [
        UserProfile::class,
        Studio::class,
        YogaClass::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userProfileDao(): UserProfileDao
    abstract fun studioDao(): StudioDao
    abstract fun yogaClassDao(): YogaClassDao
    
    companion object {
        const val DATABASE_NAME = "yoga_knete_database"
    }
}
