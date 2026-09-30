package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.TaskEntity
import com.example.data.repository.TaskRepository
import com.example.ui.model.AppLanguage
import com.example.ui.model.AppStrings
import com.example.ui.model.EnglishStrings
import com.example.ui.model.FilterStatus
import com.example.ui.model.MalayalamStrings
import com.example.ui.model.TaskCategory
import com.example.ui.model.TaskPriority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val completionPercentage: Float = 0f,
    val searchQuery: String = "",
    val selectedFilter: FilterStatus = FilterStatus.ALL,
    val selectedCategory: TaskCategory = TaskCategory.ALL,
    val editingTask: TaskEntity? = null,
    val isAddEditSheetOpen: Boolean = false,
    val showClearCompletedDialog: Boolean = false,
    val recentlyDeletedTask: TaskEntity? = null,
    val language: AppLanguage = AppLanguage.MALAYALAM
) {
    val strings: AppStrings
        get() = if (language == AppLanguage.MALAYALAM) MalayalamStrings else EnglishStrings
}

private data class DialogState(
    val editingTask: TaskEntity?,
    val isAddEditSheetOpen: Boolean,
    val showClearCompletedDialog: Boolean,
    val recentlyDeletedTask: TaskEntity?
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    private var hasInitializedSamples = false

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(FilterStatus.ALL)
    private val _selectedCategory = MutableStateFlow(TaskCategory.ALL)
    private val _editingTask = MutableStateFlow<TaskEntity?>(null)
    private val _isAddEditSheetOpen = MutableStateFlow(false)
    private val _showClearCompletedDialog = MutableStateFlow(false)
    private val _recentlyDeletedTask = MutableStateFlow<TaskEntity?>(null)
    private val _language = MutableStateFlow(AppLanguage.MALAYALAM)

    init {
        val dao = AppDatabase.getInstance(application).taskDao()
        repository = TaskRepository(dao)
    }

    private val baseTaskFlow: Flow<TaskUiState> = combine(
        repository.allTasks,
        _searchQuery,
        _selectedFilter,
        _selectedCategory
    ) { allTasks, query, filter, category ->
        if (allTasks.isEmpty() && !hasInitializedSamples) {
            hasInitializedSamples = true
            insertWelcomeTasks()
        }

        val total = allTasks.size
        val completed = allTasks.count { it.isCompleted }
        val pending = total - completed
        val percentage = if (total > 0) completed.toFloat() / total.toFloat() else 0f

        val filtered = allTasks.filter { task ->
            val matchesFilter = when (filter) {
                FilterStatus.ALL -> true
                FilterStatus.ACTIVE -> !task.isCompleted
                FilterStatus.COMPLETED -> task.isCompleted
            }
            val matchesCategory = if (category == TaskCategory.ALL) {
                true
            } else {
                task.category.equals(category.name, ignoreCase = true) ||
                task.category.equals(category.displayName, ignoreCase = true) ||
                task.category.equals(category.malayalamName, ignoreCase = true)
            }
            val matchesQuery = query.isBlank() ||
                task.title.contains(query, ignoreCase = true) ||
                task.description.contains(query, ignoreCase = true)

            matchesFilter && matchesCategory && matchesQuery
        }

        TaskUiState(
            tasks = filtered,
            totalCount = total,
            completedCount = completed,
            pendingCount = pending,
            completionPercentage = percentage,
            searchQuery = query,
            selectedFilter = filter,
            selectedCategory = category
        )
    }

    private val dialogFlow: Flow<DialogState> = combine(
        _editingTask,
        _isAddEditSheetOpen,
        _showClearCompletedDialog,
        _recentlyDeletedTask
    ) { editing, isOpen, showDialog, deleted ->
        DialogState(editing, isOpen, showDialog, deleted)
    }

    val uiState: StateFlow<TaskUiState> = combine(
        baseTaskFlow,
        dialogFlow,
        _language
    ) { baseState, dialogState, lang ->
        baseState.copy(
            editingTask = dialogState.editingTask,
            isAddEditSheetOpen = dialogState.isAddEditSheetOpen,
            showClearCompletedDialog = dialogState.showClearCompletedDialog,
            recentlyDeletedTask = dialogState.recentlyDeletedTask,
            language = lang
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskUiState(language = AppLanguage.MALAYALAM)
    )

    private fun insertWelcomeTasks() {
        viewModelScope.launch {
            repository.insert(
                TaskEntity(
                    title = "ടാസ്ക് ഫ്ലോയിലേക്ക് സ്വാഗതം! ✨",
                    description = "ടാസ്ക് പൂർത്തിയാക്കാൻ റൗണ്ട് ചെക്ക് ബോക്സിൽ അമർത്തുക. മാറ്റങ്ങൾ വരുത്താൻ ടാസ്കിൽ തൊടുക.",
                    isCompleted = false,
                    priority = TaskPriority.HIGH.name,
                    category = TaskCategory.PERSONAL.displayName,
                    dueDate = "ഇന്ന്"
                )
            )
            repository.insert(
                TaskEntity(
                    title = "ഈ ആഴ്ചയിലെ പ്രധാന ജോലികൾ 🎯",
                    description = "തീയതിയും മുൻഗണനയും നിശ്ചയിച്ച് ലക്ഷ്യങ്ങൾ പൂർത്തിയാക്കൂ.",
                    isCompleted = false,
                    priority = TaskPriority.MEDIUM.name,
                    category = TaskCategory.WORK.displayName,
                    dueDate = "നാളെ"
                )
            )
            repository.insert(
                TaskEntity(
                    title = "ഫിൽട്ടറുകളും സെർച്ചും ഉപയോഗിച്ചു നോക്കൂ 🔍",
                    description = "വിഭാഗങ്ങൾ അനുസരിച്ച് ക്രമീകരിക്കാനും തിരയാനും മുകളിലെ ഓപ്ഷനുകൾ ഉപയോഗിക്കുക.",
                    isCompleted = true,
                    priority = TaskPriority.LOW.name,
                    category = TaskCategory.IDEAS.displayName,
                    dueDate = "പൂർത്തിയായി"
                )
            )
        }
    }

    fun onToggleLanguage() {
        _language.value = if (_language.value == AppLanguage.MALAYALAM) {
            AppLanguage.ENGLISH
        } else {
            AppLanguage.MALAYALAM
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChanged(filter: FilterStatus) {
        _selectedFilter.value = filter
    }

    fun onCategoryChanged(category: TaskCategory) {
        _selectedCategory.value = category
    }

    fun onToggleCompleted(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleCompleted(task)
        }
    }

    fun onOpenAddTask() {
        _editingTask.value = null
        _isAddEditSheetOpen.value = true
    }

    fun onOpenEditTask(task: TaskEntity) {
        _editingTask.value = task
        _isAddEditSheetOpen.value = true
    }

    fun onCloseAddEditSheet() {
        _isAddEditSheetOpen.value = false
        _editingTask.value = null
    }

    fun onSaveTask(
        title: String,
        description: String,
        priority: TaskPriority,
        category: TaskCategory,
        dueDate: String
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return

        viewModelScope.launch {
            val current = _editingTask.value
            if (current != null) {
                repository.update(
                    current.copy(
                        title = trimmedTitle,
                        description = description.trim(),
                        priority = priority.name,
                        category = category.displayName,
                        dueDate = dueDate.trim()
                    )
                )
            } else {
                repository.insert(
                    TaskEntity(
                        title = trimmedTitle,
                        description = description.trim(),
                        priority = priority.name,
                        category = category.displayName,
                        dueDate = dueDate.trim(),
                        isCompleted = false
                    )
                )
            }
            onCloseAddEditSheet()
        }
    }

    fun onDeleteTask(task: TaskEntity) {
        viewModelScope.launch {
            _recentlyDeletedTask.value = task
            repository.delete(task)
            if (_editingTask.value?.id == task.id) {
                onCloseAddEditSheet()
            }
        }
    }

    fun onUndoDelete() {
        val task = _recentlyDeletedTask.value ?: return
        viewModelScope.launch {
            repository.insert(task)
            _recentlyDeletedTask.value = null
        }
    }

    fun onDismissUndo() {
        _recentlyDeletedTask.value = null
    }

    fun onShowClearCompletedDialog(show: Boolean) {
        _showClearCompletedDialog.value = show
    }

    fun onClearCompletedTasks() {
        viewModelScope.launch {
            repository.clearCompleted()
            _showClearCompletedDialog.value = false
        }
    }
}
