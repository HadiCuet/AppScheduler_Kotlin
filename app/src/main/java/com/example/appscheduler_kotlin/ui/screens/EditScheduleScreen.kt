package com.example.appscheduler_kotlin.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appscheduler_kotlin.R
import com.example.appscheduler_kotlin.data.AppDatabase
import com.example.appscheduler_kotlin.repo.SchedulesRepository
import com.example.appscheduler_kotlin.repo.SchedulesRepository as RepoFactory
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember { RepoFactory(context) }
    val vm = remember { EditScheduleViewModel(repo, AppDatabase.get(context).scheduleDao()) }

    LaunchedEffect(scheduleId) {
        vm.load(scheduleId)
    }

    val state by vm.state.collectAsState()

    val snack = remember { SnackbarHostState() }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (scheduleId == null) "Create Schedule" else "Edit Schedule") }) },
        snackbarHost = { SnackbarHost(snack) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            AppPickerRow(
                selected = state.selectedAppLabel ?: stringResource(R.string.label_pick_app),
                onPick = { vm.pickAppDialog = true }
            )

            DateTimeRow(
                millis = state.triggerAtMillis,
                onPickDate = { vm.pickDate(context) },
                onPickTime = { vm.pickTime(context) }
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    vm.saveOrUpdate(
                        onSuccess = onDone,
                        onError = { msg -> vm.errorMessage = msg }
                    )
                },
                enabled = state.canSave
            ) {
                Text(stringResource(R.string.action_save))
            }

            state.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
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
private fun AppPickerRow(selected: String, onPick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("${stringResource(R.string.label_select_app)}: $selected")
        OutlinedButton(onClick = onPick) { Text(stringResource(R.string.action_select)) }
    }
}

@Composable
private fun DateTimeRow(millis: Long?, onPickDate: () -> Unit, onPickTime: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("${stringResource(R.string.label_date)}: ${millis?.let { Formatters.formatDateTime(it).substring(0, 10) } ?: "--"}")
            OutlinedButton(onClick = onPickDate) { Text(stringResource(R.string.action_change)) }
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("${stringResource(R.string.label_time)}: ${millis?.let { Formatters.formatDateTime(it).substring(11) } ?: "--"}")
            OutlinedButton(onClick = onPickTime) { Text(stringResource(R.string.action_change)) }
        }
    }
}

@Composable
private fun AppPickerDialog(onDismiss: () -> Unit, onSelected: (String, String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val apps = remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    LaunchedEffect(Unit) {
        apps.value = InstalledApps.loadLaunchable(context)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.label_select_app)) },
        text = {
            Box(Modifier.heightIn(max = 400.dp)) {
                LazyColumn {
                    items(apps.value) { app ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelected(app.packageName, app.label)
                                    onDismiss()
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.width(8.dp))
                            Text(app.label, modifier = Modifier.weight(1f))
                            Text(app.packageName)
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
    private val dao: com.example.appscheduler_kotlin.data.ScheduleDao
) : ViewModel() {

    private val _state = MutableStateFlow(EditState())
    val state = _state.asStateFlow()

    var pickAppDialog by mutableStateOf(false)
    var errorMessage: String?
        get() = _state.value.errorMessage
        set(value) { _state.value = _state.value.copy(errorMessage = value) }

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
                    selectedAppLabel = s.packageName,
                    triggerAtMillis = s.triggerAtMillis
                )
            }
        }
    }

    fun setSelectedApp(pkg: String, label: String) {
        _state.value = _state.value.copy(packageName = pkg, selectedAppLabel = label)
    }

    fun pickDate(context: android.content.Context) {
        val cal = currentCal()
        val dlg = DatePickerDialog(context, { _, y, m, d ->
            val newCal = currentCal().apply {
                set(y, m, d)
            }
            setDatePart(newCal)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dlg.show()
    }

    fun pickTime(context: android.content.Context) {
        val cal = currentCal()
        val dlg = TimePickerDialog(context, { _, h, min ->
            val newCal = currentCal().apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, min)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            setTimePart(newCal)
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
        dlg.show()
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

    fun saveOrUpdate(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val st = _state.value
            val pkg = st.packageName
            val whenMs = st.triggerAtMillis
            if (pkg == null || whenMs == null) {
                onError("Please pick app, date and time.")
                return@launch
            }
            if (whenMs <= System.currentTimeMillis()) {
                onError(contextString("Time must be in the future."))
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
                        contextString("A schedule already exists at the same time. Pick a different time.")
                    } else it.message ?: "Failed: ${it::class.java.simpleName}"
                    onError(message)
                }
            )
        }
    }

    private fun contextString(s: String) = s
}

private fun currentCal(): Calendar = Calendar.getInstance()