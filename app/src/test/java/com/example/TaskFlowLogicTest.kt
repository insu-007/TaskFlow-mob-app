package com.example

import com.example.ui.model.AppLanguage
import com.example.ui.model.FilterStatus
import com.example.ui.model.TaskCategory
import com.example.ui.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskFlowLogicTest {

    @Test
    fun testPriorityParsing() {
        assertEquals(TaskPriority.HIGH, TaskPriority.fromString("HIGH"))
        assertEquals(TaskPriority.HIGH, TaskPriority.fromString("high"))
        assertEquals(TaskPriority.HIGH, TaskPriority.fromString("അടിയന്തിരം"))
        assertEquals(TaskPriority.MEDIUM, TaskPriority.fromString("MEDIUM"))
        assertEquals(TaskPriority.MEDIUM, TaskPriority.fromString("ഇടത്തരം"))
        assertEquals(TaskPriority.LOW, TaskPriority.fromString("LOW"))
        assertEquals(TaskPriority.LOW, TaskPriority.fromString("കുറഞ്ഞത്"))
        assertEquals(TaskPriority.MEDIUM, TaskPriority.fromString("UNKNOWN"))
    }

    @Test
    fun testCategoryParsing() {
        assertEquals(TaskCategory.WORK, TaskCategory.fromString("Work"))
        assertEquals(TaskCategory.WORK, TaskCategory.fromString("ജോലി"))
        assertEquals(TaskCategory.PERSONAL, TaskCategory.fromString("Personal"))
        assertEquals(TaskCategory.PERSONAL, TaskCategory.fromString("വ്യക്തിഗതം"))
        assertEquals(TaskCategory.SHOPPING, TaskCategory.fromString("Shopping"))
        assertEquals(TaskCategory.PERSONAL, TaskCategory.fromString("Random"))
    }

    @Test
    fun testFilterLabels() {
        assertEquals("All", FilterStatus.ALL.localizedLabel(AppLanguage.ENGLISH))
        assertEquals("എല്ലാം", FilterStatus.ALL.localizedLabel(AppLanguage.MALAYALAM))
        assertEquals("Pending", FilterStatus.ACTIVE.localizedLabel(AppLanguage.ENGLISH))
        assertEquals("ബാക്കി", FilterStatus.ACTIVE.localizedLabel(AppLanguage.MALAYALAM))
        assertEquals("Completed", FilterStatus.COMPLETED.localizedLabel(AppLanguage.ENGLISH))
        assertEquals("പൂർത്തിയായത്", FilterStatus.COMPLETED.localizedLabel(AppLanguage.MALAYALAM))
    }
}
