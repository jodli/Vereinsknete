package de.yogaknete.app.presentation.screens.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.yogaknete.app.data.local.entities.ClassTemplate
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.model.YogaClass
import de.yogaknete.app.domain.repository.ClassTemplateRepository
import de.yogaknete.app.domain.repository.StudioRepository
import de.yogaknete.app.domain.repository.YogaClassRepository
import de.yogaknete.app.domain.usecase.AutoScheduleManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

data class TemplateUiState(
    val templates: List<ClassTemplate> = emptyList(),
    val studios: List<Studio> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTemplate: ClassTemplate? = null,
    val showTemplateDialog: Boolean = false,
    val showQuickAddDialog: Boolean = false,
    val quickAddDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
)

@HiltViewModel
class TemplateViewModel @Inject constructor(
    private val templateRepository: ClassTemplateRepository,
    private val studioRepository: StudioRepository,
    private val yogaClassRepository: YogaClassRepository,
    private val autoScheduleManager: AutoScheduleManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TemplateUiState())
    val uiState: StateFlow<TemplateUiState> = _uiState.asStateFlow()
    
    init {
        loadTemplates()
        loadStudios()
    }
    
    private fun loadTemplates() {
        viewModelScope.launch {
            templateRepository.getAllTemplates().collect { templates ->
                _uiState.update { it.copy(templates = templates) }
            }
        }
    }
    
    private fun loadStudios() {
        viewModelScope.launch {
            studioRepository.getAllActiveStudios().collect { studios ->
                _uiState.update { it.copy(studios = studios) }
            }
        }
    }
    
    fun createTemplate(
        name: String,
        studioId: Long,
        className: String,
        dayOfWeek: DayOfWeek,
        startTime: LocalTime,
        duration: Double = 1.25
    ) {
        viewModelScope.launch {
            val endTime = startTime.toSecondOfDay()
                .plus((duration * 60 * 60).toInt())
                .let { LocalTime.fromSecondOfDay(it) }
            
            val template = ClassTemplate(
                name = name,
                studioId = studioId,
                className = className,
                dayOfWeek = dayOfWeek,
                startTime = startTime,
                endTime = endTime,
                duration = duration
            )
            
            templateRepository.createTemplate(template)
            _uiState.update { it.copy(showTemplateDialog = false) }
        }
    }
    
    fun updateTemplate(template: ClassTemplate) {
        viewModelScope.launch {
            templateRepository.updateTemplate(template)
            _uiState.update { it.copy(showTemplateDialog = false, selectedTemplate = null) }
        }
    }
    
    fun deleteTemplate(template: ClassTemplate) {
        viewModelScope.launch {
            templateRepository.deleteTemplate(template)
        }
    }
    
    fun toggleTemplateActive(template: ClassTemplate) {
        viewModelScope.launch {
            templateRepository.setTemplateActive(template.id, !template.isActive)
        }
    }
    
    fun toggleAutoSchedule(templateId: Long, enabled: Boolean) {
        viewModelScope.launch {
            templateRepository.updateAutoSchedule(templateId, enabled)
            
            // If enabling auto-schedule, immediately schedule upcoming classes
            if (enabled) {
                autoScheduleManager.autoScheduleOnTemplateEnabled(templateId)
            }
        }
    }
    
    fun showTemplateDialog(template: ClassTemplate? = null) {
        _uiState.update { 
            it.copy(
                showTemplateDialog = true,
                selectedTemplate = template
            )
        }
    }
    
    fun hideTemplateDialog() {
        _uiState.update { 
            it.copy(
                showTemplateDialog = false,
                selectedTemplate = null
            )
        }
    }
    
    fun showQuickAddDialog(date: LocalDate) {
        _uiState.update { 
            it.copy(
                showQuickAddDialog = true,
                quickAddDate = date
            )
        }
    }
    
    fun hideQuickAddDialog() {
        _uiState.update { 
            it.copy(showQuickAddDialog = false)
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
            
            val startDateTime = LocalDateTime(date, startTime)
            val endDateTime = startDateTime.toInstant(TimeZone.currentSystemDefault())
                .plus((duration * 60 * 60).toInt(), DateTimeUnit.SECOND)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            
            val yogaClass = YogaClass(
                studioId = template.studioId,
                title = template.className,
                startTime = startDateTime,
                endTime = endDateTime,
                durationHours = duration,
                status = de.yogaknete.app.domain.model.ClassStatus.SCHEDULED,
                creationSource = de.yogaknete.app.domain.model.CreationSource.TEMPLATE,
                sourceTemplateId = template.id
            )
            
            yogaClassRepository.addClass(yogaClass)
            hideQuickAddDialog()
        }
    }
    
    fun getTemplatesForDay(dayOfWeek: DayOfWeek): List<ClassTemplate> {
        return uiState.value.templates.filter { 
            it.dayOfWeek == dayOfWeek && it.isActive 
        }
    }
    
    fun getActiveTemplates(): List<ClassTemplate> {
        return uiState.value.templates.filter { it.isActive }
    }
}
