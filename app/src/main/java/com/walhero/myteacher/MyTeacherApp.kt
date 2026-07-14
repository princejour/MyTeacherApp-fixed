package com.walhero.myteacher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun MyTeacherApp(
    canRequestAds: Boolean,
    isRewardedAdReady: Boolean,
    isPrivacyOptionsRequired: Boolean,
    onPrivacyOptionsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onShowRewardedAd: (onRewardEarned: () -> Unit, onUnavailable: (String) -> Unit) -> Unit
) {
    var screen by remember { mutableStateOf(AppScreen.Home) }
    var teacherPasscode by rememberSaveable { mutableStateOf("teacher123") }
    var activeStudent by remember { mutableStateOf<Student?>(null) }
    var freeOpened by remember { mutableStateOf<Map<String, Set<String>>>(emptyMap()) }
    var adUnlocked by remember { mutableStateOf<Set<String>>(emptySet()) }

    val students = remember { mutableStateListOf<Student>() }
    val messages = remember { mutableStateListOf<TeacherMessage>() }

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
