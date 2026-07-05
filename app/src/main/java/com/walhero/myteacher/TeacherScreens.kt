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
import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipInputStream
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

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
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
                val rows = parseStudentRows(bytes)
                val added = importStudentRows(rows, students)
                notice = if (added > 0) "Successfully imported $added students." else "Please select a valid student list file."
            } catch (e: Exception) {
                notice = "Please select a valid student list file."
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
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                        val finalCode = if (newCode.isBlank()) nextStudentCode(students, 0) else newCode.trim()
                        if (students.any { it.parentCode == finalCode }) {
                            addError = "Code is already in use"
                        } else {
                            students.add(Student(UUID.randomUUID().toString(), newName.trim(), newClass.trim(), finalCode))
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                Text(text = notice.orEmpty(), color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.Center) {
                Button(onClick = { showSendDialog = true }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Send New Message")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Message History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            if (studentMessages.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No messages sent yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(studentMessages) { msg ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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

private fun parseStudentRows(bytes: ByteArray): List<List<String>> {
    if (bytes.isEmpty()) return emptyList()
    return if (bytes.size >= 2 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()) {
        parseXlsxRows(bytes)
    } else {
        parseTextRows(bytes)
    }
}

private fun parseXlsxRows(bytes: ByteArray): List<List<String>> {
    val entries = linkedMapOf<String, ByteArray>()
    ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
        var entry = zip.nextEntry
        while (entry != null) {
            if (!entry.isDirectory) entries[entry.name] = zip.readBytes()
            zip.closeEntry()
            entry = zip.nextEntry
        }
    }

    val sheetName = entries.keys
        .filter { it.startsWith("xl/worksheets/sheet") && it.endsWith(".xml") }
        .sortedBy { Regex("sheet(\\d+)\\.xml").find(it)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: Int.MAX_VALUE }
        .firstOrNull() ?: return emptyList()

    val sharedStrings = entries["xl/sharedStrings.xml"]?.let { parseSharedStrings(it) }.orEmpty()
    return parseWorksheet(entries[sheetName] ?: return emptyList(), sharedStrings)
}

private fun parseSharedStrings(bytes: ByteArray): List<String> {
    val result = mutableListOf<String>()
    val parser = XmlPullParserFactory.newInstance().newPullParser()
    parser.setInput(ByteArrayInputStream(bytes), "UTF-8")

    var event = parser.eventType
    var inSi = false
    var inText = false
    var builder = StringBuilder()

    while (event != XmlPullParser.END_DOCUMENT) {
        val tag = parser.name?.substringAfter(':').orEmpty()
        when (event) {
            XmlPullParser.START_TAG -> when (tag) {
                "si" -> { inSi = true; builder = StringBuilder() }
                "t" -> if (inSi) inText = true
            }
            XmlPullParser.TEXT -> if (inSi && inText) builder.append(parser.text)
            XmlPullParser.END_TAG -> when (tag) {
                "t" -> inText = false
                "si" -> { result.add(builder.toString()); inSi = false }
            }
        }
        event = parser.next()
    }
    return result
}

private fun parseWorksheet(bytes: ByteArray, sharedStrings: List<String>): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    val parser = XmlPullParserFactory.newInstance().newPullParser()
    parser.setInput(ByteArrayInputStream(bytes), "UTF-8")

    var event = parser.eventType
    var currentRow = mutableListOf<String>()
    var currentText = StringBuilder()
    var currentCellType = ""
    var currentCellIndex = 0
    var inCell = false
    var inValue = false

    while (event != XmlPullParser.END_DOCUMENT) {
        val tag = parser.name?.substringAfter(':').orEmpty()
        when (event) {
            XmlPullParser.START_TAG -> when (tag) {
                "row" -> { currentRow = mutableListOf(); currentCellIndex = 0 }
                "c" -> {
                    inCell = true
                    currentText = StringBuilder()
                    currentCellType = parser.getAttributeValue(null, "t") ?: ""
                    val colIndex = excelColumnIndex(parser.getAttributeValue(null, "r").orEmpty())
                    if (colIndex >= 0) {
                        while (currentCellIndex < colIndex) {
                            currentRow.add("")
                            currentCellIndex++
                        }
                    }
                }
                "v", "t" -> if (inCell) inValue = true
            }
            XmlPullParser.TEXT -> if (inCell && inValue) currentText.append(parser.text)
            XmlPullParser.END_TAG -> when (tag) {
                "v", "t" -> inValue = false
                "c" -> {
                    var value = currentText.toString()
                    if (currentCellType == "s") value = value.toIntOrNull()?.let { sharedStrings.getOrNull(it) }.orEmpty()
                    currentRow.add(value.trim())
                    currentCellIndex++
                    inCell = false
                    inValue = false
                    currentText = StringBuilder()
                    currentCellType = ""
                }
                "row" -> if (currentRow.any { it.isNotBlank() }) rows.add(currentRow)
            }
        }
        event = parser.next()
    }

    return rows
}

private fun excelColumnIndex(ref: String): Int {
    val letters = ref.takeWhile { it.isLetter() }.uppercase(Locale.ROOT)
    if (letters.isBlank()) return -1
    var value = 0
    for (letter in letters) value = value * 26 + (letter - 'A' + 1)
    return value - 1
}

private fun parseTextRows(bytes: ByteArray): List<List<String>> {
    val text = decodeText(bytes)
    if (text.isBlank() || isCorruptedText(text)) return emptyList()

    val lines = text
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .split('\n')
        .map { it.trim().trim('\uFEFF') }
        .filter { it.isNotBlank() }

    if (lines.isEmpty()) return emptyList()

    val delimiter = listOf(',', ';', '\t', '|').maxByOrNull { delimiter ->
        lines.take(10).sumOf { splitLine(it, delimiter).size }
    } ?: ','

    return lines.map { splitLine(it, delimiter).map { cell -> cell.trim().trim('\uFEFF') } }
}

private fun decodeText(bytes: ByteArray): String {
    if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
        return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
    }
    if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
        return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
    }
    if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
        return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
    }

    val charsets = listOf(
        Charsets.UTF_8,
        Charset.forName("windows-1256"),
        Charset.forName("ISO-8859-6"),
        Charset.forName("windows-1252"),
        Charsets.UTF_16LE,
        Charsets.UTF_16BE
    )
    return charsets.map { String(bytes, it) }.maxByOrNull { scoreDecodedText(it) }.orEmpty()
}

