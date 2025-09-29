package com.hadi.appscheduler.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hadi.appscheduler.R
import com.hadi.appscheduler.data.Schedule
import com.hadi.appscheduler.data.ScheduleStatus
import com.hadi.appscheduler.ui.AppListProvider
import com.hadi.appscheduler.ui.ScheduleRepository
import com.hadi.appscheduler.util.DateTimeUtils
import com.hadi.appscheduler.util.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleListScreen(
    repository: ScheduleRepository,
    appListProvider: AppListProvider,
    onRequestExactAlarmPermission: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: ScheduleListViewModel = viewModel { ScheduleListViewModel(repository) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Check permissions
    val canScheduleExactAlarms = PermissionHelper.canScheduleExactAlarms(context)
    
    LaunchedEffect(Unit) {
        if (!canScheduleExactAlarms) {
            onRequestExactAlarmPermission()
        }
    }
    
    // Show snackbar for messages/errors
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            // In a real app, you'd show a snackbar here
            viewModel.clearMessage()
        }
    }
    
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // In a real app, you'd show an error snackbar here
            viewModel.clearMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_schedules)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (canScheduleExactAlarms) {
                        navController.navigate("create_schedule")
                    } else {
                        onRequestExactAlarmPermission()
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create_schedule))
            }
        }
    ) { paddingValues ->
        if (uiState.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.schedules.isEmpty()) {
            EmptySchedulesState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Group schedules by status for better organization
                val upcomingSchedules = uiState.schedules.filter { it.status == ScheduleStatus.SCHEDULED }
                val completedSchedules = uiState.schedules.filter { 
                    it.status in listOf(
                        ScheduleStatus.FIRED, 
                        ScheduleStatus.LAUNCH_INTENT_SENT, 
                        ScheduleStatus.LAUNCHED_CONFIRMED,
                        ScheduleStatus.CANCELLED,
                        ScheduleStatus.MISSED
                    ) 
                }
                
                if (upcomingSchedules.isNotEmpty()) {
                    item {
                        Text(
                            text = "Upcoming Schedules",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(upcomingSchedules) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onEdit = { navController.navigate("edit_schedule/${schedule.id}") },
                            onCancel = { viewModel.cancelSchedule(schedule.id) },
                            onDelete = { viewModel.deleteSchedule(schedule.id) }
                        )
                    }
                }
                
                if (completedSchedules.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Activity",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(completedSchedules.take(5)) { schedule -> // Show only recent 5
                        ScheduleCard(
                            schedule = schedule,
                            onEdit = null, // Can't edit completed schedules
                            onCancel = null,
                            onDelete = { viewModel.deleteSchedule(schedule.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySchedulesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Schedule,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_schedules),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.no_schedules_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleCard(
    schedule: Schedule,
    onEdit: (() -> Unit)?,
    onCancel: (() -> Unit)?,
    onDelete: () -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }
    
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
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = DateTimeUtils.formatDateTime(schedule.triggerAtMillis),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (schedule.status == ScheduleStatus.SCHEDULED) {
                        val timeUntil = DateTimeUtils.getTimeUntilString(schedule.triggerAtMillis)
                        Text(
                            text = stringResource(R.string.time_until, timeUntil),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Box {
                    IconButton(onClick = { showDropdown = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        onEdit?.let {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.edit)) },
                                onClick = {
                                    showDropdown = false
                                    it()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                        }
                        
                        onCancel?.let {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.cancel_schedule)) },
                                onClick = {
                                    showDropdown = false
                                    it()
                                },
                                leadingIcon = { Icon(Icons.Default.Cancel, contentDescription = null) }
                            )
                        }
                        
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete)) },
                            onClick = {
                                showDropdown = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status chip
            StatusChip(status = schedule.status)
            
            // Result message if available
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
fun StatusChip(status: ScheduleStatus) {
    val (text, color) = when (status) {
        ScheduleStatus.SCHEDULED -> stringResource(R.string.status_scheduled) to MaterialTheme.colorScheme.primary
        ScheduleStatus.FIRED -> stringResource(R.string.status_fired) to MaterialTheme.colorScheme.secondary
        ScheduleStatus.LAUNCH_INTENT_SENT -> stringResource(R.string.status_launch_intent_sent) to MaterialTheme.colorScheme.tertiary
        ScheduleStatus.LAUNCHED_CONFIRMED -> stringResource(R.string.status_launched_confirmed) to MaterialTheme.colorScheme.primary
        ScheduleStatus.CANCELLED -> stringResource(R.string.status_cancelled) to MaterialTheme.colorScheme.outline
        ScheduleStatus.MISSED -> stringResource(R.string.status_missed) to MaterialTheme.colorScheme.error
    }
    
    AssistChip(
        onClick = { },
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = color
        )
    )
}