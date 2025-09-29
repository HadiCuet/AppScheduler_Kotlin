package com.hadi.appscheduler.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadi.appscheduler.data.Schedule
import com.hadi.appscheduler.data.ScheduleStatus
import com.hadi.appscheduler.ui.ScheduleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExecutionLogsViewModel(
    private val repository: ScheduleRepository
) : ViewModel() {
    
    private val _filterState = MutableStateFlow(LogFilter.ALL)
    val filterState: StateFlow<LogFilter> = _filterState.asStateFlow()
    
    val logs = repository.getExecutionLogs()
        .combine(filterState) { allLogs, filter ->
            when (filter) {
                LogFilter.ALL -> allLogs
                LogFilter.SUCCESS -> allLogs.filter { 
                    it.status in listOf(ScheduleStatus.LAUNCHED_CONFIRMED, ScheduleStatus.LAUNCH_INTENT_SENT) 
                }
                LogFilter.FAILED -> allLogs.filter { 
                    it.status in listOf(ScheduleStatus.MISSED, ScheduleStatus.CANCELLED) 
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun setFilter(filter: LogFilter) {
        _filterState.value = filter
    }
}

enum class LogFilter(val displayName: String) {
    ALL("All"),
    SUCCESS("Success"),
    FAILED("Failed")
}