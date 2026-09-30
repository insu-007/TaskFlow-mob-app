package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val category: String = "Personal", // Personal, Work, Shopping, Health, Study, Ideas
    val dueDate: String = "", // e.g. "Today", "Tomorrow", "Oct 15"
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
