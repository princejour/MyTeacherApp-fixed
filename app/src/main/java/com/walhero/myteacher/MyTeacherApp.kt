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
    var freeOpened by remember { mutableStateOf<Map<String, Set<String>>>(emptyMap()) }
    var adUnlocked by remember { mutableStateOf<Set<String>>(emptySet()) }

    val students = remember {
        mutableStateListOf(
            Student("s1", "Emma Johnson", "Class A", "MT-1001"),
            Student("s2", "Noah Smith", "Class A", "MT-1002"),
            Student("s3", "Olivia Brown", "Class B", "MT-1003")
        )
    }

    val messages = remember {
        mutableStateListOf(
            TeacherMessage("m1", "s1", "Homework reminder", "Please review today's reading activity at home.", "Today"),
            TeacherMessage("m2", "s1", "Class note", "Emma participated well during the science activity.", "Today"),
            TeacherMessage("m3", "s1", "Materials", "Please bring colored pencils tomorrow.", "Today"),
            TeacherMessage("m4", "s1", "Reminder", "The class project is due next week.", "Today"),
            TeacherMessage("m5", "s1", "Positive update", "Excellent effort during group work.", "Today"),
            TeacherMessage("m6", "s1", "Extra message", "This message is locked after the free monthly limit.", "Today")
        )
    }

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
            onBack = { screen = AppScreen.Home }
        )

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
            freeOpened = freeOpened,
            adUnlocked = adUnlocked,
            onOpenFree = { studentId, messageId ->
                val current = freeOpened[studentId].orEmpty()
                freeOpened = freeOpened + (studentId to (current + messageId))
            },
            onRewardedUnlock = { messageId ->
                adUnlocked = adUnlocked + messageId
            },
            onBack = { screen = AppScreen.ParentAccess }
        )
    }
}
