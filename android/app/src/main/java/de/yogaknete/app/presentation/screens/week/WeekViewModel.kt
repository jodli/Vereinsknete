package de.yogaknete.app.presentation.screens.week

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.yogaknete.app.core.utils.DateUtils
import de.yogaknete.app.data.local.YogaClassDao
import de.yogaknete.app.data.local.entities.ClassTemplate
import de.yogaknete.app.domain.model.ClassStatus
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.model.YogaClass
import de.yogaknete.app.domain.repository.ClassTemplateRepository
import de.yogaknete.app.domain.repository.StudioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

private fun Month.days(year: Int): Int = when (this) {
    Month.FEBRUARY -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
    else -> 31
}

data class WeekViewState(
    val currentWeekStart: LocalDate = DateUtils.getWeekStart(),
    val currentWeekEnd: LocalDate = DateUtils.getWeekEnd(),
    val weekDays: List<LocalDate> = DateUtils.getWeekDays(),
    val classes: Map<LocalDate, List<YogaClass>> = emptyMap(),
    val studios: List<Studio> = emptyList(),
    val templates: List<ClassTemplate> = emptyList(),
    val totalClassesThisWeek: Int = 0,
    val totalHoursThisWeek: Double = 0.0,
    val earningsPerStudio: Map<Long, Double> = emptyMap(), // Studio ID to earnings
    val totalEarningsThisWeek: Double = 0.0,
    val isLoading: Boolean = false,
    val showAddClassDialog: Boolean = false,
    val showQuickAddDialog: Boolean = false,
    val quickAddDate: LocalDate? = null,
    val selectedClass: YogaClass? = null,
    val showBulkCancelDialog: Boolean = false,
    val showEditClassDialog: Boolean = false,
    val showWeekStats: Boolean = false,
    val showMonthlyStats: Boolean = false,
    val monthlyStatsMonth: Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).monthNumber,
    val monthlyStatsYear: Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).year,
    val monthlyTotalClasses: Int = 0,
    val monthlyTotalHours: Double = 0.0,
    val monthlyTotalEarnings: Double = 0.0,
    val monthlyEarningsPerStudio: Map<Long, Double> = emptyMap()
)

