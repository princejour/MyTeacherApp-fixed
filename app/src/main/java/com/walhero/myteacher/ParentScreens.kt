package com.walhero.myteacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
                title = { Text("Parent Login") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Student Code") },
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
                    val student = students.find { it.parentCode.equals(code.trim(), ignoreCase = true) }
                    if (student != null) {
                        error = null
                        onOpenInbox(student)
                    } else {
                        error = "Invalid code"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Text("Login")
            }
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
    canRequestAds: Boolean,
    isRewardedAdReady: Boolean,
    onOpenFree: (String, String) -> Unit,
    onRewardedUnlock: (String, (String) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    if (student == null) {
        onBack()
        return
    }

    val myMessages = messages.filter { it.studentId == student.id }
    val openedIds = freeOpened[student.id].orEmpty()
    var adNotice by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${student.name}'s Messages") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (myMessages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No messages currently.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Free message openings: ${openedIds.size}/5",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "After five free openings, each additional message requires one completed rewarded ad.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            adNotice?.let {
                                Spacer(Modifier.height(8.dp))
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                items(myMessages, key = { it.id }) { message ->
                    val openedForFree = openedIds.contains(message.id)
                    val unlockedByAd = adUnlocked.contains(message.id)
                    val isOpen = openedForFree || unlockedByAd
                    val canOpenFree = openedIds.size < 5 && !isOpen

                    ParentMessageCard(
                        message = message,
                        isOpen = isOpen,
                        canOpenFree = canOpenFree,
                        canRequestAds = canRequestAds,
                        isRewardedAdReady = isRewardedAdReady,
                        onOpenFree = {
                            adNotice = null
                            onOpenFree(student.id, message.id)
                        },
                        onRewardedUnlock = {
                            adNotice = null
                            onRewardedUnlock(message.id) { errorMessage ->
                                adNotice = errorMessage
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentMessageCard(
    message: TeacherMessage,
    isOpen: Boolean,
    canOpenFree: Boolean,
    canRequestAds: Boolean,
    isRewardedAdReady: Boolean,
    onOpenFree: () -> Unit,
    onRewardedUnlock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    message.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isOpen) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = null
                )
            }
            Text(
                message.date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            when {
                isOpen -> Text(message.body, style = MaterialTheme.typography.bodyMedium)

                canOpenFree -> {
                    Text(
                        "This message can be opened within the five free openings.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = onOpenFree, modifier = Modifier.fillMaxWidth()) {
                        Text("Open Free Message")
                    }
                }

                else -> {
                    Text(
                        "Watch one rewarded ad to unlock this message.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onRewardedUnlock,
                        enabled = canRequestAds,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            when {
                                !canRequestAds -> "Ads unavailable"
                                isRewardedAdReady -> "Watch Ad to Unlock"
                                else -> "Prepare Rewarded Ad"
                            }
                        )
                    }
                }
            }
        }
    }
}
