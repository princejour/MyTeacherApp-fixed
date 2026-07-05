import re

with open("app/src/main/java/com/walhero/myteacher/TeacherScreens.kt", "r") as f:
    content = f.read()

old_row_block = """
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
"""

new_row_block = """
                        var added = 0
                        for (row in parsedRows) {
                            if (row.isEmpty() || row.all { it.isBlank() }) continue
                            
                            val rowStr = row.joinToString("")
                            if (rowStr.contains("xl/worksheets") || rowStr.contains("[Content_Types]") || rowStr.contains("\uFFFD")) continue
                            
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
"""

if old_row_block.strip() in content:
    content = content.replace(old_row_block.strip(), new_row_block.strip())
    print("Row patched")
else:
    print("Row old not found")

with open("app/src/main/java/com/walhero/myteacher/TeacherScreens.kt", "w") as f:
    f.write(content)

