package de.yogaknete.app.data.local

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDateTime

class DateTimeConverters {
    
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }
    
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it) }
    }
}
