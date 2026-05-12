package com.example.toxictask.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.toxictask.Priority
import java.time.LocalDate

enum class TaskType {
    ONE_TIME, WEEKLY, GOAL_WEEK, GOAL_MONTH
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val priority: Priority,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val scheduledDate: String = LocalDate.now().toString(), // YYYY-MM-DD
    val deadlineTime: String? = null, // HH:mm
    val notes: String = "",
    val taskType: TaskType = TaskType.ONE_TIME,
    val repeatDays: String = "" // e.g. "1,3,5" for Mon, Wed, Fri
)
