package com.example.toxictask

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toxictask.data.TaskEntity
import com.example.toxictask.data.TaskType
import com.example.toxictask.settings.LanguageCode
import com.example.toxictask.settings.NotificationSettings
import com.example.toxictask.settings.ToxicityLevel
import com.example.toxictask.ui.theme.ThemeMode
import com.example.toxictask.ui.theme.ToxicTaskTheme
import com.example.toxictask.viewmodel.PlayerRole
import com.example.toxictask.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// Helper to find Activity even through localized context
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun LocaleWrapper(langCode: String, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val locale = remember(langCode) { Locale.forLanguageTag(langCode) }
    
    val configuration = remember(locale) {
        Configuration(context.resources.configuration).apply {
            setLocale(locale)
            setLocales(LocaleList(locale))
        }
    }
    
    val localizedContext = remember(configuration) {
        val newContext = context.createConfigurationContext(configuration)
        // Wrap to preserve the activity reference for findActivity()
        object : android.content.ContextWrapper(newContext) {
            override fun getBaseContext(): Context = context
        }
    }

    LaunchedEffect(locale) {
        Locale.setDefault(locale)
        // Update resources for the whole application to be safe
        val res = context.resources
        res.updateConfiguration(configuration, res.displayMetrics)
    }
    
    CompositionLocalProvider(
        LocalConfiguration provides configuration,
        LocalContext provides localizedContext
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToxicTaskScreen(viewModel: TaskViewModel = viewModel()) {
    val themeMode by viewModel.themeMode.collectAsState()
    val currentLang by viewModel.language.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val tasksNonNull = tasks ?: emptyList()
    val insult by viewModel.toxicInsult.collectAsState()
    val notifySettings by viewModel.notificationSettings.collectAsState()
    val playerRole by viewModel.playerStatus.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val historyData by viewModel.historyData.collectAsState()
    val streak by viewModel.currentStreak.collectAsState()
    
    var currentTab by remember { mutableIntStateOf(0) }

    val isDark = when(themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    var showTaskSheet by remember { mutableStateOf<TaskEntity?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    
    val totalWeight = tasksNonNull.sumOf { it.priority.weight }
    val completedWeight = tasksNonNull.filter { it.isCompleted }.sumOf { it.priority.weight }
    val progress = if (totalWeight == 0) 0f else completedWeight.toFloat() / totalWeight

    Scaffold(
                floatingActionButton = {
                    if (currentTab == 0) {
                        FloatingActionButton(
                            onClick = { 
                                isEditing = false
                                showTaskSheet = TaskEntity(title = "", priority = Priority.LOW, scheduledDate = selectedDate.toString()) 
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(32.dp))
                        }
                    }
                },
                topBar = {
                    TopAppBar(
                        title = { Text("TOXIC TASK", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                        actions = {
                            IconButton(onClick = { showSettings = true }) {
                                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = { currentTab = 0 },
                            icon = { Icon(Icons.Rounded.Assignment, null) },
                            label = { Text(stringResource(R.string.tasks)) }
                        )
                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { currentTab = 1 },
                            icon = { Icon(Icons.Rounded.History, null) },
                            label = { Text(stringResource(R.string.history)) }
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (currentTab == 0) {
                        Column {
                            DateSelector(selectedDate, onDateSelected = { viewModel.setDate(it) })
                            StatusDashboard(playerRole, currentLang, progress, insult, streak, isDark, notifySettings.toxicityLevel)
                            Text(
                                stringResource(R.string.current_objectives),
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            if (tasksNonNull.isEmpty()) {
                                EmptyState()
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(tasksNonNull, key = { it.id }) { task ->
                                        TaskCard(
                                            task = task,
                                            isDark = isDark,
                                            onCheckedChange = { _ -> viewModel.toggleTask(task) },
                                            onDelete = { viewModel.deleteTask(task) },
                                            onEdit = {
                                                isEditing = true
                                                showTaskSheet = task
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        HistoryScreen(historyData, currentLang, viewModel, isDark) { date ->
                            viewModel.setDate(date)
                            currentTab = 0
                        }
                    }
                }

                if (showSettings) {
                    SettingsDialog(
                        currentTheme = themeMode,
                        currentLang = currentLang,
                        notifySettings = notifySettings,
                        isDark = isDark,
                        onDismiss = { showSettings = false },
                        onThemeChange = { viewModel.setThemeMode(it) },
                        onLangChange = { viewModel.setLanguage(it) },
                        onNotifySettingsChange = { viewModel.setNotificationSettings(it) }
                    )
                }

                showTaskSheet?.let { task ->
                    TaskBottomSheet(
                        task = task,
                        isEditing = isEditing,
                        isDark = isDark,
                        onDismiss = { showTaskSheet = null },
                        onTaskAction = { name, priority, deadline, notes, type, days ->
                            if (isEditing) {
                                viewModel.updateTask(task.copy(title = name, priority = priority, deadlineTime = deadline, notes = notes, taskType = type, repeatDays = days))
                            } else {
                                viewModel.addTask(name, priority, deadline, notes, type, days)
                            }
                            showTaskSheet = null
                        }
                    )
                }
            }
}

@Composable
fun StatusDashboard(role: PlayerRole, lang: LanguageCode, progress: Float, insult: String, streak: Int, isDark: Boolean, toxicity: ToxicityLevel) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "Progress")
    val labelRes = if (role == PlayerRole.SLACKER) {
        if (toxicity == ToxicityLevel.LOW) R.string.role_slacker else R.string.role_lox
    } else {
        role.labelRes
    }
    val roleLabel = stringResource(labelRes)
    val roleColor = when(role) {
        PlayerRole.SLACKER -> Color.Red
        PlayerRole.WANNABE -> if (isDark) Color.Yellow else Color(0xFFC0A000)
        PlayerRole.GIGACHAD -> Color.Green
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = roleColor,
                        strokeWidth = 8.dp,
                        trackColor = Color.Gray.copy(alpha = 0.2f),
                    )
                    Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.player_status), style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        if (streak > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(ToxicStrings.getStreakText(streak, lang), fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(text = roleLabel, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = roleColor)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = roleColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, roleColor.copy(alpha = 0.3f))
            ) {
                Text(
                    text = insult,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) roleColor else roleColor.compositeOver(Color.Black), 
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBottomSheet(
    task: TaskEntity,
    isEditing: Boolean,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onTaskAction: (String, Priority, String?, String, TaskType, String) -> Unit
) {
    var name by remember { mutableStateOf(task.title) }
    var priority by remember { mutableStateOf(task.priority) }
    var deadline by remember { mutableStateOf(task.deadlineTime) }
    var notes by remember { mutableStateOf(task.notes) }
    var type by remember { mutableStateOf(task.taskType) }
    var repeatDays by remember { mutableStateOf(task.repeatDays) }
    
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 40.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
            Text(if (isEditing) stringResource(R.string.edit_mission) else stringResource(R.string.new_mission), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.mission_objective)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text(stringResource(R.string.notes_hint)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

            Spacer(modifier = Modifier.height(16.dp))
            val types = listOf(TaskType.ONE_TIME to R.string.type_once, TaskType.WEEKLY to R.string.type_weekly, TaskType.GOAL_WEEK to R.string.type_goal_week, TaskType.GOAL_MONTH to R.string.type_goal_month)
            LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(types) { (t, labelRes) ->
                    FilterChip(selected = type == t, onClick = { type = t }, label = { Text(stringResource(labelRes), fontSize = 10.sp) })
                }
            }
            
            if (type == TaskType.WEEKLY) {
                Spacer(modifier = Modifier.height(8.dp))
                repeatDays = WeekDayPicker(repeatDays)
            }

            Spacer(modifier = Modifier.height(16.dp))
            DeadlinePickerButton(deadline, isDark) { deadline = it }

            Spacer(modifier = Modifier.height(16.dp))
            PrioritySelector(priority) { priority = it }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { if (name.isNotBlank()) onTaskAction(name, priority, deadline, notes, type, repeatDays) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (isEditing) stringResource(R.string.update_mission) else stringResource(R.string.deploy_mission), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WeekDayPicker(current: String): String {
    val days = listOf("1", "2", "3", "4", "5", "6", "7")
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    var selected by remember { mutableStateOf(current.split(",").filter { it.isNotEmpty() }.toMutableList()) }
    
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        days.forEachIndexed { i, d ->
            val isSel = selected.contains(d)
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(if(isSel) Color.Red else Color.DarkGray).clickable { 
                val newList = selected.toMutableList()
                if(isSel) newList.remove(d) else newList.add(d)
                selected = newList
            }, contentAlignment = Alignment.Center) {
                Text(labels[i], color = Color.White, fontSize = 12.sp)
            }
        }
    }
    return selected.sorted().joinToString(",")
}

@Composable
fun DeadlinePickerButton(deadline: String?, isDark: Boolean, onDeadlineSet: (String?) -> Unit) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxWidth().clickable {
            val now = LocalTime.now()
            val activity = context.findActivity()
            if (activity != null) {
                val themeId = if (isDark) android.R.style.Theme_DeviceDefault_Dialog else android.R.style.Theme_DeviceDefault_Light_Dialog
                TimePickerDialog(activity, themeId, { _, h, m ->
                    onDeadlineSet(String.format(Locale.getDefault(), "%02d:%02d", h, m))
                }, now.hour, now.minute, true).show()
            }
        },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = if (deadline == null) stringResource(R.string.set_deadline) else stringResource(R.string.deadline_label, deadline), color = if (deadline == null) Color.Gray else MaterialTheme.colorScheme.primary)
            if (deadline != null) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { onDeadlineSet(null) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Clear, null) }
            }
        }
    }
}

@Composable
fun PrioritySelector(current: Priority, onSelect: (Priority) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val priorities = listOf(
            Priority.LOW to R.string.priority_low,
            Priority.MEDIUM to R.string.priority_mid,
            Priority.HARD to R.string.priority_hardcore
        )
        priorities.forEach { (p, labelRes) ->
            val sel = current == p
            FilterChip(selected = sel, onClick = { onSelect(p) }, label = { Text(stringResource(labelRes)) }, modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = p.color.copy(alpha = 0.2f), selectedLabelColor = p.color))
        }
    }
}

@Composable
fun TaskCard(task: TaskEntity, isDark: Boolean, onCheckedChange: (Boolean) -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
    val priorityColor = if (task.priority == Priority.MEDIUM && !isDark) Color(0xFFC0A000) else task.priority.color
    Surface(modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)).clickable { onEdit() }, color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isCompleted, onCheckedChange = onCheckedChange)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null)
                if (task.notes.isNotBlank()) Text(task.notes, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(task.priority.icon, null, tint = priorityColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    val labelRes = when(task.priority) {
                        Priority.LOW -> R.string.priority_low
                        Priority.MEDIUM -> R.string.priority_mid
                        Priority.HARD -> R.string.priority_hardcore
                    }
                    Text(stringResource(labelRes), color = priorityColor, style = MaterialTheme.typography.labelSmall)
                    if (task.deadlineTime != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.Timer, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(task.deadlineTime, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.DeleteOutline, null, tint = Color.Gray) }
        }
    }
}

@Composable
fun DateSelector(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        IconButton(onClick = { onDateSelected(selectedDate.minusDays(1)) }) { Icon(Icons.Default.ChevronLeft, null) }
        Text(text = if (selectedDate == LocalDate.now()) stringResource(R.string.today) else selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
        IconButton(onClick = { onDateSelected(selectedDate.plusDays(1)) }) { Icon(Icons.Default.ChevronRight, null) }
    }
}

@Composable
fun EmptyState() {
    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Rounded.PlaylistAddCheck, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f) )
        Text(stringResource(R.string.no_missions), color = Color.Gray)
    }
}

