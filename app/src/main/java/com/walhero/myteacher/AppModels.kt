package com.walhero.myteacher

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
    ParentAccess,
    ParentInbox
}
