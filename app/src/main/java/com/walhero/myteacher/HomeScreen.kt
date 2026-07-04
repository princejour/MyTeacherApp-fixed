package com.walhero.myteacher

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onTeacher: () -> Unit, onParent: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("تطبيقي المدرسي") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(24.dp))
            Text("مرحباً بك", style = MaterialTheme.typography.headlineMedium)
            Text("الرجاء اختيار نوع الدخول", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onParent,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("دخول الولي", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onTeacher,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("دخول المعلمة", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
