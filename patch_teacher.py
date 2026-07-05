import re

with open("app/src/main/java/com/walhero/myteacher/TeacherScreens.kt", "r") as f:
    content = f.read()

# Define the new blocks
csv_launcher_block = """
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
                                var inT = false
                                while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                                    when (eventType) {
                                        org.xmlpull.v1.XmlPullParser.START_TAG -> {
                                            if (parser.name == "t") {
                                                inT = true
                                                currentText = ""
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
                                            } else if (parser.name == "v" || parser.name == "t" || parser.name == "is") {
                                                inV = true
                                                currentV = ""
                                            }
                                        }
                                        org.xmlpull.v1.XmlPullParser.TEXT -> {
                                            if (inV) {
                                                currentV += parser.text
                                            }
                                        }
                                        org.xmlpull.v1.XmlPullParser.END_TAG -> {
                                            if (parser.name == "v" || parser.name == "t" || parser.name == "is") {
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
                            if (!utf8Str.contains("\uFFFD")) {
                                contentStr = utf8Str
                            } else {
                                try {
                                    val cp1256Str = String(bytes, java.nio.charset.Charset.forName("windows-1256"))
                                    if (cp1256Str.any { it in '\u0600'..'\u06FF' }) {
                                        contentStr = cp1256Str
                                    } else {
                                        val isoStr = String(bytes, java.nio.charset.Charset.forName("ISO-8859-6"))
                                        if (isoStr.any { it in '\u0600'..'\u06FF' }) {
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

                        if (encodingError || contentStr.contains("\uFFFD")) {
                            fileParseError = "Please select a valid student list file."
                        } else {
                            val lines = contentStr.split(Regex("\\r?\\n")).filter { it.isNotBlank() }
                            
                            val delimiters = listOf(",", ";", "\t", "|")
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
                    val templateData = "class_name,student_name,student_code\\nClass A,Ahmed Ben Ali,MT-1001\\nClass A,Sara Ben Amor,MT-1002\\nClass B,Youssef Triki,MT-1003"
                    stream.write(templateData.toByteArray(Charsets.UTF_8))
                }
                notice = "Template downloaded successfully."
            } catch (e: Exception) {
                notice = "Error downloading template."
            }
        }
    }
"""

buttons_block = """
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { csvLauncher.launch("*/*") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Import Class")
                }
                FilledTonalButton(onClick = { createTemplateLauncher.launch("template.csv") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Template")
                }
                FilledTonalButton(onClick = { showAddDialog = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
            }
"""

# Match csvLauncher starting from `val csvLauncher =` to `val classes = students.map { it.className }.distinct()`
match = re.search(r'    val csvLauncher = rememberLauncherForActivityResult\(ActivityResultContracts\.GetContent\(\)\) \{ uri: Uri\? ->.*?    val classes = students.map \{ it.className \}.distinct\(\)', content, re.DOTALL)
if match:
    content = content[:match.start()] + csv_launcher_block + "\n    val classes = students.map { it.className }.distinct()\n" + content[match.end():]
else:
    print("Could not find csvLauncher")
    exit(1)

# Match buttons block
match_btns = re.search(r'            Row\(modifier = Modifier\.fillMaxWidth\(\), horizontalArrangement = Arrangement\.spacedBy\(8\.dp\)\) \{.*?            \}\n\s*if \(notice != null\)', content, re.DOTALL)
if match_btns:
    content = content[:match_btns.start()] + buttons_block + "\n            if (notice != null)" + content[match_btns.end():]
else:
    print("Could not find buttons block")
    exit(1)

with open("app/src/main/java/com/walhero/myteacher/TeacherScreens.kt", "w") as f:
    f.write(content)

print("Patch applied")

