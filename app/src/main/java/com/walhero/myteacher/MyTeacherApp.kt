package com.walhero.myteacher

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
