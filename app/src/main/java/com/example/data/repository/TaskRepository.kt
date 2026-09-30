package com.example.data.repository

import com.example.data.local.TaskDao
import com.example.data.local.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val totalCount: Flow<Int> = taskDao.getTotalCount()
    val completedCount: Flow<Int> = taskDao.getCompletedCount()

    suspend fun insert(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun update(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun delete(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun toggleCompleted(task: TaskEntity) {
        val newStatus = !task.isCompleted
        val completedAt = if (newStatus) System.currentTimeMillis() else null
        taskDao.setTaskCompleted(task.id, newStatus, completedAt)
    }

    suspend fun clearCompleted() = taskDao.clearCompletedTasks()
}
