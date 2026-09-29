package com.gogart.toxictask.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE scheduledDate = :date ORDER BY timestamp DESC")
    fun getTasksByDate(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksStatic(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isRolledOver = 0 AND scheduledDate = :date")
    fun observeUncompletedTasksOn(date: String): Flow<List<TaskEntity>>

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}
