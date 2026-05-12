package com.example.toxictask.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.toxictask.Priority
import com.example.toxictask.R
import com.example.toxictask.ToxicStrings
import com.example.toxictask.data.AppDatabase
import com.example.toxictask.data.TaskEntity
import com.example.toxictask.data.TaskType
import com.example.toxictask.settings.LanguageCode
import com.example.toxictask.settings.NotificationSettings
import com.example.toxictask.settings.SettingsManager
import com.example.toxictask.ui.theme.ThemeMode
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

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val tasks: StateFlow<List<TaskEntity>?> = _selectedDate.flatMapLatest { date ->
        dao.getTasksByDate(date.toString()).map { list ->
            list?.sortedWith(compareBy<TaskEntity> { it.isCompleted }.thenByDescending { it.priority.weight })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val themeMode: StateFlow<ThemeMode> = settingsManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val language: StateFlow<LanguageCode> = settingsManager.languageCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LanguageCode.EN)

    val notificationSettings: StateFlow<NotificationSettings> = settingsManager.notificationSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationSettings(true, 60, 9, 0, 21, 0, true, com.example.toxictask.settings.ToxicityLevel.LOW))

    val playerStatus = tasks.map { list ->
        if (list == null) return@map PlayerRole.SLACKER
        val totalCount = list.size
        val totalWeight = list.sumOf { it.priority.weight }
        val completedWeight = list.filter { it.isCompleted }.sumOf { it.priority.weight }
        val progress = if (totalWeight == 0) 0f else completedWeight.toFloat() / totalWeight

        when {
            totalCount < 3 -> PlayerRole.SLACKER
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
            val totalCount = dayTasks.size
            val totalWeight = dayTasks.sumOf { it.priority.weight }
            val completedWeight = dayTasks.filter { it.isCompleted }.sumOf { it.priority.weight }
            val progress = if (totalWeight == 0) 0f else completedWeight.toFloat() / totalWeight
            
            val role = when {
                totalCount < 3 -> PlayerRole.SLACKER
                progress < 0.35f -> PlayerRole.SLACKER
                progress < 0.75f -> PlayerRole.WANNABE
                else -> PlayerRole.GIGACHAD
            }
            Triple(progress, totalCount, dayTasks.count { it.isCompleted }) to role
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalStats = dao.getAllTasks().map { allTasks ->
        val completed = allTasks.count { it.isCompleted }
        val total = allTasks.size
        val gigachadDays = allTasks.groupBy { it.scheduledDate }.count { (_, tasks) ->
            val weight = tasks.sumOf { it.priority.weight }
            val done = tasks.filter { it.isCompleted }.sumOf { it.priority.weight }
            tasks.size >= 3 && (if (weight == 0) 0f else done.toFloat() / weight) >= 0.75f
        }
        Triple(completed, total, gigachadDays)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Triple(0, 0, 0))

    val currentStreak = dao.getAllTasks().map { allTasks ->
        val days = allTasks.groupBy { it.scheduledDate }.toSortedMap(reverseOrder())
        var streak = 0
        var checkDate = LocalDate.now()
        
        while (true) {
            val dayTasks = days[checkDate.toString()]
            if (dayTasks == null) {
                if (checkDate == LocalDate.now()) {
                    checkDate = checkDate.minusDays(1)
                    continue
                } else break
            }
            
            val totalWeight = dayTasks.sumOf { it.priority.weight }
            val completedWeight = dayTasks.filter { it.isCompleted }.sumOf { it.priority.weight }
            val progress = if (totalWeight == 0) 0f else completedWeight.toFloat() / totalWeight
            
            if (dayTasks.size >= 3 && progress >= 0.75f) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                if (checkDate == LocalDate.now()) {
                   checkDate = checkDate.minusDays(1)
                   continue
                } else break
            }
        }
        streak
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            checkAndAddRepeatingTasks()
        }
    }

    private suspend fun checkAndAddRepeatingTasks() {
        val today = LocalDate.now()
        val todayStr = today.toString()
        val dayOfWeek = today.dayOfWeek.value.toString()
        
        val allTasks = dao.getAllTasks().first()
        val repeatingTemplates = allTasks
            .filter { it.taskType == TaskType.WEEKLY && it.repeatDays.contains(dayOfWeek) }
            .distinctBy { it.title }
        
        val todayExisting = dao.getTasksByDate(todayStr).first()
        
        repeatingTemplates.forEach { template ->
            if (todayExisting.none { it.title == template.title && it.taskType == template.taskType }) {
                dao.insertTask(TaskEntity(
                    title = template.title,
                    priority = template.priority,
                    scheduledDate = todayStr,
                    deadlineTime = template.deadlineTime,
                    notes = template.notes,
                    taskType = template.taskType,
                    repeatDays = template.repeatDays
                ))
            }
        }
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
}