@Composable
fun HistoryScreen(historyData: Map<String, Pair<Triple<Float, Int, Int>, PlayerRole>>, lang: LanguageCode, viewModel: TaskViewModel, isDark: Boolean, onDayClick: (LocalDate) -> Unit) {
    var viewedMonth by remember { mutableStateOf(YearMonth.now()) }
    val daysInMonth = viewedMonth.lengthOfMonth()
    val firstDayOfMonth = viewedMonth.atDay(1).dayOfWeek.value % 7
    val stats by viewModel.totalStats.collectAsState()
    val monthLabel = viewedMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag(lang.code)))

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewedMonth = viewedMonth.minusMonths(1) }) { Icon(Icons.Default.ArrowBackIos, null, modifier = Modifier.size(16.dp)) }
                Text(text = monthLabel.uppercase(Locale.getDefault()), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                IconButton(onClick = { viewedMonth = viewedMonth.plusMonths(1) }) { Icon(Icons.Default.ArrowForwardIos, null, modifier = Modifier.size(16.dp)) }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            val days = if (lang == LanguageCode.UK) listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд") else listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    days.forEach { Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                val calendarRows = (daysInMonth + (if(firstDayOfMonth == 0) 6 else firstDayOfMonth - 1) + 6) / 7
                for (row in 0 until calendarRows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val dayIndex = row * 7 + col - (if(firstDayOfMonth == 0) 6 else firstDayOfMonth - 1)
                            if (dayIndex in 0 until daysInMonth) {
                                val date = viewedMonth.atDay(dayIndex + 1)
                                val dateStr = date.toString()
                                val dayData = historyData[dateStr]
                                val bgColor = when {
                                    dayData == null -> Color.DarkGray.copy(alpha = 0.1f)
                                    dayData.second == PlayerRole.GIGACHAD -> Color.Green.copy(alpha = 0.6f)
                                    dayData.second == PlayerRole.WANNABE -> (if (isDark) Color.Yellow else Color(0xFFC0A000)).copy(alpha = 0.6f)
                                    else -> Color.Red.copy(alpha = 0.6f)
                                }
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp).clip(RoundedCornerShape(8.dp)).background(bgColor).clickable { onDayClick(date) }.then(if(LocalDate.now()==date) Modifier.border(2.dp, Color.White, RoundedCornerShape(8.dp)) else Modifier), contentAlignment = Alignment.Center) {
                                    Text((dayIndex + 1).toString(), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.player_status).uppercase(), style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem(stringResource(R.string.stat_completed), stats.first.toString())
                        StatItem(stringResource(R.string.stat_total), stats.second.toString())
                        StatItem(stringResource(R.string.stat_giga_days), stats.third.toString())
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            listOf(Color.Green to stringResource(R.string.role_gigachad), (if(isDark) Color.Yellow else Color(0xFFC0A000)) to stringResource(R.string.role_wannabe), Color.Red to stringResource(R.string.role_slacker)).forEach { (color, label) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun SettingsDialog(currentTheme: ThemeMode, currentLang: LanguageCode, notifySettings: NotificationSettings, isDark: Boolean, onDismiss: () -> Unit, onThemeChange: (ThemeMode) -> Unit, onLangChange: (LanguageCode) -> Unit, onNotifySettingsChange: (NotificationSettings) -> Unit) {
    var showDisclaimer by remember { mutableStateOf(false) }
    val context = LocalContext.current

    key(currentLang) {
        if (showDisclaimer) {
            AlertDialog(
                onDismissRequest = { showDisclaimer = false },
                confirmButton = { TextButton(onClick = { showDisclaimer = false }) { Text(stringResource(android.R.string.ok)) } },
                title = { Text(stringResource(R.string.disclaimer_title), fontWeight = FontWeight.Bold) },
                text = { Text(stringResource(R.string.disclaimer_text)) }
            )
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.ok)) } },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { showDisclaimer = true }) {
                        Icon(Icons.Rounded.Info, contentDescription = "Disclaimer")
                    }
                }
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    item {
                        Column {
                            Text(stringResource(R.string.theme), style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val themes = listOf(ThemeMode.LIGHT to R.string.theme_light, ThemeMode.DARK to R.string.theme_dark, ThemeMode.SYSTEM to R.string.theme_system)
                                themes.forEach { (mode, labelRes) ->
                                    FilterChip(selected = currentTheme == mode, onClick = { onThemeChange(mode) }, label = { Text(stringResource(labelRes)) })
                                }
                            }
                        }
                    }
                    item {
                        Column {
                            Text(stringResource(R.string.language), style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val langs = listOf(LanguageCode.EN to R.string.lang_en, LanguageCode.UK to R.string.lang_uk)
                                langs.forEach { (lang, labelRes) ->
                                    FilterChip(selected = currentLang == lang, onClick = { onLangChange(lang) }, label = { Text(stringResource(labelRes)) })
                                }
                            }
                        }
                    }
                    item {
                        Column {
                            Text(stringResource(R.string.toxicity_level), style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val levels = listOf(ToxicityLevel.LOW to R.string.toxicity_low, ToxicityLevel.NORMAL to R.string.toxicity_normal, ToxicityLevel.EXTREME to R.string.toxicity_extreme)
                                levels.forEach { (level, labelRes) ->
                                    FilterChip(selected = notifySettings.toxicityLevel == level, onClick = { onNotifySettingsChange(notifySettings.copy(toxicityLevel = level)) }, label = { Text(stringResource(labelRes)) })
                                }
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(stringResource(R.string.notifications), style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                            Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(stringResource(R.string.enabled), modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                        Switch(checked = notifySettings.enabled, onCheckedChange = { onNotifySettingsChange(notifySettings.copy(enabled = it)) })
                                    }
                                    if (notifySettings.enabled) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(stringResource(R.string.nag_until_100), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                            Checkbox(checked = notifySettings.nagUntilFinish, onCheckedChange = { onNotifySettingsChange(notifySettings.copy(nagUntilFinish = it)) })
                                        }
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                        Text(stringResource(R.string.active_hours), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                                            ActiveTimeSelector(notifySettings.startHour, notifySettings.startMinute, stringResource(R.string.start), isDark, context) { h, m ->
                                                val currentEndTotal = notifySettings.endHour * 60 + notifySettings.endMinute
                                                val newStartTotal = h * 60 + m
                                                if (newStartTotal < currentEndTotal) {
                                                    onNotifySettingsChange(notifySettings.copy(startHour = h, startMinute = m))
                                                }
                                            }
                                            Text("—", fontWeight = FontWeight.Bold)
                                            ActiveTimeSelector(notifySettings.endHour, notifySettings.endMinute, stringResource(R.string.end), isDark, context) { h, m ->
                                                val currentStartTotal = notifySettings.startHour * 60 + notifySettings.startMinute
                                                val newEndTotal = h * 60 + m
                                                if (newEndTotal > currentStartTotal) {
                                                    onNotifySettingsChange(notifySettings.copy(endHour = h, endMinute = m))
                                                }
                                            }
                                        }
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                        var intervalText by remember { mutableStateOf(notifySettings.intervalMinutes.toString()) }
                                        OutlinedTextField(value = intervalText, onValueChange = { intervalText = it; it.toIntOrNull()?.let { m -> onNotifySettingsChange(notifySettings.copy(intervalMinutes = m.coerceAtLeast(15))) } },
                                            label = { Text(stringResource(R.string.interval)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ActiveTimeSelector(hour: Int, minute: Int, label: String, isDark: Boolean, context: Context, onTimeSet: (Int, Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color.Gray)
        Surface(
            modifier = Modifier.clickable {
                context.findActivity()?.let { activity ->
                    val themeId = if (isDark) android.R.style.Theme_DeviceDefault_Dialog else android.R.style.Theme_DeviceDefault_Light_Dialog
                    TimePickerDialog(activity, themeId, { _, h, m -> onTimeSet(h, m) }, hour, minute, true).show()
                }
            },
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