private fun scoreDecodedText(text: String): Int {
    if (text.isBlank()) return -10000
    if (text.contains("xl/worksheets", ignoreCase = true) || text.contains("[Content_Types]", ignoreCase = true)) return -10000
    val replacement = text.count { it == '�' }
    val tableMarks = text.count { it == ',' || it == ';' || it == '\t' || it == '|' || it == '\n' }
    val useful = text.count { it.isLetterOrDigit() || !it.isISOControl() }
    return useful + tableMarks * 10 - replacement * 500
}

private fun isCorruptedText(text: String): Boolean {
    return text.contains('�') || text.contains("xl/worksheets", ignoreCase = true) || text.contains("[Content_Types]", ignoreCase = true)
}

private fun splitLine(line: String, delimiter: Char): List<String> {
    val cells = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
        val ch = line[i]
        if (ch == '"') {
            if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                current.append('"')
                i++
            } else {
                inQuotes = !inQuotes
            }
        } else if (ch == delimiter && !inQuotes) {
            cells.add(current.toString())
            current.clear()
        } else {
            current.append(ch)
        }
        i++
    }
    cells.add(current.toString())
    return cells
}

private fun importStudentRows(inputRows: List<List<String>>, students: MutableList<Student>): Int {
    val rows = inputRows.map { row -> row.map { it.trim() } }.filter { row -> row.any { it.isNotBlank() } }
    if (rows.isEmpty()) return 0

    var dataRows = rows
    var classIndex = 0
    var nameIndex = if (rows.first().size >= 2) 1 else 0
    var codeIndex = 2

    val firstRow = rows.first().map { normalizeHeader(it) }
    val classHeaderIndex = firstRow.indexOfFirst { isClassHeader(it) }
    val nameHeaderIndex = firstRow.indexOfFirst { isNameHeader(it) }
    val codeHeaderIndex = firstRow.indexOfFirst { isCodeHeader(it) }
    val hasHeader = classHeaderIndex >= 0 || nameHeaderIndex >= 0 || codeHeaderIndex >= 0

    if (hasHeader) {
        if (classHeaderIndex >= 0) classIndex = classHeaderIndex
        if (nameHeaderIndex >= 0) nameIndex = nameHeaderIndex
        if (codeHeaderIndex >= 0) codeIndex = codeHeaderIndex
        dataRows = rows.drop(1)
    } else {
        when (rows.first().size) {
            1 -> { classIndex = -1; nameIndex = 0; codeIndex = -1 }
            2 -> { classIndex = 0; nameIndex = 1; codeIndex = -1 }
            else -> { classIndex = 0; nameIndex = 1; codeIndex = 2 }
        }
    }

    var added = 0
    for (row in dataRows) {
        val className = if (classIndex >= 0) row.getOrNull(classIndex).orEmpty().trim() else "Imported Class"
        val studentName = row.getOrNull(nameIndex).orEmpty().trim()
        val rawCode = if (codeIndex >= 0) row.getOrNull(codeIndex).orEmpty().trim() else ""

        if (className.isBlank() || studentName.isBlank()) continue
        if (isCorruptedText(className) || isCorruptedText(studentName) || isCorruptedText(rawCode)) continue

        val code = if (rawCode.isBlank()) nextStudentCode(students, added) else rawCode
        if (students.none { it.parentCode == code }) {
            students.add(Student(UUID.randomUUID().toString(), studentName, className, code))
            added++
        }
    }
    return added
}

private fun normalizeHeader(value: String): String {
    return value.trim().trim('\uFEFF').lowercase(Locale.ROOT).replace(" ", "_").replace("-", "_")
}

private fun isClassHeader(value: String): Boolean {
    return value in setOf("class", "class_name", "section", "grade", "level", "classe", "sınıf", "класс", "班级", "القسم", "الفصل", "المستوى") || value.contains("قسم")
}

private fun isNameHeader(value: String): Boolean {
    return value in setOf("name", "student", "student_name", "full_name", "pupil", "nom", "élève", "öğrenci", "имя", "姓名", "الاسم", "الأسماء", "اسم", "اسم_التلميذ", "الاسم_واللقب") || value.contains("اسم") || value.contains("أسماء") || value.contains("اسماء")
}

private fun isCodeHeader(value: String): Boolean {
    return value in setOf("code", "student_code", "parent_code", "id", "identifier", "الكود", "الرمز", "المعرف", "رقم") || value.contains("code") || value.contains("كود") || value.contains("رمز")
}

private fun nextStudentCode(students: List<Student>, offset: Int): String {
    var number = 1001 + students.size + offset
    var code = "MT-$number"
    while (students.any { it.parentCode == code }) {
        number++
        code = "MT-$number"
    }
    return code
}
