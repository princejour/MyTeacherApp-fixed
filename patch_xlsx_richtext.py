import re

with open("app/src/main/java/com/walhero/myteacher/TeacherScreens.kt", "r") as f:
    content = f.read()

shared_strings_old = """
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
"""

shared_strings_new = """
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
"""

sheet_old = """
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
"""

sheet_new = """
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
"""

if shared_strings_old.strip() in content:
    content = content.replace(shared_strings_old.strip(), shared_strings_new.strip())
    print("Shared strings patched")
else:
    print("Shared strings old not found")

if sheet_old.strip() in content:
    content = content.replace(sheet_old.strip(), sheet_new.strip())
    print("Sheet patched")
else:
    print("Sheet old not found")

with open("app/src/main/java/com/walhero/myteacher/TeacherScreens.kt", "w") as f:
    f.write(content)

