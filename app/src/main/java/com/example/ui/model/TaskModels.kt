package com.example.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium

enum class TaskPriority(val displayName: String, val malayalamName: String, val color: Color) {
    LOW("Low", "കുറഞ്ഞത്", PriorityLow),
    MEDIUM("Medium", "ഇടത്തരം", PriorityMedium),
    HIGH("Urgent", "അടിയന്തിരം", PriorityHigh);

    fun localizedName(language: AppLanguage): String =
        if (language == AppLanguage.MALAYALAM) malayalamName else displayName

    companion object {
        fun fromString(value: String): TaskPriority {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true) ||
                it.malayalamName.equals(value, ignoreCase = true)
            } ?: MEDIUM
        }
    }
}

enum class TaskCategory(
    val displayName: String,
    val malayalamName: String,
    val icon: ImageVector,
    val color: Color
) {
    ALL("All", "എല്ലാം", Icons.Default.Bookmark, Color(0xFF6366F1)),
    PERSONAL("Personal", "വ്യക്തിഗതം", Icons.Default.Person, Color(0xFF8B5CF6)),
    WORK("Work", "ജോലി", Icons.Default.Work, Color(0xFF3B82F6)),
    SHOPPING("Shopping", "ഷോപ്പിംഗ്", Icons.Default.ShoppingCart, Color(0xFFEC4899)),
    HEALTH("Health", "ആരോഗ്യം", Icons.Default.FitnessCenter, Color(0xFF10B981)),
    STUDY("Study", "പഠനം", Icons.Default.School, Color(0xFFF59E0B)),
    IDEAS("Ideas", "ചിന്തകൾ", Icons.Default.Lightbulb, Color(0xFF06B6D4));

    fun localizedName(language: AppLanguage): String =
        if (language == AppLanguage.MALAYALAM) malayalamName else displayName

    companion object {
        fun fromString(value: String): TaskCategory {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true) ||
                it.malayalamName.equals(value, ignoreCase = true)
            } ?: PERSONAL
        }
    }
}

enum class FilterStatus(val label: String, val malayalamLabel: String) {
    ALL("All", "എല്ലാം"),
    ACTIVE("Pending", "ബാക്കി"),
    COMPLETED("Completed", "പൂർത്തിയായത്");

    fun localizedLabel(language: AppLanguage): String =
        if (language == AppLanguage.MALAYALAM) malayalamLabel else label
}
