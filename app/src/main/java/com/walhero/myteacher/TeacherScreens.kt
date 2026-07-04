package com.walhero.myteacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherLoginScreen(
    passcode: String,
    onPasscodeChanged: (String) -> Unit,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var input by rememberSaveable { mutableStateOf("") }
    var showChangeDialog by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("Teacher Login") }, navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.Center) {
            OutlinedTextField(input, { input = it }, label = { Text("Passcode") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true)
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (input == passcode) {
                    error = null
                    onSuccess()
                } else {
                    error = "Wrong passcode"
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Login") }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = { showChangeDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("Change Passcode") }
        }
    }

    if (showChangeDialog) {
        ChangePasscodeDialog(
            currentPasscode = passcode,
            onDismiss = { showChangeDialog = false },
            onSave = {
                onPasscodeChanged(it)
                showChangeDialog = false
                input = ""
                error = "Passcode changed successfully"
            }
        )
    }
}

@Composable
fun ChangePasscodeDialog(currentPasscode: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var current by remember { mutableStateOf("") }
    var newCode by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Passcode") },
        text = {
            Column {
                OutlinedTextField(current, { current = it }, label = { Text("Current passcode") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(newCode, { newCode = it }, label = { Text("New passcode") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(confirm, { confirm = it }, label = { Text("Confirm new passcode") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                error = when {
                    current != currentPasscode -> "Current passcode is wrong"
                    newCode.isBlank() -> "New passcode is required"
                    newCode != confirm -> "New passcodes do not match"
                    else -> null
                }
                if (error == null) onSave(newCode)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(students: List<Student>, messages: MutableList<TeacherMessage>, onBack: () -> Unit) {
    var selectedStudentId by rememberSaveable { mutableStateOf(students.firstOrNull()?.id.orEmpty()) }
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var notice by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("Teacher Dashboard") }, navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Students", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            students.forEach { student ->
                Card(colors = CardDefaults.cardColors(containerColor = if (selectedStudentId == student.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(student.name, fontWeight = FontWeight.Bold)
                            Text("${student.className} • Code: ${student.parentCode}")
                        }
                        TextButton(onClick = { selectedStudentId = student.id }) { Text("Select") }
                    }
                }
            }
            Text("Send Message", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(body, { body = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Button(onClick = {
                if (selectedStudentId.isNotBlank() && title.isNotBlank() && body.isNotBlank()) {
                    messages.add(0, TeacherMessage("m${messages.size + 1}", selectedStudentId, title, body, "Today"))
                    title = ""
                    body = ""
                    notice = "Message sent"
                } else {
                    notice = "Select a student and write a message"
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Send") }
            if (notice != null) Text(notice.orEmpty())
        }
    }
}
