@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.appscheduler_kotlin.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appscheduler_kotlin.R
import com.example.appscheduler_kotlin.data.AppDatabase
import com.example.appscheduler_kotlin.data.ScheduleDao
import com.example.appscheduler_kotlin.repo.SchedulesRepository
import com.example.appscheduler_kotlin.ui.PermissionHelpers
import com.example.appscheduler_kotlin.util.Formatters
import com.example.appscheduler_kotlin.util.InstalledApp
import com.example.appscheduler_kotlin.util.InstalledApps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun EditScheduleScreen(
    scheduleId: Long?,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val vm = remember { EditScheduleViewModel(SchedulesRepository(context), AppDatabase.get(context).scheduleDao()) }

    var hasExactAlarmPermission by rememberSaveable {
        mutableStateOf(PermissionHelpers.canScheduleExactAlarms(context))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasExactAlarmPermission = PermissionHelpers.canScheduleExactAlarms(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(scheduleId) {
        vm.load(scheduleId)
    }

    val state by vm.state.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (scheduleId == null) stringResource(R.string.title_create_schedule) else stringResource(R.string.title_edit_schedule)) }) },
        snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDone, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Button(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            vm.saveOrUpdate(
                                context = context,
                                onSuccess = onDone,
                                onError = { msg -> vm.errorMessage = msg }
                            )
                        },
                        enabled = state.canSave && hasExactAlarmPermission,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasExactAlarmPermission) {
                PermissionWarningCard { PermissionHelpers.openExactAlarmSettings(context) }
            }

            AppPickerRow(
                selected = state.selectedAppLabel ?: stringResource(R.string.label_pick_app),
                onPick = { vm.pickAppDialog = true }
            )

            DateTimeRow(
                millis = state.triggerAtMillis,
                onPickDate = { vm.pickDate(context) },
                onPickTime = { vm.pickTime(context) }
            )

            state.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth())
            }
        }

        if (vm.pickAppDialog) {
            AppPickerDialog(
                onDismiss = { vm.pickAppDialog = false },
                onSelected = { pkg, label -> vm.setSelectedApp(pkg, label) }
            )
        }
    }
}

@Composable
private fun PermissionWarningCard(onOpenSettings: () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Info, contentDescription = "Info")
                Text(stringResource(id = R.string.title_permission_required), style = MaterialTheme.typography.titleMedium)
            }
            Text(stringResource(id = R.string.permission_exact_alarm_needed_explanation), style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onOpenSettings, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(id = R.string.action_open_settings))
            }
        }
    }
}

@Composable
private fun AppPickerRow(selected: String, onPick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.label_app_selection_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedCard(onClick = onPick, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selected,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.desc_tappable_select_app),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DateTimeRow(millis: Long?, onPickDate: () -> Unit, onPickTime: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.label_date_selection_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedCard(onClick = onPickDate, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = millis?.let { Formatters.formatDateTime(it).substring(0, 10) } ?: stringResource(R.string.label_select_date),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.desc_tappable_select_date),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.label_time_selection_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedCard(onClick = onPickTime, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = millis?.let { Formatters.formatDateTime(it).substring(11) } ?: stringResource(R.string.label_select_time),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.desc_tappable_select_time),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AppPickerDialog(onDismiss: () -> Unit, onSelected: (String, String) -> Unit) {
    val context = LocalContext.current
    val apps = remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    LaunchedEffect(Unit) { apps.value = InstalledApps.loadLaunchable(context) }

    val iconSize = 36.dp
    val density = LocalDensity.current
    val iconSizePx = remember(iconSize, density) { with(density) { iconSize.roundToPx() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.label_select_app)) },
        text = {
            Box(Modifier.heightIn(max = 400.dp)) {
                LazyColumn {
                    items(apps.value) { app ->
                        val imageBitmap = remember(app.icon, iconSizePx) {
                            app.icon?.let { try { it.toBitmap(width = iconSizePx, height = iconSizePx).asImageBitmap() } catch (e: Exception) { null } }
                        }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onSelected(app.packageName, app.label); onDismiss() }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (imageBitmap != null) {
                                Image(bitmap = imageBitmap, contentDescription = "${app.label} icon", modifier = Modifier.size(iconSize))
                            } else {
                                Spacer(Modifier.size(iconSize))
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(app.label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
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

class EditScheduleViewModel(
    private val repo: SchedulesRepository,
    private val dao: ScheduleDao
) : ViewModel() {

    private val _state = MutableStateFlow(EditState())
    val state = _state.asStateFlow()

    var pickAppDialog by mutableStateOf(false)
    var errorMessage: String?
        get() = _state.value.errorMessage
        set(value) { _state.value = _state.value.copy(errorMessage = value) }

    fun load(id: Long?) {
        viewModelScope.launch {
            _state.value = if (id == null) EditState() else dao.get(id)?.let {
                EditState(
                    id = it.id,
                    packageName = it.packageName,
                    selectedAppLabel = it.appLabel,
                    triggerAtMillis = it.triggerAtMillis
                )
            } ?: EditState()
        }
    }

    fun setSelectedApp(pkg: String, label: String) {
        _state.value = _state.value.copy(packageName = pkg, selectedAppLabel = label)
    }

    fun pickDate(context: Context) {
        val cal = currentCal()
        DatePickerDialog(context, { _, y, m, d ->
            val newCal = currentCal().apply { set(y, m, d) }
            setDatePart(newCal)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    fun pickTime(context: Context) {
        val cal = currentCal()
        TimePickerDialog(context, { _, h, min ->
            val newCal = currentCal().apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, min)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            setTimePart(newCal)
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun setDatePart(cal: Calendar) {
        val existing = _state.value.triggerAtMillis
        val base = if (existing != null) Calendar.getInstance().apply { timeInMillis = existing } else Calendar.getInstance()
        base.set(Calendar.YEAR, cal.get(Calendar.YEAR))
        base.set(Calendar.MONTH, cal.get(Calendar.MONTH))
        base.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH))
        _state.value = _state.value.copy(triggerAtMillis = base.timeInMillis, errorMessage = null)
    }

    private fun setTimePart(cal: Calendar) {
        val existing = _state.value.triggerAtMillis
        val base = if (existing != null) Calendar.getInstance().apply { timeInMillis = existing } else Calendar.getInstance()
        base.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY))
        base.set(Calendar.MINUTE, cal.get(Calendar.MINUTE))
        base.set(Calendar.SECOND, 0)
        base.set(Calendar.MILLISECOND, 0)
        _state.value = _state.value.copy(triggerAtMillis = base.timeInMillis, errorMessage = null)
    }

    fun saveOrUpdate(context: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val st = _state.value
            val pkg = st.packageName
            val whenMs = st.triggerAtMillis
            if (pkg == null || whenMs == null) {
                onError("Please pick app, date and time.")
                return@launch
            }
            if (whenMs <= System.currentTimeMillis()) {
                onError(context.getString(R.string.error_time_must_be_future))
                return@launch
            }
            val result = if (st.id == null) {
                repo.create(pkg, whenMs)
            } else {
                repo.updateTime(st.id, whenMs)
            }
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = {
                    val message = if (it.message?.contains("UNIQUE", true) == true) {
                        context.getString(R.string.error_schedule_conflict)
                    } else it.message ?: "Failed: ${it::class.java.simpleName}"
                    onError(message)
                }
            )
        }
    }
}

private fun currentCal(): Calendar = Calendar.getInstance()
