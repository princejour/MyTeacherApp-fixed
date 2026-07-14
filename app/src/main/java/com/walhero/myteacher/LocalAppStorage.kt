package com.walhero.myteacher

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class LocalAppStorage(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun loadStudents(): List<Student> = runCatching {
        val array = JSONArray(preferences.getString(KEY_STUDENTS, "[]") ?: "[]")
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    Student(
                        id = item.optString("id"),
                        name = item.optString("name"),
                        className = item.optString("className"),
                        parentCode = item.optString("parentCode")
                    )
                )
            }
        }.filter { it.id.isNotBlank() && it.name.isNotBlank() }
    }.getOrDefault(emptyList())

    fun saveStudents(students: List<Student>) {
        val array = JSONArray()
        students.forEach { student ->
            array.put(
                JSONObject()
                    .put("id", student.id)
                    .put("name", student.name)
                    .put("className", student.className)
                    .put("parentCode", student.parentCode)
            )
        }
        preferences.edit().putString(KEY_STUDENTS, array.toString()).commit()
    }

    fun loadMessages(): List<TeacherMessage> = runCatching {
        val array = JSONArray(preferences.getString(KEY_MESSAGES, "[]") ?: "[]")
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    TeacherMessage(
                        id = item.optString("id"),
                        studentId = item.optString("studentId"),
                        title = item.optString("title"),
                        body = item.optString("body"),
                        date = item.optString("date")
                    )
                )
            }
        }.filter { it.id.isNotBlank() && it.studentId.isNotBlank() }
    }.getOrDefault(emptyList())

    fun saveMessages(messages: List<TeacherMessage>) {
        val array = JSONArray()
        messages.forEach { message ->
            array.put(
                JSONObject()
                    .put("id", message.id)
                    .put("studentId", message.studentId)
                    .put("title", message.title)
                    .put("body", message.body)
                    .put("date", message.date)
            )
        }
        preferences.edit().putString(KEY_MESSAGES, array.toString()).commit()
    }

    fun loadTeacherPasscode(): String =
        preferences.getString(KEY_TEACHER_PASSCODE, DEFAULT_TEACHER_PASSCODE)
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_TEACHER_PASSCODE

    fun saveTeacherPasscode(passcode: String) {
        preferences.edit().putString(KEY_TEACHER_PASSCODE, passcode).commit()
    }

    fun loadFreeOpened(): Map<String, Set<String>> = runCatching {
        val root = JSONObject(preferences.getString(KEY_FREE_OPENED, "{}") ?: "{}")
        buildMap {
            val keys = root.keys()
            while (keys.hasNext()) {
                val studentId = keys.next()
                val values = root.optJSONArray(studentId) ?: JSONArray()
                put(
                    studentId,
                    buildSet {
                        for (index in 0 until values.length()) {
                            values.optString(index).takeIf { it.isNotBlank() }?.let(::add)
                        }
                    }
                )
            }
        }
    }.getOrDefault(emptyMap())

    fun saveFreeOpened(freeOpened: Map<String, Set<String>>) {
        val root = JSONObject()
        freeOpened.forEach { (studentId, messageIds) ->
            val values = JSONArray()
            messageIds.forEach(values::put)
            root.put(studentId, values)
        }
        preferences.edit().putString(KEY_FREE_OPENED, root.toString()).commit()
    }

    fun loadAdUnlocked(): Set<String> = runCatching {
        val array = JSONArray(preferences.getString(KEY_AD_UNLOCKED, "[]") ?: "[]")
        buildSet {
            for (index in 0 until array.length()) {
                array.optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }
    }.getOrDefault(emptySet())

    fun saveAdUnlocked(messageIds: Set<String>) {
        val array = JSONArray()
        messageIds.forEach(array::put)
        preferences.edit().putString(KEY_AD_UNLOCKED, array.toString()).commit()
    }

    private companion object {
        const val PREFERENCES_NAME = "my_teacher_local_data"
        const val KEY_STUDENTS = "students"
        const val KEY_MESSAGES = "messages"
        const val KEY_TEACHER_PASSCODE = "teacher_passcode"
        const val KEY_FREE_OPENED = "free_opened"
        const val KEY_AD_UNLOCKED = "ad_unlocked"
        const val DEFAULT_TEACHER_PASSCODE = "teacher123"
    }
}
