package com.walhero.myteacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentAccessScreen(
    students: List<Student>,
    onOpenInbox: (Student) -> Unit,
    onBack: () -> Unit
) {
    var code by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("دخول الولي") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("كود التلميذ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val s = students.find { it.parentCode == code }
                    if (s != null) {
                        error = null
                        onOpenInbox(s)
                    } else {
                        error = "كود غير صحيح"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) { Text("دخول") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentInboxScreen(
    student: Student?,
    messages: List<TeacherMessage>,
    freeOpened: Map<String, Set<String>>,
    adUnlocked: Set<String>,
    onOpenFree: (String, String) -> Unit,
    onRewardedUnlock: (String) -> Unit,
    onBack: () -> Unit
) {
    if (student == null) {
        onBack()
        return
    }
    
    val myMessages = messages.filter { it.studentId == student.id }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("رسائل \${student.name}") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        if (myMessages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("لا توجد رسائل حالياً.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myMessages) { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(msg.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(msg.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text(msg.body, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
