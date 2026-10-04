package com.gogart.toxictask.utils

import com.gogart.toxictask.data.TaskDao
import com.gogart.toxictask.data.TaskEntity
import com.gogart.toxictask.data.TaskType
import kotlinx.coroutines.flow.first
import java.time.LocalDate

object TaskUtils {
    fun isGigaDay(tasks: List<TaskEntity>, minTasks: Int = 3): Boolean {
        if (tasks.isEmpty()) return false
        
        // Всі таски (включаючи перенесені) рахуються в загальну кількість для перевірки на ЛОХ-статус (мін 3)
        if (tasks.size < minTasks) return false
        
        // Але для прогресу перенесені таски ігноруються (вони не дають % і не заважають його досягти)
        val validTasks = tasks.filter { !it.isRolledOver }
        if (validTasks.isEmpty()) return false
        
        val weight = validTasks.sumOf { it.priority.weight }
        val done = validTasks.filter { it.isCompleted }.sumOf { it.priority.weight }
        return (done.toFloat() / weight) >= 0.75f
    }

    suspend fun checkAndAddRepeatingTasks(dao: TaskDao) {
        val today = LocalDate.now()
        val todayStr = today.toString()
        val dayOfWeek = today.dayOfWeek.value.toString()
        
        val allTasks = dao.getAllTasksStatic()
        val repeatingTemplates = allTasks
            .filter { it.taskType == TaskType.WEEKLY && it.repeatDays.contains(dayOfWeek) }
            .distinctBy { it.title }
        
        val todayExisting = dao.getTasksByDate(todayStr).first()
        
        repeatingTemplates.forEach { template ->
            if (todayExisting.none { it.title == template.title && it.taskType == template.taskType }) {
                dao.insertTask(TaskEntity(
                    title = template.title,
                    priority = template.priority,
                    scheduledDate = todayStr,
                    deadlineTime = template.deadlineTime,
                    notes = "",
                    taskType = template.taskType,
                    repeatDays = template.repeatDays
                ))
            }
        }
    }

    fun calculateStreak(allTasks: List<TaskEntity>, today: LocalDate = LocalDate.now(), minTasks: Int = 3): Int {
        val days = allTasks.groupBy { it.scheduledDate }.toSortedMap(reverseOrder())
        var streak = 0
        var checkDate = today
        
        // Якщо сьогодні ще не Гігачад - починаємо перевірку зі вчорашнього дня
        if (!isGigaDay(days[checkDate.toString()] ?: emptyList(), minTasks)) {
            checkDate = checkDate.minusDays(1)
        }

        while (true) {
            val dateStr = checkDate.toString()
            if (days.containsKey(dateStr)) {
                if (isGigaDay(days[dateStr]!!, minTasks)) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                } else {
                    // День був, але не Гігачад - вогонь гасне
                    break
                }
            } else {
                // День порожній (можливо був перенос або просто нічого не планували)
                // Шукаємо активність на тиждень назад
                var hasAnythingBefore = false
                var lookBack = checkDate.minusDays(1)
                for (i in 1..7) {
                    if (days.containsKey(lookBack.toString())) {
                        hasAnythingBefore = true
                        break
                    }
                    lookBack = lookBack.minusDays(1)
                }
                
                if (hasAnythingBefore) {
                    checkDate = checkDate.minusDays(1)
                    continue
                } else break
            }
        }
        return streak
    }
}
