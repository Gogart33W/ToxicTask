package com.gogart.toxictask.worker

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.gogart.toxictask.MainActivity
import com.gogart.toxictask.ToxicStrings
import com.gogart.toxictask.data.AppDatabase
import com.gogart.toxictask.settings.LanguageCode
import com.gogart.toxictask.settings.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ToxicAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scope = CoroutineScope(Dispatchers.Default)
        
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scope.launch { scheduleNextAlarm(context) }
            return
        }

        scope.launch {
            try {
                processNotifications(context)
            } catch (e: Exception) {
                Log.e("ToxicAlarm", "Error processing notifications", e)
            } finally {
                scheduleNextAlarm(context)
            }
        }
    }

    private suspend fun processNotifications(context: Context) {
        val settingsManager = SettingsManager(context)
        val settings = settingsManager.notificationSettings.first()
        val lang = settingsManager.languageCode.first()
        val toxicity = settings.toxicityLevel
        val lastNotify = settingsManager.lastNotifyTime.first()
        val lastAdded = settingsManager.lastTaskAddedTime.first()

        if (!settings.enabled) return

        val currentTime = LocalTime.now()
        val currentDay = LocalDate.now().dayOfWeek.value
        val daySchedule = settings.detailedSchedule[currentDay] ?: com.gogart.toxictask.settings.DaySchedule()

        val startTime = if (settings.useDetailedSchedule) LocalTime.of(daySchedule.startHour, daySchedule.startMinute)
                        else LocalTime.of(settings.startHour, settings.startMinute)
        val endTime = if (settings.useDetailedSchedule) LocalTime.of(daySchedule.endHour, daySchedule.endMinute)
                      else LocalTime.of(settings.endHour, settings.endMinute)

        val isWithinRange = if (startTime.isBefore(endTime)) {
            currentTime.isAfter(startTime) && currentTime.isBefore(endTime)
        } else {
            currentTime.isAfter(startTime) || currentTime.isBefore(endTime)
        }

        if (!isWithinRange) return

        val db = AppDatabase.getDatabase(context)
        val dao = db.taskDao()
        
        // Автоматично додаємо повторювані завдання на сьогодні
        com.gogart.toxictask.utils.TaskUtils.checkAndAddRepeatingTasks(dao)
        
        val tasks = dao.getTasksByDate(LocalDate.now().toString()).first()
        val uncompletedTasks = tasks.filter { !it.isCompleted }

        val totalWeight = tasks.sumOf { it.priority.weight }
        val completedWeight = tasks.filter { it.isCompleted }.sumOf { it.priority.weight }
        val progress = if (totalWeight == 0) 0f else completedWeight.toFloat() / totalWeight
        val isGigachad = tasks.size >= 3 && progress >= 0.75f

        val threeDaysMillis = 3 * 24 * 60 * 60 * 1000L
        val inactiveForThreeDays = System.currentTimeMillis() - lastAdded > threeDaysMillis

        val shouldStopNagging = if (settings.nagUntilFinish) progress >= 1.0f else isGigachad
        if (tasks.isNotEmpty() && shouldStopNagging && !inactiveForThreeDays) return

        val urgentTask = uncompletedTasks.find {
            it.deadlineTime != null && LocalTime.parse(it.deadlineTime).isBefore(currentTime.plusMinutes(60)) && LocalTime.parse(it.deadlineTime).isAfter(currentTime)
        }
        val expiredTask = uncompletedTasks.find {
            it.deadlineTime != null && LocalTime.parse(it.deadlineTime).isBefore(currentTime)
        }
        val isEndOfDayPressure = currentTime.isAfter(endTime.minusMinutes(60)) && currentTime.isBefore(endTime)

        val isAggressive = urgentTask != null || expiredTask != null || (isEndOfDayPressure && !shouldStopNagging) || inactiveForThreeDays
        
        val intervalMillis = if (isAggressive) 15 * 60 * 1000L else settings.intervalMinutes * 60 * 1000L
        if (System.currentTimeMillis() - lastNotify < intervalMillis - 5000) return

        val title: String
        val message: String

        when {
            inactiveForThreeDays -> {
                val strs = ToxicStrings.getNotificationStrings(lang, "INACTIVE")
                title = strs.first
                message = strs.second
            }
            expiredTask != null -> {
                val strs = ToxicStrings.getNotificationStrings(lang, "EXPIRED", expiredTask.title, expiredTask.deadlineTime ?: "")
                title = strs.first
                message = strs.second
            }
            urgentTask != null -> {
                val deadline = LocalTime.parse(urgentTask.deadlineTime)
                val diffMins = java.time.Duration.between(currentTime, deadline).toMinutes()
                val type = if (diffMins in 0..20) "LAST_CHANCE" else "URGENT"
                val strs = ToxicStrings.getNotificationStrings(lang, type, urgentTask.title, timeLeft = diffMins)
                title = strs.first
                message = strs.second
            }
            isEndOfDayPressure && uncompletedTasks.isNotEmpty() && !shouldStopNagging -> {
                val strs = ToxicStrings.getNotificationStrings(lang, "END_OF_DAY")
                title = strs.first
                message = strs.second
            }
            else -> {
                val strs = ToxicStrings.getNotificationStrings(lang, "DEFAULT")
                title = strs.first
                val status = when {
                    tasks.size < 3 -> "LOX"
                    progress < 0.35f -> "LOX"
                    progress < 0.75f -> "WANNABE"
                    else -> "GIGACHAD"
                }
                message = if (tasks.isEmpty()) ToxicStrings.getEmptyInsults(lang, toxicity).random()
                          else if (tasks.size < 3) ToxicStrings.getTooFewTasksInsult(tasks.size, lang, toxicity)
                          else ToxicStrings.getInsults(lang, toxicity, status).random()
            }
        }

        showNotification(context, title, message)
        settingsManager.setLastNotifyTime(System.currentTimeMillis())
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "toxic_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Toxic Reminders", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(42, notification)
    }

    companion object {
        suspend fun scheduleNextAlarm(context: Context) {
            val settingsManager = SettingsManager(context)
            val settings = settingsManager.notificationSettings.first()
            if (!settings.enabled) return

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ToxicAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val now = LocalDateTime.now()
            val currentDay = now.toLocalDate().dayOfWeek.value
            val daySchedule = settings.detailedSchedule[currentDay] ?: com.gogart.toxictask.settings.DaySchedule()

            val startTime = if (settings.useDetailedSchedule) LocalTime.of(daySchedule.startHour, daySchedule.startMinute)
                            else LocalTime.of(settings.startHour, settings.startMinute)
            val endTime = if (settings.useDetailedSchedule) LocalTime.of(daySchedule.endHour, daySchedule.endMinute)
                          else LocalTime.of(settings.endHour, settings.endMinute)

            var triggerAt: Long
            val currentTime = now.toLocalTime()

            val isWithinRange = if (startTime.isBefore(endTime)) {
                currentTime.isAfter(startTime) && currentTime.isBefore(endTime)
            } else {
                currentTime.isAfter(startTime) || currentTime.isBefore(endTime)
            }

            if (isWithinRange) {
                // В робочий час - кожні 15 хвилин
                triggerAt = System.currentTimeMillis() + (15 * 60 * 1000L)
            } else {
                // Вночі - плануємо на початок наступного робочого дня
                var nextStart = now.toLocalDate().atTime(startTime)
                if (currentTime.isAfter(endTime) || currentTime == endTime) {
                    nextStart = nextStart.plusDays(1)
                    // Оновити startTime для наступного дня, якщо використовується детальний графік
                    if (settings.useDetailedSchedule) {
                        val nextDay = nextStart.dayOfWeek.value
                        val nextSchedule = settings.detailedSchedule[nextDay] ?: com.gogart.toxictask.settings.DaySchedule()
                        nextStart = nextStart.toLocalDate().atTime(nextSchedule.startHour, nextSchedule.startMinute)
                    }
                }
                triggerAt = nextStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                
                // Якщо раптом час вже минув (наприклад, через зміну годинника), додаємо 15 хв
                if (triggerAt <= System.currentTimeMillis()) {
                    triggerAt = System.currentTimeMillis() + (15 * 60 * 1000L)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        }
    }
}
