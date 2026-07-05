import re

with open("app/src/main/java/com/walhero/myteacher/TeacherScreens.kt", "r") as f:
    content = f.read()

old_encoding_block = """
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
"""

new_encoding_block = """
                    } else {
                        var contentStr = ""
                        
                        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
                            contentStr = String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
                        } else if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
                            contentStr = String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
                        } else if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
                            contentStr = String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
                        } else {
                            val encodings = listOf("UTF-8", "windows-1256", "ISO-8859-6", "windows-1252", "UTF-16")
                            for (enc in encodings) {
                                try {
                                    val str = String(bytes, java.nio.charset.Charset.forName(enc))
                                    if (!str.contains("\uFFFD")) {
                                        contentStr = str
                                        break
                                    }
                                } catch (e: Exception) {}
                            }
                            if (contentStr.isEmpty()) {
                                contentStr = String(bytes, Charsets.UTF_8)
                            }
                        }

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
"""

if old_encoding_block.strip() in content:
    content = content.replace(old_encoding_block.strip(), new_encoding_block.strip())
    print("Encoding patched")
else:
    print("Encoding old not found")
    with open("debug_old.txt", "w") as f:
        f.write(old_encoding_block.strip())
    with open("debug_content.txt", "w") as f:
        f.write(content)

with open("app/src/main/java/com/walhero/myteacher/TeacherScreens.kt", "w") as f:
    f.write(content)

