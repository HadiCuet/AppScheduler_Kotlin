@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.appscheduler_kotlin.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appscheduler_kotlin.util.TimePresets
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeSection(
    triggerAtMillis: Long?,
    onPresetSelected: (TimePresets.Preset) -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    hasConflict: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPastTime = triggerAtMillis?.let { TimePresets.isPastTime(it) } ?: false
    val hasError = isPastTime || hasConflict
    
    // Animated colors based on error state
    val cardColor by animateColorAsState(
        targetValue = if (hasError) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        label = "cardColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (hasError) {
            MaterialTheme.colorScheme.error
        } else {
            Color.Transparent
        },
        label = "borderColor"
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Header
            Text(
                text = "Date & Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            // Quick Presets
            Text(
                text = "Quick presets",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(TimePresets.Preset.values()) { preset ->
                    AssistChip(
                        onClick = { onPresetSelected(preset) },
                        label = { Text(preset.label) },
                        modifier = Modifier.semantics {
                            contentDescription = "Quick preset: ${preset.label}"
                        }
                    )
                }
            }

            // Date and Time Selectors
            val (dateText, timeText) = TimePresets.formatTimeForChip(triggerAtMillis)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date Button
                InputChip(
                    onClick = onDateClick,
                    label = { Text(dateText) },
                    selected = triggerAtMillis != null,
                    leadingIcon = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Date",
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = "Select date: $dateText"
                        }
                )

                // Time Button  
                InputChip(
                    onClick = onTimeClick,
                    label = { Text(timeText) },
                    selected = triggerAtMillis != null,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Time",
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = "Select time: $timeText"
                        }
                )
            }

            // Human-friendly summary
            if (triggerAtMillis != null) {
                val summary = TimePresets.getHumanFriendlySummary(triggerAtMillis)
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    fontWeight = if (!hasError) FontWeight.Medium else FontWeight.Normal,
                    modifier = Modifier.animateContentSize()
                )
            }

            // Error Messages
            when {
                isPastTime -> {
                    Text(
                        text = "Time must be in the future.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.animateContentSize()
                    )
                }
                hasConflict -> {
                    Text(
                        text = "A schedule already exists at this time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.animateContentSize()
                    )
                }
            }
        }
    }
}

// Simplified version for preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickers(
    showDatePicker: Boolean,
    showTimePicker: Boolean,
    onDateSelected: (Long) -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
    onDatePickerDismiss: () -> Unit,
    onTimePickerDismiss: () -> Unit,
    currentTimeMillis: Long? = null
) {
    // Date Picker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentTimeMillis ?: System.currentTimeMillis()
        )
        
        ModalBottomSheet(
            onDismissRequest = onDatePickerDismiss
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Date",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDatePickerDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { selectedDate ->
                                onDateSelected(selectedDate)
                            }
                            onDatePickerDismiss()
                        }
                    ) {
                        Text("OK")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Time Picker
    if (showTimePicker) {
        val cal = Calendar.getInstance().apply {
            if (currentTimeMillis != null) {
                timeInMillis = currentTimeMillis
            }
        }
        
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true
        )
        
        ModalBottomSheet(
            onDismissRequest = onTimePickerDismiss
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Time",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onTimePickerDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            onTimeSelected(timePickerState.hour, timePickerState.minute)
                            onTimePickerDismiss()
                        }
                    ) {
                        Text("OK")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}