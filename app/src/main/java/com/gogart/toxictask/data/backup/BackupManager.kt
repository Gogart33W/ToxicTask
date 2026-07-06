package com.gogart.toxictask.data.backup

import android.content.Context
import android.net.Uri
import com.gogart.toxictask.data.AppDatabase
import com.gogart.toxictask.data.TaskEntity
import com.gogart.toxictask.Priority
import com.gogart.toxictask.data.TaskType
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class BackupManager(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.taskDao()

    suspend fun exportData(uri: Uri) {
        val tasks = dao.getAllTasksStatic()
        val jsonArray = JSONArray()
        tasks.forEach { task ->
            val obj = JSONObject().apply {
                put("title", task.title)
                put("priority", task.priority.name)
                put("isCompleted", task.isCompleted)
                put("timestamp", task.timestamp)
                put("scheduledDate", task.scheduledDate)
                put("deadlineTime", task.deadlineTime ?: "")
                put("notes", task.notes)
                put("taskType", task.taskType.name)
                put("repeatDays", task.repeatDays)
            }
            jsonArray.put(obj)
        }
        context.contentResolver.openOutputStream(uri)?.use { 
            it.write(jsonArray.toString(4).toByteArray())
        }
    }

    suspend fun importData(uri: Uri) {
        try {
            val content = StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        content.append(line)
                    }
                }
            }
            
            val jsonArray = JSONArray(content.toString())
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val task = TaskEntity(
                    title = obj.getString("title"),
                    priority = Priority.valueOf(obj.getString("priority")),
                    isCompleted = obj.getBoolean("isCompleted"),
                    timestamp = obj.getLong("timestamp"),
                    scheduledDate = obj.getString("scheduledDate"),
                    deadlineTime = obj.optString("deadlineTime").takeIf { it.isNotBlank() },
                    notes = obj.optString("notes", ""),
                    taskType = TaskType.valueOf(obj.optString("taskType", "ONE_TIME")),
                    repeatDays = obj.optString("repeatDays", "")
                )
                dao.insertTask(task)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // In a real app, we'd throw a custom exception or use a Result type to show a UI error
        }
    }
}
