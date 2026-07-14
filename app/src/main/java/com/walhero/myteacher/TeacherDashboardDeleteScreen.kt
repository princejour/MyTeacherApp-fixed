package com.walhero.myteacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TeacherDashboardWithDeleteScreen(
    students: MutableList<Student>,
    messages: MutableList<TeacherMessage>,
    onStudentClick: (Student) -> Unit,
    onDeleteClass: (String) -> Unit,
    onBack: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedClassToDelete by remember { mutableStateOf<String?>(null) }
    val classes = students.map { it.className }.distinct()

    Box(modifier = Modifier.fillMaxSize()) {
        key(classes) {
            TeacherDashboardScreen(
                students = students,
                messages = messages,
                onStudentClick = onStudentClick,
                onBack = onBack
            )
        }

        if (classes.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = {
                    selectedClassToDelete = null
                    showDeleteDialog = true
                },
                icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                text = { Text("Delete Class") },
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedClassToDelete = null
            },
            title = { Text("Delete class list") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Choose the class list to delete. Its students and message history will also be deleted permanently.")
                    Spacer(modifier = Modifier.height(8.dp))
                    classes.forEach { className ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedClassToDelete = className }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedClassToDelete == className,
                                onClick = { selectedClassToDelete = className }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(className, modifier = Modifier.weight(1f))
                            Text(
                                "${students.count { it.className == className }} students",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = selectedClassToDelete != null,
                    onClick = {
                        selectedClassToDelete?.let(onDeleteClass)
                        showDeleteDialog = false
                        selectedClassToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedClassToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
