package com.gogart.toxictask.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gogart.toxictask.Priority
import com.gogart.toxictask.R
import com.gogart.toxictask.ToxicStrings
import com.gogart.toxictask.data.AppDatabase
import com.gogart.toxictask.data.TaskEntity
import com.gogart.toxictask.data.TaskType
import com.gogart.toxictask.settings.LanguageCode
import com.gogart.toxictask.settings.NotificationSettings
import com.gogart.toxictask.data.backup.BackupManager
import com.gogart.toxictask.settings.SettingsManager
import com.gogart.toxictask.ui.theme.ThemeMode
import com.gogart.toxictask.utils.TaskUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class PlayerRole(val labelRes: Int, val key: String) {
    SLACKER(R.string.role_slacker, "SLACKER"),
    WANNABE(R.string.role_wannabe, "WANNABE"),
    GIGACHAD(R.string.role_gigachad, "GIGACHAD")
}

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.taskDao()
    private val settingsManager = SettingsManager(application)
    private val backupManager = BackupManager(application)

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _ignoredRollover = MutableStateFlow(false)
    val pendingRolloverTasks: StateFlow<List<TaskEntity>> = dao.observeUncompletedTasksBefore(LocalDate.now().toString())
        .combine(_ignoredRollover) { tasks, ignored ->
            if (ignored) emptyList() else tasks
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringTemplates: Flow<List<TaskEntity>> = dao.getAllTasks().map { all ->
        all.filter { it.taskType == TaskType.WEEKLY }.distinctBy { it.title }
    }

    val tasks: StateFlow<List<TaskEntity>?> = _selectedDate.flatMapLatest { date ->
        dao.getTasksByDate(date.toString()).map { list ->
            list.sortedWith(compareBy<TaskEntity> { it.isCompleted }.thenByDescending { it.priority.weight })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val themeMode: StateFlow<ThemeMode> = settingsManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val language: StateFlow<LanguageCode> = settingsManager.languageCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LanguageCode.EN)

    val notificationSettings: StateFlow<NotificationSettings> = settingsManager.notificationSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationSettings(true, 60, 9, 0, 21, 0, true, com.gogart.toxictask.settings.ToxicityLevel.LOW))

    val playerStatus = tasks.map { list ->
        if (list == null) return@map PlayerRole.SLACKER
        val activeTasks = list.filter { !it.isRolledOver }
        val totalWeight = activeTasks.sumOf { it.priority.weight }
        val completedWeight = activeTasks.filter { it.isCompleted }.sumOf { it.priority.weight }
        val progress = if (totalWeight == 0) 0f else completedWeight.toFloat() / totalWeight

        when {
            list.size < 3 -> PlayerRole.SLACKER
            progress < 0.35f -> PlayerRole.SLACKER
            progress < 0.75f -> PlayerRole.WANNABE
            else -> PlayerRole.GIGACHAD
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerRole.SLACKER)

    val toxicInsult = combine(tasks, language, playerStatus, notificationSettings) { list, lang, role, settings ->
        if (list == null) return@combine "..."
        val totalCount = list.size
        val toxicity = settings.toxicityLevel
        
        when {
            totalCount == 0 -> ToxicStrings.getEmptyInsults(lang, toxicity).random()
            totalCount < 3 -> {
                ToxicStrings.getTooFewTasksInsult(totalCount, lang, toxicity)
            }
            else -> ToxicStrings.getInsults(lang, toxicity, role.key).random()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "...")

    val historyData = dao.getAllTasks().map { allTasks ->
        allTasks.groupBy { it.scheduledDate }.mapValues { (_, dayTasks) ->
            val activeTasks = dayTasks.filter { !it.isRolledOver }
            val totalWeight = activeTasks.sumOf { it.priority.weight }
            val completedWeight = activeTasks.filter { it.isCompleted }.sumOf { it.priority.weight }
            val progress = if (totalWeight == 0) 0f else completedWeight.toFloat() / totalWeight
            
            val role = when {
                dayTasks.size < 3 -> PlayerRole.SLACKER
                progress < 0.35f -> PlayerRole.SLACKER
                progress < 0.75f -> PlayerRole.WANNABE
                else -> PlayerRole.GIGACHAD
            }
            Triple(progress, dayTasks.size, dayTasks.count { it.isCompleted && !it.isRolledOver }) to role
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalStats = dao.getAllTasks().map { allTasks ->
        val completed = allTasks.count { it.isCompleted }
        val total = allTasks.size
        val gigachadDays = allTasks.groupBy { it.scheduledDate }.count { (_, tasks) ->
            TaskUtils.isGigaDay(tasks)
        }
        Triple(completed, total, gigachadDays)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Triple(0, 0, 0))

    val currentStreak = dao.getAllTasks().map { allTasks ->
        TaskUtils.calculateStreak(allTasks)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            checkAndAddRepeatingTasks()
        }
    }

    fun rolloverTasks(selectedTasks: List<TaskEntity>) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val todayTasks = dao.getTasksByDate(today).first()
            
            selectedTasks.forEach { task ->
                // Перевіряємо чи це повторювана задача і чи вона вже є сьогодні
                val alreadyExists = task.taskType == TaskType.WEEKLY && 
                                    todayTasks.any { it.title == task.title && it.taskType == TaskType.WEEKLY }
                
                if (!alreadyExists) {
                    // Створюємо копію на сьогодні
                    dao.insertTask(task.copy(
                        id = 0, 
                        scheduledDate = today, 
                        isCompleted = false, 
                        isRolledOver = false,
                        timestamp = System.currentTimeMillis()
                    ))
                }
                
                // Стару задачу маркуємо як перенесену
                dao.updateTask(task.copy(isRolledOver = true))
            }
        }
    }

    fun clearRollover() {
        _ignoredRollover.value = true
    }

    private suspend fun checkAndAddRepeatingTasks() {
        TaskUtils.checkAndAddRepeatingTasks(dao)
    }

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsManager.setThemeMode(mode) }
    }

    fun setLanguage(lang: LanguageCode) {
        viewModelScope.launch {
            settingsManager.setLanguage(lang)
            updateLocale(lang)
        }
    }

    fun setNotificationSettings(settings: NotificationSettings) {
        viewModelScope.launch { settingsManager.updateNotificationSettings(settings) }
    }

    private fun updateLocale(lang: LanguageCode) {
        val locale = Locale(lang.code)
        Locale.setDefault(locale)
    }

    fun addTask(title: String, priority: Priority, deadline: String? = null, notes: String = "", type: TaskType = TaskType.ONE_TIME, repeatDays: String = "") {
        viewModelScope.launch {
            settingsManager.setLastTaskAddedTime(System.currentTimeMillis())
            val date = _selectedDate.value
            val scheduledDate = when (type) {
                TaskType.GOAL_WEEK -> date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toString()
                TaskType.GOAL_MONTH -> date.with(TemporalAdjusters.lastDayOfMonth()).toString()
                TaskType.WEEKLY -> {
                    val days = repeatDays.split(",").filter { it.isNotEmpty() }.map { it.toInt() }
                    if (days.isEmpty()) date.toString()
                    else {
                        var nextDate = date
                        var found = false
                        for (i in 0..7) {
                            if (days.contains(nextDate.dayOfWeek.value)) {
                                found = true
                                break
                            }
                            nextDate = nextDate.plusDays(1)
                        }
                        if (found) nextDate.toString() else date.toString()
                    }
                }
                else -> date.toString()
            }

            dao.insertTask(TaskEntity(
                title = title, 
                priority = priority, 
                scheduledDate = scheduledDate,
                deadlineTime = deadline,
                notes = notes,
                taskType = type,
                repeatDays = repeatDays
            ))
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            dao.updateTask(task)
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            dao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            dao.deleteTask(task)
        }
    }

    fun deleteRecurringTemplate(title: String) {
        viewModelScope.launch {
            val all = dao.getAllTasks().first()
            val today = LocalDate.now()
            all.filter { it.title == title && it.taskType == TaskType.WEEKLY }.forEach { task ->
                val taskDate = try {
                    LocalDate.parse(task.scheduledDate)
                } catch (e: Exception) {
                    today
                }
                
                if (taskDate.isAfter(today)) {
                    dao.deleteTask(task)
                } else {
                    // Перетворюємо старі таски на звичайні, щоб вони лишилися в історії,
                    // але перестали бути шаблонами для нових днів.
                    dao.updateTask(task.copy(taskType = TaskType.ONE_TIME))
                }
            }
        }
    }

    fun updateRecurringTemplate(oldTitle: String, newTask: TaskEntity) {
        viewModelScope.launch {
            val all = dao.getAllTasks().first()
            all.filter { it.title == oldTitle && it.taskType == TaskType.WEEKLY }.forEach {
                dao.updateTask(it.copy(
                    title = newTask.title,
                    priority = newTask.priority,
                    deadlineTime = newTask.deadlineTime,
                    notes = newTask.notes,
                    repeatDays = newTask.repeatDays
                ))
            }
        }
    }

    fun exportTasks(uri: android.net.Uri) {
        viewModelScope.launch {
            backupManager.exportData(uri)
        }
    }

    fun importTasks(uri: android.net.Uri) {
        viewModelScope.launch {
            backupManager.importData(uri)
        }
    }

    // --- DEBUG METHODS ---
    fun debugResetLastTaskTime() {
        viewModelScope.launch {
            val fourDaysAgo = System.currentTimeMillis() - (4 * 24 * 60 * 60 * 1000L)
            settingsManager.setLastTaskAddedTime(fourDaysAgo)
        }
    }

    fun debugAddPastUncompletedTask() {
        viewModelScope.launch {
            _ignoredRollover.value = false
            val yesterday = LocalDate.now().minusDays(1).toString()
            dao.insertTask(TaskEntity(
                title = "DEBUG: Past Ghost Task",
                priority = Priority.HARD,
                scheduledDate = yesterday,
                isCompleted = false
            ))
        }
    }

    fun debugClearAllTasks() {
        viewModelScope.launch {
            dao.deleteAll()
        }
    }

    fun debugForceGigachad() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            repeat(3) { i ->
                dao.insertTask(TaskEntity(
                    title = "DEBUG: Giga Task $i",
                    priority = Priority.HARD,
                    scheduledDate = today,
                    isCompleted = true
                ))
            }
        }
    }

    fun debugSetDeadlineSoon() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val soon = java.time.LocalTime.now().plusMinutes(5).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
            dao.insertTask(TaskEntity(
                title = "DEBUG: Deadline Soon",
                priority = Priority.HARD,
                scheduledDate = today,
                deadlineTime = soon,
                isCompleted = false
            ))
        }
    }

    fun debugResetNotifyTime() {
        viewModelScope.launch {
            settingsManager.setLastNotifyTime(0)
        }
    }
}
