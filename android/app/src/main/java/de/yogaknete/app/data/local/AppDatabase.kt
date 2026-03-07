package de.yogaknete.app.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import de.yogaknete.app.domain.model.UserProfile
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.model.YogaClass
import de.yogaknete.app.domain.model.Invoice
import de.yogaknete.app.data.local.entities.ClassTemplate
import de.yogaknete.app.data.local.dao.ClassTemplateDao

@Database(
    entities = [
        UserProfile::class,
        Studio::class,
        YogaClass::class,
        ClassTemplate::class,
        Invoice::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(DateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userProfileDao(): UserProfileDao
    abstract fun studioDao(): StudioDao
    abstract fun yogaClassDao(): YogaClassDao
    abstract fun classTemplateDao(): ClassTemplateDao
    abstract fun invoiceDao(): InvoiceDao
    
    companion object {
        const val DATABASE_NAME = "yoga_knete_database"

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE class_templates ADD COLUMN recurrenceIntervalWeeks INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE class_templates ADD COLUMN referenceDate TEXT DEFAULT NULL")
            }
        }
    }
}
