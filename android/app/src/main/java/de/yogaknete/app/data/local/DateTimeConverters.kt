package de.yogaknete.app.data.local

import androidx.room.TypeConverter
import de.yogaknete.app.domain.model.CreationSource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.DayOfWeek

class DateTimeConverters {
    
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }
    
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it) }
    }
    
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.toString()
    }
    
    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? {
        return timeString?.let { LocalTime.parse(it) }
    }
    
    @TypeConverter
    fun fromDayOfWeek(dayOfWeek: DayOfWeek?): Int? {
        return dayOfWeek?.value
    }
    
    @TypeConverter
    fun toDayOfWeek(dayValue: Int?): DayOfWeek? {
        return dayValue?.let { DayOfWeek.of(it) }
    }
    
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }
    
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }
    
    @TypeConverter
    fun fromCreationSource(source: CreationSource?): String? {
        return source?.name
    }
    
    @TypeConverter
    fun toCreationSource(sourceName: String?): CreationSource? {
        return sourceName?.let { CreationSource.valueOf(it) }
    }
}
