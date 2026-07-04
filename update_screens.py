import os

app_models = """package com.walhero.myteacher

data class Student(
    val id: String,
    val name: String,
    val className: String,
    val parentCode: String
)

data class TeacherMessage(
    val id: String,
    val studentId: String,
    val title: String,
    val body: String,
    val date: String
)

enum class AppScreen {
    Home,
    TeacherLogin,
    TeacherDashboard,
    TeacherStudentDetails,
    ParentAccess,
    ParentInbox
}
"""

my_teacher_app = """package com.walhero.myteacher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun MyTeacherApp() {
    var screen by remember { mutableStateOf(AppScreen.Home) }
    var teacherPasscode by rememberSaveable { mutableStateOf("teacher123") }
    var activeStudent by remember { mutableStateOf<Student?>(null) }
    
    val students = remember { mutableStateListOf<Student>() }
    val messages = remember { mutableStateListOf<TeacherMessage>() }

    when (screen) {
        AppScreen.Home -> HomeScreen(
            onTeacher = { screen = AppScreen.TeacherLogin },
            onParent = { screen = AppScreen.ParentAccess }
        )
        AppScreen.TeacherLogin -> TeacherLoginScreen(
            passcode = teacherPasscode,
            onPasscodeChanged = { teacherPasscode = it },
            onSuccess = { screen = AppScreen.TeacherDashboard },
            onBack = { screen = AppScreen.Home }
        )
        AppScreen.TeacherDashboard -> TeacherDashboardScreen(
            students = students,
            messages = messages,
            onStudentClick = { student ->
                activeStudent = student
                screen = AppScreen.TeacherStudentDetails
            },
            onBack = { screen = AppScreen.Home }
        )
        AppScreen.TeacherStudentDetails -> {
            activeStudent?.let { student ->
                TeacherStudentDetailsScreen(
                    student = student,
                    messages = messages,
                    onBack = { screen = AppScreen.TeacherDashboard }
                )
            } ?: run {
                screen = AppScreen.TeacherDashboard
            }
        }
        AppScreen.ParentAccess -> ParentAccessScreen(
            students = students,
            onOpenInbox = {
                activeStudent = it
                screen = AppScreen.ParentInbox
            },
            onBack = { screen = AppScreen.Home }
        )
        AppScreen.ParentInbox -> ParentInboxScreen(
            student = activeStudent,
            messages = messages,
            freeOpened = emptyMap(),
            adUnlocked = emptySet(),
            onOpenFree = { _, _ -> },
            onRewardedUnlock = { _ -> },
            onBack = { screen = AppScreen.ParentAccess }
        )
    }
}
"""

