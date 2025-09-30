@file:OptIn(ExperimentalMaterial3Api::class) // File-level OptIn to cover all M3 experimental APIs

package com.example.appscheduler_kotlin.ui.screens

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appscheduler_kotlin.R
import com.example.appscheduler_kotlin.data.AppDatabase
import com.example.appscheduler_kotlin.repo.SchedulesRepository
import com.example.appscheduler_kotlin.repo.SchedulesRepository as RepoFactory
import com.example.appscheduler_kotlin.ui.PermissionHelpers
import com.example.appscheduler_kotlin.ui.components.*
import com.example.appscheduler_kotlin.util.InstalledApps
import com.example.appscheduler_kotlin.util.TimePresets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleScreen(
    scheduleId: Long?,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val repo = remember { RepoFactory(context) }
    val vm = remember { EditScheduleViewModel(repo, AppDatabase.get(context).scheduleDao()) }

    LaunchedEffect(scheduleId) {
        vm.load(scheduleId)
    }

    val state by vm.state.collectAsState()
    val uiState by vm.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Recent apps (simple in-memory storage for this demo)
    var recentApps by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (scheduleId == null) "New schedule" else "Edit schedule",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        state.selectedAppLabel?.let { appLabel ->
                            Text(
                                text = appLabel,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onDone,
                        modifier = Modifier.semantics {
                            contentDescription = "Go back"
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            )
        },
        bottomBar = {
            BottomActionBar(
                onSave = {
                    // Permission check
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        !PermissionHelpers.canScheduleExactAlarms(context)) {
                        vm.setError("Exact alarm permission is required for precise scheduling.")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } else {
                        vm.saveOrUpdate(
                            onSuccess = {
                                // Add to recent apps
                                state.packageName?.let { pkg ->
                                    recentApps = (listOf(pkg) + recentApps.filter { it != pkg }).take(3)
                                }
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDone()
                            },
                            onError = { msg ->
                                vm.setError(msg)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                    }
                },
                onCancel = onDone,
                saveEnabled = state.canSave,
                isLoading = uiState.isSaving
            )
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Permission Banner
            PermissionBanner()

            // App Selection Card
            AppSelectionCard(
                selectedApp = state.packageName to state.selectedAppLabel,
                onClick = { vm.setAppPickerVisible(true) },
                modifier = Modifier.fillMaxWidth()
            )

            // Date & Time Card
            DateTimeSection(
                triggerAtMillis = state.triggerAtMillis,
                onPresetSelected = { preset ->
                    val newTime = TimePresets.applyPreset(preset, state.triggerAtMillis ?: System.currentTimeMillis())
                    vm.setDateTime(newTime)
                },
                onDateClick = { vm.setDatePickerVisible(true) },
                onTimeClick = { vm.setTimePickerVisible(true) },
                hasConflict = uiState.hasConflict,
                modifier = Modifier.fillMaxWidth()
            )

            // Error Message
            state.errorMessage?.let { error ->
                ErrorBanner(
                    title = "Error",
                    message = error,
                    onDismiss = { vm.clearError() }
                )
            }

            // Bottom spacing for content
            Spacer(modifier = Modifier.height(80.dp))
        }

        // Bottom Sheets
        AppPickerSheet(
            isVisible = uiState.showAppPicker,
            onDismiss = { vm.setAppPickerVisible(false) },
            onAppSelected = { pkg, label ->
                vm.setSelectedApp(pkg, label)
                vm.setAppPickerVisible(false)
            },
            recentApps = recentApps
        )

        DateTimePickers(
            showDatePicker = uiState.showDatePicker,
            showTimePicker = uiState.showTimePicker,
            onDateSelected = { dateMillis ->
                vm.setDatePart(dateMillis)
                vm.setDatePickerVisible(false)
            },
            onTimeSelected = { hour, minute ->
                vm.setTimePart(hour, minute)
                vm.setTimePickerVisible(false)
            },
            onDatePickerDismiss = { vm.setDatePickerVisible(false) },
            onTimePickerDismiss = { vm.setTimePickerVisible(false) },
            currentTimeMillis = state.triggerAtMillis
        )
    }
}

@Composable
private fun AppSelectionCard(
    selectedApp: Pair<String?, String?>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val (packageName, appLabel) = selectedApp
    
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (packageName != null) {
                    // Try to get the actual app icon
                    val icon = remember(packageName) {
                        try {
                            context.packageManager.getApplicationIcon(packageName)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    
                    if (icon != null) {
                        val bitmap = remember(icon) { 
                            icon.toBitmap(48, 48)
                        }
                        androidx.compose.foundation.Image(
                            painter = BitmapPainter(bitmap.asImageBitmap()),
                            contentDescription = "$appLabel icon",
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        // Fallback to first letter
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (appLabel ?: "A").take(1).uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    // No app selected placeholder
                    Icon(
                        Icons.Default.Apps,
                        contentDescription = "Select app",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // App Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = appLabel ?: "Pick app",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (appLabel != null) FontWeight.Medium else FontWeight.Normal,
                    color = if (appLabel != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (packageName != null) {
                    Text(
                        text = packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Select an app to schedule",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Change Button
            FilledTonalButton(
                onClick = onClick,
                modifier = Modifier.semantics {
                    contentDescription = if (appLabel != null) "Change selected app" else "Select app"
                }
            ) {
                Text(if (appLabel != null) "Change" else "Select")
            }
        }
    }
}

data class EditState(
    val id: Long? = null,
    val packageName: String? = null,
    val selectedAppLabel: String? = null,
    val triggerAtMillis: Long? = null,
    val errorMessage: String? = null
) {
    val canSave: Boolean get() = packageName != null && triggerAtMillis != null
}

data class EditScheduleUiState(
    val showAppPicker: Boolean = false,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val isSaving: Boolean = false,
    val hasConflict: Boolean = false
)

class EditScheduleViewModel(
    private val repo: SchedulesRepository,
    private val dao: com.example.appscheduler_kotlin.data.ScheduleDao
) : ViewModel() {

    private val _state = MutableStateFlow(EditState())
    val state = _state.asStateFlow()

    private val _uiState = MutableStateFlow(EditScheduleUiState())
    val uiState = _uiState.asStateFlow()

    fun load(id: Long?) {
        viewModelScope.launch {
            if (id == null) {
                _state.value = EditState()
            } else {
                val s = dao.get(id)
                _state.value = if (s == null) EditState()
                else EditState(
                    id = s.id,
                    packageName = s.packageName,
                    selectedAppLabel = getAppLabel(s.packageName),
                    triggerAtMillis = s.triggerAtMillis
                )
            }
        }
    }

    private suspend fun getAppLabel(packageName: String): String {
        // This should ideally be done with a context injected into the ViewModel
        // For now, we'll just return the package name
        return packageName
    }

    fun setSelectedApp(pkg: String, label: String) {
        _state.value = _state.value.copy(
            packageName = pkg, 
            selectedAppLabel = label,
            errorMessage = null
        )
        checkForConflicts()
    }

    fun setDateTime(timeMillis: Long) {
        _state.value = _state.value.copy(
            triggerAtMillis = timeMillis,
            errorMessage = null
        )
        checkForConflicts()
    }

    fun setDatePart(dateMillis: Long) {
        val existing = _state.value.triggerAtMillis
        val cal = Calendar.getInstance()
        
        // Set the new date
        val dateCal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        
        // Preserve existing time if available, otherwise use current time
        if (existing != null) {
            val existingCal = Calendar.getInstance().apply { timeInMillis = existing }
            cal.set(Calendar.YEAR, dateCal.get(Calendar.YEAR))
            cal.set(Calendar.MONTH, dateCal.get(Calendar.MONTH))
            cal.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, existingCal.get(Calendar.HOUR_OF_DAY))
            cal.set(Calendar.MINUTE, existingCal.get(Calendar.MINUTE))
        } else {
            cal.timeInMillis = dateMillis
        }
        
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        setDateTime(cal.timeInMillis)
    }

    fun setTimePart(hour: Int, minute: Int) {
        val existing = _state.value.triggerAtMillis
        val cal = Calendar.getInstance()
        
        if (existing != null) {
            cal.timeInMillis = existing
        }
        
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        setDateTime(cal.timeInMillis)
    }

    private fun checkForConflicts() {
        val triggerTime = _state.value.triggerAtMillis
        if (triggerTime != null) {
            viewModelScope.launch {
                try {
                    // Check if there's already a schedule at this exact time
                    // This is a simplified check - in a real app you might want more sophisticated conflict detection
                    val existingSchedules = dao.getAll()
                    val hasConflict = existingSchedules.any { 
                        it.triggerAtMillis == triggerTime && it.id != _state.value.id 
                    }
                    _uiState.value = _uiState.value.copy(hasConflict = hasConflict)
                } catch (e: Exception) {
                    // Handle error silently for conflict check
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(hasConflict = false)
        }
    }

    fun setAppPickerVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showAppPicker = visible)
    }

    fun setDatePickerVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showDatePicker = visible)
    }

    fun setTimePickerVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showTimePicker = visible)
    }

    fun setError(message: String) {
        _state.value = _state.value.copy(errorMessage = message)
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun saveOrUpdate(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            
            val st = _state.value
            val pkg = st.packageName
            val whenMs = st.triggerAtMillis
            
            if (pkg == null || whenMs == null) {
                _uiState.value = _uiState.value.copy(isSaving = false)
                onError("Please pick app, date and time.")
                return@launch
            }
            
            if (whenMs <= System.currentTimeMillis()) {
                _uiState.value = _uiState.value.copy(isSaving = false)
                onError("Time must be in the future.")
                return@launch
            }
            
            val result = if (st.id == null) {
                repo.create(pkg, whenMs)
            } else {
                repo.updateTime(st.id, whenMs)
            }
            
            _uiState.value = _uiState.value.copy(isSaving = false)
            
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = {
                    val message = if (it.message?.contains("UNIQUE", true) == true) {
                        "A schedule already exists at the same time. Pick a different time."
                    } else it.message ?: "Failed: ${it::class.java.simpleName}"
                    onError(message)
                }
            )
        }
    }
}

