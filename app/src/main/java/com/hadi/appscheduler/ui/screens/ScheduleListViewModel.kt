package com.hadi.appscheduler.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadi.appscheduler.data.Schedule
import com.hadi.appscheduler.data.ScheduleStatus
import com.hadi.appscheduler.ui.ScheduleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScheduleListViewModel(
    private val repository: ScheduleRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ScheduleListUiState())
    val uiState: StateFlow<ScheduleListUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.getAllSchedules().collect { schedules ->
                _uiState.value = _uiState.value.copy(
                    schedules = schedules,
                    loading = false
                )
            }
        }
    }
    
    fun cancelSchedule(scheduleId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            repository.cancelSchedule(scheduleId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        message = "Schedule cancelled successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = error.message ?: "Failed to cancel schedule"
                    )
                }
        }
    }
    
    fun deleteSchedule(scheduleId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            repository.deleteSchedule(scheduleId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        message = "Schedule deleted successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = error.message ?: "Failed to delete schedule"
                    )
                }
        }
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
}

data class ScheduleListUiState(
    val schedules: List<Schedule> = emptyList(),
    val loading: Boolean = true,
    val message: String? = null,
    val error: String? = null
)