teacher_screens = """package com.walhero.myteacher

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تسجيل دخول المعلمة") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("كلمة المرور") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (input == passcode) {
                        error = null
                        onSuccess()
                    } else {
                        error = "كلمة المرور خاطئة"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) { Text("دخول") }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = { showChangeDialog = true }) {
                Text("تغيير كلمة المرور")
            }
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
                error = "تم تغيير كلمة المرور بنجاح"
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
        title = { Text("تغيير كلمة المرور") },
        text = {
            Column {
                OutlinedTextField(current, { current = it }, label = { Text("الكلمة الحالية") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(newCode, { newCode = it }, label = { Text("الكلمة الجديدة") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(confirm, { confirm = it }, label = { Text("تأكيد الكلمة الجديدة") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                error = when {
                    current != currentPasscode -> "الكلمة الحالية خاطئة"
                    newCode.isBlank() -> "الكلمة الجديدة مطلوبة"
                    newCode != confirm -> "الكلمات غير متطابقة"
                    else -> null
                }
                if (error == null) onSave(newCode)
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    students: MutableList<Student>,
    messages: List<TeacherMessage>,
    onStudentClick: (Student) -> Unit,
    onBack: () -> Unit
) {
    var selectedClass by rememberSaveable { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().use { reader ->
                        val lines = reader.readLines()
                        var added = 0
                        for (i in 1 until lines.size) {
                            val parts = lines[i].split(",")
                            if (parts.size >= 3) {
                                val cName = parts[0].trim()
                                val sName = parts[1].trim()
                                val sCode = parts[2].trim()
                                
                                if (sName.isNotEmpty() && cName.isNotEmpty() && sCode.isNotEmpty()) {
                                    if (students.none { it.parentCode == sCode }) {
                                        students.add(Student(UUID.randomUUID().toString(), sName, cName, sCode))
                                        added++
                                    }
                                }
                            }
                        }
                        notice = "تم استيراد \$added تلاميذ بنجاح."
                    }
                }
            } catch (e: Exception) {
                notice = "حدث خطأ أثناء قراءة الملف."
            }
        }
    }

    val classes = students.map { it.className }.distinct()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لوحة المعلمة") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { csvLauncher.launch("*/*") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("استيراد قسم")
                }
                FilledTonalButton(onClick = { showAddDialog = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("إضافة تلميذ")
                }
            }
            
            if (notice != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(notice.orEmpty(), modifier = Modifier.weight(1f))
                        IconButton(onClick = { notice = null }) { Icon(Icons.Default.Close, null) }
                    }
                }
            }

            if (classes.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("لا يوجد تلاميذ، يرجى الاستيراد أو الإضافة.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Text("الأقسام", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    classes.forEach { className ->
                        FilterChip(
                            selected = selectedClass == className,
                            onClick = { selectedClass = if (selectedClass == className) null else className },
                            label = { Text(className) }
                        )
                    }
                }
                
                if (selectedClass != null) {
                    Text("التلاميذ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val classStudents = students.filter { it.className == selectedClass }
                    classStudents.forEach { student ->
                        val studentMessages = messages.filter { it.studentId == student.id }
                        val messageCount = studentMessages.size
                        val lastMessageDate = studentMessages.firstOrNull()?.date ?: "لا يوجد"
                        
                        Card(
                            onClick = { onStudentClick(student) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(student.name, fontWeight = FontWeight.Bold)
                                    Text("كود: \${student.parentCode}", style = MaterialTheme.typography.bodySmall)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("الرسائل: \$messageCount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    Text("آخر رسالة: \$lastMessageDate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var newName by remember { mutableStateOf("") }
        var newClass by remember { mutableStateOf("") }
        var newCode by remember { mutableStateOf("") }
        var addError by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة تلميذ جديد") },
            text = {
                Column {
                    OutlinedTextField(newName, { newName = it }, label = { Text("اسم التلميذ") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(newClass, { newClass = it }, label = { Text("القسم") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(newCode, { newCode = it }, label = { Text("كود الولي (اختياري)") }, modifier = Modifier.fillMaxWidth())
                    if (addError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(addError.orEmpty(), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isBlank() || newClass.isBlank()) {
                        addError = "الاسم والقسم مطلوبان"
                    } else {
                        val finalCode = if (newCode.isBlank()) "MT-\${(1000..9999).random()}" else newCode
                        if (students.any { it.parentCode == finalCode }) {
                            addError = "الكود مستخدم بالفعل"
                        } else {
                            students.add(Student(UUID.randomUUID().toString(), newName.trim(), newClass.trim(), finalCode.trim()))
                            showAddDialog = false
                            notice = "تمت الإضافة بنجاح"
                        }
                    }
                }) { Text("حفظ التلميذ") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("إلغاء") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherStudentDetailsScreen(
    student: Student,
    messages: MutableList<TeacherMessage>,
    onBack: () -> Unit
) {
    var showSendDialog by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }
    
    val studentMessages = messages.filter { it.studentId == student.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ملف التلميذ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Student Details Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("الاسم: \${student.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("القسم: \${student.className}", style = MaterialTheme.typography.bodyLarge)
                    Text("الكود: \${student.parentCode}", style = MaterialTheme.typography.bodyLarge)
                }
            }
            
            if (notice != null) {
                Text(
                    text = notice.orEmpty(),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            // Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { showSendDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("إرسال رسالة جديدة")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "سجل الرسائل",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (studentMessages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لم يتم إرسال أي رسالة بعد.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(studentMessages) { msg ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(msg.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(msg.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(msg.body, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSendDialog) {
        var title by rememberSaveable { mutableStateOf("") }
        var body by rememberSaveable { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { showSendDialog = false },
            title = { Text("رسالة جديدة لـ \${student.name}") },
            text = {
                Column {
                    OutlinedTextField(title, { title = it }, label = { Text("العنوان") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(body, { body = it }, label = { Text("المحتوى") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
                    if (error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank() && body.isNotBlank()) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        val now = sdf.format(Date())
                        messages.add(0, TeacherMessage(UUID.randomUUID().toString(), student.id, title, body, now))
                        showSendDialog = false
                        notice = "تم إرسال الرسالة بنجاح."
                    } else {
                        error = "يرجى كتابة العنوان والمحتوى."
                    }
                }) { Text("إرسال") }
            },
            dismissButton = { TextButton(onClick = { showSendDialog = false }) { Text("إلغاء") } }
        )
    }
}
"""

with open("app/src/main/java/com/walhero/myteacher/AppModels.kt", "w") as f:
    f.write(app_models)
with open("app/src/main/java/com/walhero/myteacher/MyTeacherApp.kt", "w") as f:
    f.write(my_teacher_app)
with open("app/src/main/java/com/walhero/myteacher/TeacherScreens.kt", "w") as f:
    f.write(teacher_screens)
