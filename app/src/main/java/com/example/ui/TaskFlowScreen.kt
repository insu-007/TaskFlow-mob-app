package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddEditTaskSheet
import com.example.ui.components.FilterChipRow
import com.example.ui.components.TaskItemCard
import com.example.ui.components.TaskSummaryCard
import com.example.ui.model.AppLanguage
import com.example.ui.model.AppStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFlowScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = uiState.strings

    var isSearchExpanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Handle Undo Snackbar when a task is deleted
    LaunchedEffect(uiState.recentlyDeletedTask) {
        val deleted = uiState.recentlyDeletedTask
        if (deleted != null) {
            val result = snackbarHostState.showSnackbar(
                message = strings.taskDeletedMessage(deleted.title),
                actionLabel = strings.undo,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onUndoDelete()
            } else {
                viewModel.onDismissUndo()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.appName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.appSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Language Switcher Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                            .clickable { viewModel.onToggleLanguage() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("language_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.language == AppLanguage.MALAYALAM) "മലയാളം" else "EN",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { isSearchExpanded = !isSearchExpanded },
                        modifier = Modifier.testTag("search_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Clear else Icons.Default.Search,
                            contentDescription = strings.searchToggle
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("more_options_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (uiState.language == AppLanguage.MALAYALAM) "ഭാഷ മാറ്റുക (English)" else "Switch to മലയാളം"
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.onToggleLanguage()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(strings.clearCompleted) },
                                onClick = {
                                    showMenu = false
                                    viewModel.onShowClearCompletedDialog(true)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = null
                                    )
                                },
                                enabled = uiState.completedCount > 0,
                                modifier = Modifier.testTag("menu_clear_completed")
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onOpenAddTask() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(strings.newTask, fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_task_fab")
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            val contentModifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)

            Column(modifier = contentModifier) {
                // Search Field (collapsible)
                AnimatedVisibility(
                    visible = isSearchExpanded,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text(strings.searchPlaceholder) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = strings.searchClear)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_input_field")
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .animateContentSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Progress Summary Card
                    item(key = "summary_card") {
                        TaskSummaryCard(
                            totalCount = uiState.totalCount,
                            completedCount = uiState.completedCount,
                            pendingCount = uiState.pendingCount,
                            completionPercentage = uiState.completionPercentage,
                            strings = strings,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    // 2. Filter Status & Categories
                    item(key = "filters_section") {
                        FilterChipRow(
                            selectedFilter = uiState.selectedFilter,
                            selectedCategory = uiState.selectedCategory,
                            totalCount = uiState.totalCount,
                            pendingCount = uiState.pendingCount,
                            completedCount = uiState.completedCount,
                            language = uiState.language,
                            onFilterSelected = { viewModel.onFilterChanged(it) },
                            onCategorySelected = { viewModel.onCategoryChanged(it) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // 3. Task Items or Empty State
                    if (uiState.tasks.isEmpty()) {
                        item(key = "empty_state") {
                            EmptyTasksView(
                                hasTasksOverall = uiState.totalCount > 0,
                                searchQuery = uiState.searchQuery,
                                strings = strings,
                                onAddTask = { viewModel.onOpenAddTask() }
                            )
                        }
                    } else {
                        items(
                            items = uiState.tasks,
                            key = { it.id }
                        ) { task ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(spring()),
                                exit = fadeOut(spring())
                            ) {
                                TaskItemCard(
                                    task = task,
                                    language = uiState.language,
                                    onToggleCompleted = { viewModel.onToggleCompleted(task) },
                                    onEdit = { viewModel.onOpenEditTask(task) },
                                    onDelete = { viewModel.onDeleteTask(task) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Modal Bottom Sheet
    if (uiState.isAddEditSheetOpen) {
        AddEditTaskSheet(
            sheetState = sheetState,
            taskToEdit = uiState.editingTask,
            strings = strings,
            language = uiState.language,
            onDismiss = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    viewModel.onCloseAddEditSheet()
                }
            },
            onSave = { title, description, priority, category, dueDate ->
                viewModel.onSaveTask(title, description, priority, category, dueDate)
            },
            onDelete = { task ->
                viewModel.onDeleteTask(task)
            }
        )
    }

    // Confirm Clear Completed Tasks Dialog
    if (uiState.showClearCompletedDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onShowClearCompletedDialog(false) },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(strings.clearCompletedDialogTitle) },
            text = {
                Text(strings.clearCompletedDialogDesc(uiState.completedCount))
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onClearCompletedTasks() },
                    modifier = Modifier.testTag("confirm_clear_button")
                ) {
                    Text(strings.clearAll, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onShowClearCompletedDialog(false) },
                    modifier = Modifier.testTag("cancel_clear_button")
                ) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun EmptyTasksView(
    hasTasksOverall: Boolean,
    searchQuery: String,
    strings: AppStrings,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (searchQuery.isNotEmpty()) {
                strings.emptyNoMatching
            } else if (!hasTasksOverall) {
                strings.emptyListClear
            } else {
                strings.emptyFilter
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (searchQuery.isNotEmpty()) {
                strings.emptyNoMatchingDesc
            } else if (!hasTasksOverall) {
                strings.emptyListClearDesc
            } else {
                strings.emptyFilterDesc
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
