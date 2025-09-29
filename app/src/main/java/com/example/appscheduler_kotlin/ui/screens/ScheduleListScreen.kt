package com.example.appscheduler_kotlin.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appscheduler_kotlin.R
import com.example.appscheduler_kotlin.data.Schedule
import com.example.appscheduler_kotlin.data.ScheduleStatus
import com.example.appscheduler_kotlin.repo.SchedulesRepository
import com.example.appscheduler_kotlin.repo.SchedulesRepository as RepoFactory
import com.example.appscheduler_kotlin.ui.PermissionHelpers
import com.example.appscheduler_kotlin.util.Formatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
fun ScheduleListScreen(
    onCreateNew: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val vm = remember { ScheduleListViewModel(RepoFactory(context)) }

    val schedules by vm.schedules.collectAsState()
    val snack = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.label_schedules)) })
        },
        snackbarHost = { SnackbarHost(snack) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateNew) {
                Text("+")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            PermissionWarnings()

            if (schedules.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.label_no_schedules))
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(schedules) { s ->
                        ScheduleRow(
                            schedule = s,
                            onEdit = { onEdit(s.id) },
                            onCancel = { vm.cancel(s.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionWarnings() {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !PermissionHelpers.canScheduleExactAlarms(context)) {
            AssistChip(
                onClick = { PermissionHelpers.openExactAlarmSettings(context) },
                label = { Text(stringResource(R.string.permission_exact_alarm_needed)) }
            )
        }
    }
}

@Composable
private fun ScheduleRow(
    schedule: Schedule,
    onEdit: () -> Unit,
    onCancel: () -> Unit
) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = schedule.packageName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(schedule.status)
            }
            Spacer(Modifier.height(6.dp))
            Row {
                Text("${stringResource(R.string.label_when)}: ${Formatters.formatDateTime(schedule.triggerAtMillis)}")
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) { Text(stringResource(R.string.action_edit)) }
                OutlinedButton(onClick = onCancel) { Text(stringResource(R.string.action_cancel)) }
            }
        }
    }
}

@Composable
private fun StatusChip(status: ScheduleStatus) {
    val color = when (status) {
        ScheduleStatus.SCHEDULED -> MaterialTheme.colorScheme.primary
        ScheduleStatus.FIRED -> MaterialTheme.colorScheme.tertiary
        ScheduleStatus.LAUNCH_INTENT_SENT -> MaterialTheme.colorScheme.secondary
        ScheduleStatus.CANCELLED -> MaterialTheme.colorScheme.error
        ScheduleStatus.MISSED -> MaterialTheme.colorScheme.error
    }
    AssistChip(
        onClick = { },
        label = { Text("${stringResource(R.string.label_status)}: $status") },
        colors = AssistChipDefaults.assistChipColors(containerColor = color.copy(alpha = 0.2f))
    )
}

class ScheduleListViewModel(private val repo: SchedulesRepository) : ViewModel() {
    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeAll().collect { _schedules.value = it }
        }
    }

    fun cancel(id: Long) {
        viewModelScope.launch { repo.cancel(id) }
    }
}