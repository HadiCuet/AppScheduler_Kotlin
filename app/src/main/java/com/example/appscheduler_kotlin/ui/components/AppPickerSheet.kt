@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.appscheduler_kotlin.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.appscheduler_kotlin.R
import com.example.appscheduler_kotlin.util.InstalledApp
import com.example.appscheduler_kotlin.util.InstalledApps

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onAppSelected: (String, String) -> Unit,
    recentApps: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var allApps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var filteredApps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Load apps when sheet becomes visible
    LaunchedEffect(isVisible) {
        if (isVisible && allApps.isEmpty()) {
            allApps = InstalledApps.loadLaunchable(context)
            filteredApps = allApps
        }
    }

    // Filter apps based on search query
    LaunchedEffect(searchQuery, allApps) {
        filteredApps = if (searchQuery.isBlank()) {
            allApps
        } else {
            allApps.filter { app ->
                app.label.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .animateContentSize()
            ) {
                // Header
                Text(
                    text = "Pick an app",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Search Bar
                DockedSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { },
                    active = false,
                    onActiveChange = { },
                    placeholder = { Text("Search apps...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {}

                // Recent Apps Section (if available)
                if (recentApps.isNotEmpty()) {
                    Text(
                        text = "Recent",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    val recentAppDetails = allApps.filter { it.packageName in recentApps.take(3) }
                    recentAppDetails.forEach { app ->
                        AppListItem(
                            app = app,
                            onClick = {
                                onAppSelected(app.packageName, app.label)
                                onDismiss()
                            }
                        )
                    }
                    
                    if (recentAppDetails.isNotEmpty()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                // All Apps List
                Text(
                    text = "All Apps",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        AppListItem(
                            app = app,
                            onClick = {
                                onAppSelected(app.packageName, app.label)
                                onDismiss()
                            },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }

                // Bottom spacing for gesture navigation
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AppListItem(
    app: InstalledApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .semantics {
                contentDescription = "Select ${app.label}"
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (app.icon != null) {
                    val bitmap = remember(app.icon) { 
                        app.icon.toBitmap(40, 40)
                    }
                    androidx.compose.foundation.Image(
                        painter = BitmapPainter(bitmap.asImageBitmap()),
                        contentDescription = "${app.label} icon",
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    // Fallback icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.label.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // App Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}