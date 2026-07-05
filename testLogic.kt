fun main() {
    val parsedRows = mutableListOf<List<String>>()
    parsedRows.add(listOf("القسم", "اسم التلميذ", "الكود"))
    parsedRows.add(listOf("Class A", "Ahmed", "MT-1001"))
    
    var classIdx = -1
    var nameIdx = -1
    var codeIdx = -1
    
    val classHeaders = listOf("class_name", "class name", "class", "section", "القسم")
    val nameHeaders = listOf("student_name", "student name", "student", "name", "اسم التلميذ", "الاسم")
    val codeHeaders = listOf("student_code", "student code", "code", "الكود", "الرمز")

    if (parsedRows.isNotEmpty()) {
        val firstRow = parsedRows[0].map { it.lowercase().trim() }
        
        val isHeader = firstRow.any { classHeaders.contains(it) || nameHeaders.contains(it) || codeHeaders.contains(it) }
        println("Is Header: $isHeader")
        
        if (isHeader) {
            for ((i, cell) in firstRow.withIndex()) {
                if (classHeaders.contains(cell)) classIdx = i
                else if (nameHeaders.contains(cell)) nameIdx = i
                else if (codeHeaders.contains(cell)) codeIdx = i
            }
            parsedRows.removeAt(0)
        }
    }
    
    if (classIdx == -1) classIdx = 0
    if (nameIdx == -1) nameIdx = 1
    if (codeIdx == -1) codeIdx = 2
    
    println("classIdx: $classIdx, nameIdx: $nameIdx, codeIdx: $codeIdx")
}