@HiltViewModel
class WeekViewModel @Inject constructor(
    private val yogaClassDao: YogaClassDao,
    private val studioRepository: StudioRepository,
    private val classTemplateRepository: ClassTemplateRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(WeekViewState())
    val state: StateFlow<WeekViewState> = _state.asStateFlow()
    
    init {
        loadCurrentWeek()
        observeStudios()
        observeTemplates()
    }
    
    private fun loadCurrentWeek() {
        viewModelScope.launch {
            val weekStart = _state.value.currentWeekStart
            val weekEnd = _state.value.currentWeekEnd
            
            // Convert to LocalDateTime for the query
            val startDateTime = weekStart.atTime(0, 0)
            val endDateTime = weekEnd.atTime(23, 59)
            
            yogaClassDao.getClassesInRange(startDateTime, endDateTime)
                .collect { classList ->
                    // Group classes by date
                    val classesByDate = classList.groupBy { yogaClass ->
                        yogaClass.startTime.date
                    }
                    
                    // Calculate totals - only count COMPLETED classes for statistics
                    val completedClasses = classList.filter { it.status == ClassStatus.COMPLETED }
                    val totalClasses = completedClasses.size
                    val totalHours = completedClasses.sumOf { it.durationHours }
                    
                    // Calculate earnings per studio
                    val studios = _state.value.studios
                    val earningsPerStudio = completedClasses
                        .groupBy { it.studioId }
                        .mapValues { (studioId, classes) ->
                            val studio = studios.find { it.id == studioId }
                            val hourlyRate = studio?.hourlyRate ?: 0.0
                            classes.sumOf { it.durationHours * hourlyRate }
                        }
                    
                    val totalEarnings = earningsPerStudio.values.sum()
                    
                    _state.update { currentState ->
                        currentState.copy(
                            classes = classesByDate,
                            totalClassesThisWeek = totalClasses,
                            totalHoursThisWeek = totalHours,
                            earningsPerStudio = earningsPerStudio,
                            totalEarningsThisWeek = totalEarnings
                        )
                    }
                }
        }
    }
    
    private fun observeStudios() {
        viewModelScope.launch {
            studioRepository.getAllActiveStudios()
                .collect { studioList ->
                    _state.update { it.copy(studios = studioList) }
                }
        }
    }
    
    private fun observeTemplates() {
        viewModelScope.launch {
            classTemplateRepository.getAllActiveTemplates()
                .collect { templates ->
                    _state.update { it.copy(templates = templates) }
                }
        }
    }
    
    fun navigateToPreviousWeek() {
        val newWeekStart = _state.value.currentWeekStart.minus(DatePeriod(days = 7 * 1))
        val newWeekEnd = DateUtils.getWeekEnd(newWeekStart)
        val newWeekDays = DateUtils.getWeekDays(newWeekStart)
        
        _state.update { 
            it.copy(
                currentWeekStart = newWeekStart,
                currentWeekEnd = newWeekEnd,
                weekDays = newWeekDays
            )
        }
        
        loadCurrentWeek()
    }
    
    fun navigateToNextWeek() {
        val newWeekStart = _state.value.currentWeekStart.plus(DatePeriod(days = 7 * 1))
        val newWeekEnd = DateUtils.getWeekEnd(newWeekStart)
        val newWeekDays = DateUtils.getWeekDays(newWeekStart)
        
        _state.update { 
            it.copy(
                currentWeekStart = newWeekStart,
                currentWeekEnd = newWeekEnd,
                weekDays = newWeekDays
            )
        }
        
        loadCurrentWeek()
    }
    
    fun navigateToToday() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val newWeekStart = DateUtils.getWeekStart(today)
        val newWeekEnd = DateUtils.getWeekEnd(today)
        val newWeekDays = DateUtils.getWeekDays(today)
        
        _state.update { 
            it.copy(
                currentWeekStart = newWeekStart,
                currentWeekEnd = newWeekEnd,
                weekDays = newWeekDays
            )
        }
        
        loadCurrentWeek()
    }
    
    fun showAddClassDialog() {
        _state.update { it.copy(showAddClassDialog = true) }
    }
    
    fun hideAddClassDialog() {
        _state.update { it.copy(showAddClassDialog = false) }
    }
    
    fun showQuickAddDialogForDate(date: LocalDate) {
        _state.update { it.copy(showQuickAddDialog = true, quickAddDate = date) }
    }
    
    fun hideQuickAddDialog() {
        _state.update { it.copy(showQuickAddDialog = false, quickAddDate = null) }
    }
    
    fun addClass(
        studioId: Long,
        title: String,
        date: LocalDate,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ) {
        viewModelScope.launch {
            val startTime = date.atTime(startHour, startMinute)
            val endTime = date.atTime(endHour, endMinute)
            val duration = DateUtils.calculateDurationHours(startTime, endTime)
            
            val newClass = YogaClass(
                studioId = studioId,
                title = title,
                startTime = startTime,
                endTime = endTime,
                durationHours = duration,
                status = ClassStatus.SCHEDULED
            )
            
            yogaClassDao.insertClass(newClass)
            hideAddClassDialog()
        }
    }
    
    fun selectClass(yogaClass: YogaClass) {
        _state.update { it.copy(selectedClass = yogaClass) }
    }
    
    fun clearSelectedClass() {
        _state.update { it.copy(selectedClass = null) }
    }
    
    fun markClassAsCompleted(yogaClass: YogaClass) {
        viewModelScope.launch {
            yogaClassDao.updateClassStatus(yogaClass.id, ClassStatus.COMPLETED)
            clearSelectedClass()
        }
    }
    
    fun markClassAsCancelled(yogaClass: YogaClass) {
        viewModelScope.launch {
            yogaClassDao.updateClassStatus(yogaClass.id, ClassStatus.CANCELLED)
            clearSelectedClass()
        }
    }

    fun showBulkCancelDialog() {
        _state.update { it.copy(showBulkCancelDialog = true) }
    }

    fun hideBulkCancelDialog() {
        _state.update { it.copy(showBulkCancelDialog = false) }
    }

    fun bulkCancelClasses(ids: List<Long>) {
        if (ids.isEmpty()) {
            hideBulkCancelDialog()
            return
        }
        viewModelScope.launch {
            yogaClassDao.updateClassesStatus(ids, ClassStatus.CANCELLED)
            hideBulkCancelDialog()
        }
    }
    
    fun deleteClass(yogaClass: YogaClass) {
        viewModelScope.launch {
            yogaClassDao.deleteClass(yogaClass)
            clearSelectedClass()
        }
    }

    fun showEditClassDialog(yogaClass: YogaClass) {
        _state.update { it.copy(selectedClass = yogaClass, showEditClassDialog = true) }
    }

    fun hideEditClassDialog() {
        _state.update { it.copy(showEditClassDialog = false) }
    }

    fun updateClassSchedule(
        yogaClassId: Long,
        newDate: LocalDate,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ) {
        viewModelScope.launch {
            val existing = yogaClassDao.getClassById(yogaClassId) ?: return@launch
            val newStart = newDate.atTime(startHour, startMinute)
            val newEnd = newDate.atTime(endHour, endMinute)
            val newDuration = DateUtils.calculateDurationHours(newStart, newEnd)
            val updated = existing.copy(
                startTime = newStart,
                endTime = newEnd,
                durationHours = newDuration
            )
            yogaClassDao.updateClass(updated)
            hideEditClassDialog()
            clearSelectedClass()
        }
    }
    
    fun createClassFromTemplate(
        template: ClassTemplate,
        date: LocalDate,
        startTimeOverride: LocalTime? = null,
        durationOverride: Double? = null
    ) {
        viewModelScope.launch {
            val startTime = startTimeOverride ?: template.startTime
            val duration = durationOverride ?: template.duration
            
            val startDateTime = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, startTime.hour, startTime.minute)
            val endDateTime = startDateTime
                .toInstant(TimeZone.currentSystemDefault())
                .plus((duration * 60 * 60).toInt(), DateTimeUnit.SECOND)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            
            val newClass = YogaClass(
                studioId = template.studioId,
                title = template.className,
                startTime = startDateTime,
                endTime = endDateTime,
                durationHours = duration,
                status = ClassStatus.SCHEDULED
            )
            
            yogaClassDao.insertClass(newClass)
            hideQuickAddDialog()
        }
    }
    
    fun showWeekStats() {
        _state.update { it.copy(showWeekStats = true) }
    }
    
    fun hideWeekStats() {
        _state.update { it.copy(showWeekStats = false) }
    }
    
    fun loadMonthlyStats(
        month: Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).monthNumber,
        year: Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
    ) {
        viewModelScope.launch {
            // Get first and last day of the month
            val firstDay = LocalDate(year, month, 1)
            val lastDay = LocalDate(year, month, firstDay.month.days(year))
            
            val startDateTime = firstDay.atTime(0, 0)
            val endDateTime = lastDay.atTime(23, 59)
            
            yogaClassDao.getClassesInRange(startDateTime, endDateTime)
                .first() // Get first emission for the calculation
                .let { classList ->
                    val completedClasses = classList.filter { it.status == ClassStatus.COMPLETED }
                    val totalClasses = completedClasses.size
                    val totalHours = completedClasses.sumOf { it.durationHours }
                    
                    // Calculate earnings per studio
                    val studios = _state.value.studios
                    val earningsPerStudio = completedClasses
                        .groupBy { it.studioId }
                        .mapValues { (studioId, classes) ->
                            val studio = studios.find { it.id == studioId }
                            val hourlyRate = studio?.hourlyRate ?: 0.0
                            classes.sumOf { it.durationHours * hourlyRate }
                        }
                    
                    val totalEarnings = earningsPerStudio.values.sum()
                    
                    // Update state with monthly stats
                    _state.update {
                        it.copy(
                            monthlyStatsMonth = month,
                            monthlyStatsYear = year,
                            monthlyTotalClasses = totalClasses,
                            monthlyTotalHours = totalHours,
                            monthlyTotalEarnings = totalEarnings,
                            monthlyEarningsPerStudio = earningsPerStudio,
                            showMonthlyStats = true
                        )
                    }
                }
        }
    }
    
    fun showMonthlyStats() {
        loadMonthlyStats()
    }
    
    fun hideMonthlyStats() {
        _state.update { it.copy(showMonthlyStats = false) }
    }
}
