package com.hadi.appscheduler.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadi.appscheduler.data.Schedule
import com.hadi.appscheduler.ui.AppInfo
import com.hadi.appscheduler.ui.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class CreateEditScheduleViewModel(
    private val repository: ScheduleRepository,
    private val scheduleId: Long? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CreateEditScheduleUiState())
    val uiState: StateFlow<CreateEditScheduleUiState> = _uiState.asStateFlow()
    
    init {
        if (scheduleId != null) {
            loadSchedule(scheduleId)
        }
    }
    
    private fun loadSchedule(id: Long) {
        viewModelScope.launch {
            val schedule = repository.getScheduleById(id)
            if (schedule != null) {
                _uiState.value = _uiState.value.copy(
                    isEdit = true,
                    selectedApp = AppInfo(schedule.packageName, schedule.appName, null), // Drawable will be loaded in UI
                    selectedDateTime = schedule.triggerAtMillis,
                    loading = false
                )
            }
        }
    }
    
    fun selectApp(appInfo: AppInfo) {
        _uiState.value = _uiState.value.copy(
            selectedApp = appInfo,
            appError = null
        )
    }
    
    fun selectDateTime(dateTimeMillis: Long) {
        _uiState.value = _uiState.value.copy(
            selectedDateTime = dateTimeMillis,
            dateTimeError = null
        )
    }
    
    fun saveSchedule() {
        val state = _uiState.value
        
        // Validation
        if (state.selectedApp == null) {
            _uiState.value = state.copy(appError = "Please select an app")
            return
        }
        
        if (state.selectedDateTime == null || state.selectedDateTime <= System.currentTimeMillis()) {
            _uiState.value = state.copy(dateTimeError = "Please select a future date and time")
            return
        }
        
        _uiState.value = state.copy(loading = true, error = null)
        
        viewModelScope.launch {
            if (state.isEdit && scheduleId != null) {
                // Update existing schedule
                repository.updateSchedule(scheduleId, state.selectedDateTime)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            loading = false,
                            saveSuccess = true
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            loading = false,
                            error = error.message ?: "Failed to update schedule"
                        )
                    }
            } else {
                // Create new schedule
                repository.createSchedule(
                    packageName = state.selectedApp.packageName,
                    appName = state.selectedApp.appName,
                    triggerAtMillis = state.selectedDateTime
                )
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            loading = false,
                            saveSuccess = true
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            loading = false,
                            error = error.message ?: "Failed to create schedule"
                        )
                    }
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, appError = null, dateTimeError = null)
    }
}

data class CreateEditScheduleUiState(
    val isEdit: Boolean = false,
    val selectedApp: AppInfo? = null,
    val selectedDateTime: Long? = null,
    val loading: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val appError: String? = null,
    val dateTimeError: String? = null
)