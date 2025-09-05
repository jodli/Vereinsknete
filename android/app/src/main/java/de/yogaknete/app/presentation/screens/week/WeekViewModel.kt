package de.yogaknete.app.presentation.screens.week

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.yogaknete.app.core.utils.DateUtils
import de.yogaknete.app.data.local.YogaClassDao
import de.yogaknete.app.domain.model.ClassStatus
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.model.YogaClass
import de.yogaknete.app.domain.repository.StudioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

data class WeekViewState(
    val currentWeekStart: LocalDate = DateUtils.getWeekStart(),
    val currentWeekEnd: LocalDate = DateUtils.getWeekEnd(),
    val weekDays: List<LocalDate> = DateUtils.getWeekDays(),
    val classes: Map<LocalDate, List<YogaClass>> = emptyMap(),
    val studios: List<Studio> = emptyList(),
    val totalClassesThisWeek: Int = 0,
    val totalHoursThisWeek: Double = 0.0,
    val isLoading: Boolean = false,
    val showAddClassDialog: Boolean = false,
    val selectedClass: YogaClass? = null
)

@HiltViewModel
class WeekViewModel @Inject constructor(
    private val yogaClassDao: YogaClassDao,
    private val studioRepository: StudioRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(WeekViewState())
    val state: StateFlow<WeekViewState> = _state.asStateFlow()
    
    init {
        loadCurrentWeek()
        observeStudios()
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
                    
                    // Calculate totals
                    val totalClasses = classList.count { it.status != ClassStatus.CANCELLED }
                    val totalHours = classList
                        .filter { it.status != ClassStatus.CANCELLED }
                        .sumOf { it.durationHours }
                    
                    _state.update { currentState ->
                        currentState.copy(
                            classes = classesByDate,
                            totalClassesThisWeek = totalClasses,
                            totalHoursThisWeek = totalHours
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
    
    fun deleteClass(yogaClass: YogaClass) {
        viewModelScope.launch {
            yogaClassDao.deleteClass(yogaClass)
            clearSelectedClass()
        }
    }
}
