package com.walhero.myteacher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.collect

@Composable
fun MyTeacherApp(
    canRequestAds: Boolean,
    isRewardedAdReady: Boolean,
    isPrivacyOptionsRequired: Boolean,
    onPrivacyOptionsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onShowRewardedAd: (onRewardEarned: () -> Unit, onUnavailable: (String) -> Unit) -> Unit
) {
    val context = LocalContext.current.applicationContext
    val storage = remember(context) { LocalAppStorage(context) }

    var screen by remember { mutableStateOf(AppScreen.Home) }
    var teacherPasscode by rememberSaveable { mutableStateOf(storage.loadTeacherPasscode()) }
    var activeStudent by remember { mutableStateOf<Student?>(null) }
    var freeOpened by remember { mutableStateOf(storage.loadFreeOpened()) }
    var adUnlocked by remember { mutableStateOf(storage.loadAdUnlocked()) }

    val students = remember {
        mutableStateListOf<Student>().apply {
            addAll(storage.loadStudents())
        }
    }
    val messages = remember {
        mutableStateListOf<TeacherMessage>().apply {
            addAll(storage.loadMessages())
        }
    }

    LaunchedEffect(students, messages) {
        snapshotFlow { students.toList() to messages.toList() }
            .collect { (studentSnapshot, messageSnapshot) ->
                storage.saveStudents(studentSnapshot)
                storage.saveMessages(messageSnapshot)
            }
    }

    LaunchedEffect(teacherPasscode) {
        storage.saveTeacherPasscode(teacherPasscode)
    }

    LaunchedEffect(freeOpened) {
        storage.saveFreeOpened(freeOpened)
    }

    LaunchedEffect(adUnlocked) {
        storage.saveAdUnlocked(adUnlocked)
    }

    when (screen) {
        AppScreen.Home -> HomeScreen(
            isPrivacyOptionsRequired = isPrivacyOptionsRequired,
            onTeacher = { screen = AppScreen.TeacherLogin },
            onParent = { screen = AppScreen.ParentAccess },
            onPrivacyOptionsClick = onPrivacyOptionsClick,
            onPrivacyPolicyClick = onPrivacyPolicyClick
        )

        AppScreen.TeacherLogin -> TeacherLoginScreen(
            passcode = teacherPasscode,
            onPasscodeChanged = { teacherPasscode = it },
            onSuccess = { screen = AppScreen.TeacherDashboard },
            onBack = { screen = AppScreen.Home }
        )

        AppScreen.TeacherDashboard -> TeacherDashboardWithDeleteScreen(
            students = students,
            messages = messages,
            onStudentClick = { student ->
                activeStudent = student
                screen = AppScreen.TeacherStudentDetails
            },
            onDeleteClass = { className ->
                val deletedStudentIds = students
                    .filter { it.className == className }
                    .map { it.id }
                    .toSet()
                val deletedMessageIds = messages
                    .filter { it.studentId in deletedStudentIds }
                    .map { it.id }
                    .toSet()

                students.removeAll { it.id in deletedStudentIds }
                messages.removeAll { it.studentId in deletedStudentIds }
                freeOpened = freeOpened - deletedStudentIds
                adUnlocked = adUnlocked - deletedMessageIds

                if (activeStudent?.id?.let { it in deletedStudentIds } == true) {
                    activeStudent = null
                }
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
            freeOpened = freeOpened,
            adUnlocked = adUnlocked,
            canRequestAds = canRequestAds,
            isRewardedAdReady = isRewardedAdReady,
            onOpenFree = { studentId, messageId ->
                val current = freeOpened[studentId].orEmpty()
                freeOpened = freeOpened + (studentId to (current + messageId))
            },
            onRewardedUnlock = { messageId, onUnavailable ->
                onShowRewardedAd(
                    { adUnlocked = adUnlocked + messageId },
                    onUnavailable
                )
            },
            onBack = { screen = AppScreen.ParentAccess }
        )
    }
}
