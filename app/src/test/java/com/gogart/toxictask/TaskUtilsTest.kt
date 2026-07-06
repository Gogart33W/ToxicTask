package com.gogart.toxictask

import com.gogart.toxictask.data.TaskEntity
import com.gogart.toxictask.utils.TaskUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TaskUtilsTest {

    @Test
    fun testCalculateStreak_ContinuousSuccess() {
        val today = LocalDate.now()
        val tasks = listOf(
            createGigaDay(today),
            createGigaDay(today.minusDays(1)),
            createGigaDay(today.minusDays(2))
        ).flatten()
        
        assertEquals(3, TaskUtils.calculateStreak(tasks, today))
    }

    @Test
    fun testCalculateStreak_WithEmptyDays() {
        val today = LocalDate.now()
        val tasks = listOf(
            createGigaDay(today),
            // Day -1 is empty (rollover)
            createGigaDay(today.minusDays(2))
        ).flatten()
        
        assertEquals(2, TaskUtils.calculateStreak(tasks, today))
    }

    @Test
    fun testCalculateStreak_BrokenByFailure() {
        val today = LocalDate.now()
        val tasks = listOf(
            createGigaDay(today),
            createLazyDay(today.minusDays(1)),
            createGigaDay(today.minusDays(2))
        ).flatten()
        
        assertEquals(1, TaskUtils.calculateStreak(tasks, today))
    }

    private fun createGigaDay(date: LocalDate): List<TaskEntity> {
        return List(3) { 
            TaskEntity(title = "Task", priority = Priority.HARD, isCompleted = true, scheduledDate = date.toString()) 
        }
    }

    private fun createLazyDay(date: LocalDate): List<TaskEntity> {
        return List(3) { 
            TaskEntity(title = "Task", priority = Priority.LOW, isCompleted = false, scheduledDate = date.toString()) 
        }
    }
}
