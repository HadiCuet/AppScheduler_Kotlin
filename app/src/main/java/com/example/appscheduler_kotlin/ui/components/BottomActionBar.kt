@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.appscheduler_kotlin.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomActionBar(
    onSave: () -> Unit,
    onCancel: () -> Unit,
    saveEnabled: Boolean,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        modifier = modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
        tonalElevation = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cancel Button
            TextButton(
                onClick = onCancel,
                enabled = !isLoading,
                modifier = Modifier.semantics {
                    contentDescription = "Cancel schedule creation"
                }
            ) {
                Text("Cancel")
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Save Button
            Button(
                onClick = onSave,
                enabled = saveEnabled && !isLoading,
                modifier = Modifier
                    .semantics {
                        contentDescription = if (saveEnabled) {
                            "Save schedule"
                        } else {
                            "Save schedule - disabled, select app and time first"
                        }
                    }
                    .sizeIn(minWidth = 80.dp, minHeight = 48.dp)
            ) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text("Saving...")
                    }
                } else {
                    Text("Save")
                }
            }
        }
    }
}