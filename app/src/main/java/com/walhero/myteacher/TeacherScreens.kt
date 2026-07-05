package com.walhero.myteacher

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
                title = { Text("Teacher Login") },
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
                label = { Text("Passcode") },
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
                        error = "Incorrect passcode"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) { Text("Login") }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = { showChangeDialog = true }) {
                Text("Change Passcode")
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
                    current != currentPasscode -> "Current passcode is incorrect"
                    newCode.isBlank() -> "New passcode is required"
                    newCode != confirm -> "Passcodes do not match"
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
                    val bytes = stream.readBytes()
                    val parsedRows = mutableListOf<List<String>>()
                    var fileParseError = ""

                    val isZip = bytes.size >= 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() && bytes[2] == 0x03.toByte() && bytes[3] == 0x04.toByte()

                    if (isZip) {
                        try {
                            val sharedStrings = mutableListOf<String>()
                            var sheetXml: ByteArray? = null
                            var sharedStringsXml: ByteArray? = null

                            java.util.zip.ZipInputStream(java.io.ByteArrayInputStream(bytes)).use { zis ->
                                var entry = zis.nextEntry
                                while (entry != null) {
                                    if (entry.name == "xl/sharedStrings.xml") {
                                        sharedStringsXml = zis.readBytes()
                                    } else if (entry.name.startsWith("xl/worksheets/sheet") && entry.name.endsWith(".xml") && sheetXml == null) {
                                        sheetXml = zis.readBytes()
                                    }
                                    zis.closeEntry()
                                    entry = zis.nextEntry
                                }
                            }

                            if (sharedStringsXml != null) {
                                val factory = org.xmlpull.v1.XmlPullParserFactory.newInstance()
                                val parser = factory.newPullParser()
                                parser.setInput(java.io.ByteArrayInputStream(sharedStringsXml), "UTF-8")
                                
                                var eventType = parser.eventType
                                var currentText = ""
                                var inSi = false
                                var inT = false
                                while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                                    when (eventType) {
                                        org.xmlpull.v1.XmlPullParser.START_TAG -> {
                                            if (parser.name == "si") {
                                                inSi = true
                                                currentText = ""
                                            } else if (parser.name == "t" && inSi) {
                                                inT = true
                                            }
                                        }
                                        org.xmlpull.v1.XmlPullParser.TEXT -> {
                                            if (inT) {
                                                currentText += parser.text
                                            }
                                        }
                                        org.xmlpull.v1.XmlPullParser.END_TAG -> {
                                            if (parser.name == "t") {
                                                inT = false
                                            } else if (parser.name == "si") {
                                                inSi = false
                                                sharedStrings.add(currentText)
                                            }
                                        }
                                    }
                                    eventType = parser.next()
                                }
                            }

                            if (sheetXml != null) {
                                val factory = org.xmlpull.v1.XmlPullParserFactory.newInstance()
                                val parser = factory.newPullParser()
                                parser.setInput(java.io.ByteArrayInputStream(sheetXml), "UTF-8")
                                
                                var eventType = parser.eventType
                                var currentRow = mutableListOf<String>()
                                var inV = false
                                var currentV = ""
                                var cellType = ""
                                var cellRef = ""
                                var currentCellIndex = 0

                                while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                                    when (eventType) {
                                        org.xmlpull.v1.XmlPullParser.START_TAG -> {
                                            if (parser.name == "row") {
                                                currentRow = mutableListOf()
                                                currentCellIndex = 0
                                            } else if (parser.name == "c") {
                                                cellType = parser.getAttributeValue(null, "t") ?: ""
                                                cellRef = parser.getAttributeValue(null, "r") ?: ""
                                                currentV = ""
                                                inV = false
                                                
                                                val colStr = cellRef.takeWhile { it.isLetter() }
                                                var colIdx = 0
                                                for (c in colStr) {
                                                    colIdx = colIdx * 26 + (c - 'A' + 1)
                                                }
                                                colIdx -= 1
                                                
                                                while (currentCellIndex < colIdx) {
                                                    currentRow.add("")
                                                    currentCellIndex++
                                                }
                                            } else if (parser.name == "v" || parser.name == "t") {
                                                inV = true
                                            }
                                        }
                                        org.xmlpull.v1.XmlPullParser.TEXT -> {
                                            if (inV) {
                                                currentV += parser.text
                                            }
                                        }
                                        org.xmlpull.v1.XmlPullParser.END_TAG -> {
                                            if (parser.name == "v" || parser.name == "t") {
                                                inV = false
                                            } else if (parser.name == "c") {
                                                var value = currentV
                                                if (cellType == "s") {
                                                    val index = value.toIntOrNull()
                                                    if (index != null && index >= 0 && index < sharedStrings.size) {
                                                        value = sharedStrings[index]
                                                    }
                                                }
                                                currentRow.add(value)
                                                currentCellIndex++
                                                currentV = ""
                                                cellType = ""
                                            } else if (parser.name == "row") {
                                                parsedRows.add(currentRow)
                                            }
                                        }
                                    }
                                    eventType = parser.next()
                                }
                            } else {
                                fileParseError = "Please select a valid student list file."
                            }
                        } catch (e: Exception) {
                            fileParseError = "Please select a valid student list file."
                        }
                    } else {
                        var contentStr = ""
                        var encodingError = false
                        
                        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
                            contentStr = String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
                        } else if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
                            contentStr = String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
                        } else if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
                            contentStr = String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
                        } else {
                            val utf8Str = String(bytes, Charsets.UTF_8)
                            if (!utf8Str.contains("�")) {
                                contentStr = utf8Str
                            } else {
                                try {
                                    val cp1256Str = String(bytes, java.nio.charset.Charset.forName("windows-1256"))
                                    if (cp1256Str.any { it in '؀'..'ۿ' }) {
                                        contentStr = cp1256Str
                                    } else {
                                        val isoStr = String(bytes, java.nio.charset.Charset.forName("ISO-8859-6"))
                                        if (isoStr.any { it in '؀'..'ۿ' }) {
                                            contentStr = isoStr
                                        } else {
                                            encodingError = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    encodingError = true
                                }
                            }
                        }

                        if (encodingError || contentStr.contains("�")) {
                            fileParseError = "Please select a valid student list file."
                        } else {
                            val lines = contentStr.split(Regex("\r?\n")).filter { it.isNotBlank() }
                            
                            val delimiters = listOf(",", ";", "	", "|")
                            var bestDelimiter = ","
                            var maxCols = 0
                            
                            for (delim in delimiters) {
                                val sampleCols = lines.take(5).sumOf { it.split(delim).size }
                                if (sampleCols > maxCols) {
                                    maxCols = sampleCols
                                    bestDelimiter = delim
                                }
                            }
                            
                            for (line in lines) {
                                parsedRows.add(line.split(bestDelimiter).map { it.trim() })
                            }
                        }
                    }

                    if (fileParseError.isNotEmpty()) {
                        notice = fileParseError
                    } else if (parsedRows.isNotEmpty()) {
                        val classHeaders = listOf("class_name", "class", "section", "القسم")
                        val nameHeaders = listOf("student_name", "student", "name", "اسم التلميذ", "الاسم")
                        val codeHeaders = listOf("student_code", "code", "parent_code", "الكود", "الرمز")
    
                        var classIdx = -1
                        var nameIdx = -1
                        var codeIdx = -1
    
                        val firstRow = parsedRows[0].map { it.lowercase().trim() }
                        val isHeader = firstRow.any { it in classHeaders || it in nameHeaders || it in codeHeaders }
                        
                        if (isHeader) {
                            for ((i, cell) in firstRow.withIndex()) {
                                if (cell in classHeaders) classIdx = i
                                else if (cell in nameHeaders) nameIdx = i
                                else if (cell in codeHeaders) codeIdx = i
                            }
                            parsedRows.removeAt(0)
                        }
    
                        if (classIdx == -1) classIdx = 0
                        if (nameIdx == -1) nameIdx = 1
                        if (codeIdx == -1) codeIdx = 2
    
                        var added = 0
                        for (row in parsedRows) {
                            if (row.isEmpty() || row.all { it.isBlank() }) continue
                            
                            val cName = if (classIdx < row.size && classIdx >= 0) row[classIdx].trim() else ""
                            val sName = if (nameIdx < row.size && nameIdx >= 0) row[nameIdx].trim() else ""
                            val sCodeRaw = if (codeIdx < row.size && codeIdx >= 0) row[codeIdx].trim() else ""
                            
                            if (sName.isNotEmpty() && cName.isNotEmpty()) {
                                val sCode = if (sCodeRaw.isEmpty()) "MT-${(1000..9999).random()}" else sCodeRaw
                                if (students.none { it.parentCode == sCode }) {
                                    students.add(Student(UUID.randomUUID().toString(), sName, cName, sCode))
                                    added++
                                }
                            }
                        }
    
                        if (added > 0) {
                            notice = "Successfully imported $added students."
                        } else {
                            notice = "Please select a valid student list file."
                        }
                    } else {
                        notice = "Please select a valid student list file."
                    }
                }
            } catch (e: Exception) {
                notice = "Please select a valid student list file."
            }
        }
    }

    val createTemplateLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            try {
                contentResolver.openOutputStream(uri)?.use { stream ->
                    val templateData = "class_name,student_name,student_code\nClass A,Ahmed Ben Ali,MT-1001\nClass A,Sara Ben Amor,MT-1002\nClass B,Youssef Triki,MT-1003"
                    stream.write(templateData.toByteArray(Charsets.UTF_8))
                }
                notice = "Template downloaded successfully."
            } catch (e: Exception) {
                notice = "Error downloading template."
            }
        }
    }

    val classes = students.map { it.className }.distinct()

    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teacher Dashboard") },
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
                    Text("Import Class")
                }
                FilledTonalButton(onClick = { createTemplateLauncher.launch("template.csv") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Download Template")
                }
                FilledTonalButton(onClick = { showAddDialog = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Add Student")
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
                    Text("No students yet. Please import or add.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Text("Classes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                    Text("Students", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val classStudents = students.filter { it.className == selectedClass }
                    classStudents.forEach { student ->
                        val studentMessages = messages.filter { it.studentId == student.id }
                        val messageCount = studentMessages.size
                        val lastMessageDate = studentMessages.firstOrNull()?.date ?: "None"
                        
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
                                    Text("Code: ${student.parentCode}", style = MaterialTheme.typography.bodySmall)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Messages: $messageCount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    Text("Last: $lastMessageDate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            title = { Text("Add New Student") },
            text = {
                Column {
                    OutlinedTextField(newName, { newName = it }, label = { Text("Student Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(newClass, { newClass = it }, label = { Text("Class") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(newCode, { newCode = it }, label = { Text("Parent Code (Optional)") }, modifier = Modifier.fillMaxWidth())
                    if (addError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(addError.orEmpty(), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isBlank() || newClass.isBlank()) {
                        addError = "Name and class are required"
                    } else {
                        val finalCode = if (newCode.isBlank()) "MT-${(1000..9999).random()}" else newCode
                        if (students.any { it.parentCode == finalCode }) {
                            addError = "Code is already in use"
                        } else {
                            students.add(Student(UUID.randomUUID().toString(), newName.trim(), newClass.trim(), finalCode.trim()))
                            showAddDialog = false
                            notice = "Successfully added"
                        }
                    }
                }) { Text("Save Student") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
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
                title = { Text("Student Profile") },
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
                    Text("Name: ${student.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Class: ${student.className}", style = MaterialTheme.typography.bodyLarge)
                    Text("Code: ${student.parentCode}", style = MaterialTheme.typography.bodyLarge)
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
                    Text("Send New Message")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Message History",
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
                    Text("No messages sent yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            title = { Text("New Message to ${student.name}") },
            text = {
                Column {
                    OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(body, { body = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
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
                        notice = "Message sent successfully."
                    } else {
                        error = "Please write a title and message."
                    }
                }) { Text("Send") }
            },
            dismissButton = { TextButton(onClick = { showSendDialog = false }) { Text("Cancel") } }
        )
    }
}
