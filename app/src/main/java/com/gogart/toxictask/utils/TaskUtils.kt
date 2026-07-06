package com.gogart.toxictask.utils

import com.gogart.toxictask.data.TaskEntity
import java.time.LocalDate

object TaskUtils {
    fun isGigaDay(tasks: List<TaskEntity>): Boolean {
        if (tasks.size < 3) return false
        val weight = tasks.sumOf { it.priority.weight }
        val done = tasks.filter { it.isCompleted }.sumOf { it.priority.weight }
        return (if (weight == 0) 0f else done.toFloat() / weight) >= 0.75f
    }

    fun calculateStreak(allTasks: List<TaskEntity>, today: LocalDate = LocalDate.now()): Int {
        val days = allTasks.groupBy { it.scheduledDate }.toSortedMap(reverseOrder())
        var streak = 0
        var checkDate = today
        
        // Якщо сьогодні ще не Гігачад - починаємо перевірку зі вчорашнього дня
        if (!isGigaDay(days[checkDate.toString()] ?: emptyList())) {
            checkDate = checkDate.minusDays(1)
        }

        while (true) {
            val dateStr = checkDate.toString()
            if (days.containsKey(dateStr)) {
                if (isGigaDay(days[dateStr]!!)) {
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
