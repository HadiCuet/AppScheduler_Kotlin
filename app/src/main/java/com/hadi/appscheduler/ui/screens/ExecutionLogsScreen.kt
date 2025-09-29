package com.hadi.appscheduler.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hadi.appscheduler.R
import com.hadi.appscheduler.data.Schedule
import com.hadi.appscheduler.data.ScheduleStatus
import com.hadi.appscheduler.ui.ScheduleRepository
import com.hadi.appscheduler.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutionLogsScreen(
    repository: ScheduleRepository
) {
    val viewModel: ExecutionLogsViewModel = viewModel { ExecutionLogsViewModel(repository) }
    val logs by viewModel.logs.collectAsState()
    val currentFilter by viewModel.filterState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.execution_logs)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips
            FilterChips(
                currentFilter = currentFilter,
                onFilterSelected = { viewModel.setFilter(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            if (logs.isEmpty()) {
                EmptyLogsState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs) { log ->
                        LogCard(schedule = log)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChips(
    currentFilter: LogFilter,
    onFilterSelected: (LogFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LogFilter.values().forEach { filter ->
            FilterChip(
                selected = filter == currentFilter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.displayName) }
            )
        }
    }
}

@Composable
fun EmptyLogsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_logs),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LogCard(schedule: Schedule) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Scheduled: ${DateTimeUtils.formatDateTime(schedule.triggerAtMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Updated: ${DateTimeUtils.formatDateTime(schedule.updatedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    schedule.lastVerificationAt?.let { verificationTime ->
                        Text(
                            text = "Verified: ${DateTimeUtils.formatDateTime(verificationTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                LogStatusIcon(status = schedule.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status chip
            StatusChip(status = schedule.status)
            
            // Result message
            schedule.resultMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun LogStatusIcon(status: ScheduleStatus) {
    val (icon, color) = when (status) {
        ScheduleStatus.LAUNCHED_CONFIRMED -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
        ScheduleStatus.LAUNCH_INTENT_SENT -> Icons.Default.Send to MaterialTheme.colorScheme.secondary
        ScheduleStatus.FIRED -> Icons.Default.Alarm to MaterialTheme.colorScheme.tertiary
        ScheduleStatus.CANCELLED -> Icons.Default.Cancel to MaterialTheme.colorScheme.outline
        ScheduleStatus.MISSED -> Icons.Default.Error to MaterialTheme.colorScheme.error
        ScheduleStatus.SCHEDULED -> Icons.Default.Schedule to MaterialTheme.colorScheme.primary
    }
    
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(32.dp)
    )
}