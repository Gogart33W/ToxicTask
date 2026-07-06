package com.gogart.toxictask.worker

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
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
import java.time.LocalTime

class ToxicAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleNextAlarm(context)
            return
        }

        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            processNotifications(context)
            scheduleNextAlarm(context)
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
        val tasks = db.taskDao().getTasksByDate(LocalDate.now().toString()).first()
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
                title = when (lang) {
                    LanguageCode.UK -> "ТИТУЛ ЛОХА ПІДТВЕРДЖЕНО!"
                    LanguageCode.DE -> "VERSAGER-TITEL BESTÄTIGT!"
                    else -> "LOSER TITLE CONFIRMED!"
                }
                message = when (lang) {
                    LanguageCode.UK -> "Ти вже 3 дні нічого не робиш! Твій список тасків такий же порожній, як і твоє майбутнє!"
                    LanguageCode.DE -> "Du hast seit 3 Tagen nichts getan! Deine Aufgabenliste ist so leer wie deine Zukunft!"
                    else -> "You haven't done anything for 3 days! Your task list is as empty as your future!"
                }
            }
            expiredTask != null -> {
                title = when (lang) {
                    LanguageCode.UK -> "ДЕДЛАЙН МИНУВ!"
                    LanguageCode.DE -> "DEADLINE ABGELAUFEN!"
                    else -> "DEADLINE EXPIRED!"
                }
                message = when (lang) {
                    LanguageCode.UK -> "Місія '${expiredTask.title}' провалена! Дедлайн був о ${expiredTask.deadlineTime}."
                    LanguageCode.DE -> "Mission '${expiredTask.title}' fehlgeschlagen! Deadline war um ${expiredTask.deadlineTime}."
                    else -> "Mission '${expiredTask.title}' failed! Deadline was at ${expiredTask.deadlineTime}."
                }
            }
            urgentTask != null -> {
                val deadline = LocalTime.parse(urgentTask.deadlineTime)
                val diffMins = java.time.Duration.between(currentTime, deadline).toMinutes()
                
                title = when (lang) {
                    LanguageCode.UK -> if (diffMins in 0..20) "ОСТАННІЙ ШАНС!" else "ЧАС ПІДЖИМАЄ!"
                    LanguageCode.DE -> if (diffMins in 0..20) "LETZTE CHANCE!" else "DIE ZEIT LÄUFT AB!"
                    else -> if (diffMins in 0..20) "LAST CHANCE!" else "TIME IS RUNNING OUT!"
                }
                
                message = when (lang) {
                    LanguageCode.UK -> {
                        if (diffMins in 0..20) "Останній шанс виконати '${urgentTask.title}'!"
                        else "Ти ще не виконав '${urgentTask.title}'! Залишилось всього $diffMins хв."
                    }
                    LanguageCode.DE -> {
                        if (diffMins in 0..20) "Letzte Chance, '${urgentTask.title}' zu erledigen!"
                        else "Du hast '${urgentTask.title}' noch nicht erledigt! Nur noch $diffMins Min. übrig."
                    }
                    else -> {
                        if (diffMins in 0..20) "Last chance to complete '${urgentTask.title}'!"
                        else "You haven't finished '${urgentTask.title}'! Only $diffMins mins left."
                    }
                }
            }
            isEndOfDayPressure && uncompletedTasks.isNotEmpty() && !shouldStopNagging -> {
                title = when (lang) {
                    LanguageCode.UK -> "ДЕНЬ ЗАКІНЧУЄТЬСЯ!"
                    LanguageCode.DE -> "TAG ENDET!"
                    else -> "DAY IS ENDING!"
                }
                
                message = if (settings.nagUntilFinish) {
                    when (lang) {
                        LanguageCode.UK -> "День закінчується, а ти ще не добив план! Живо за роботу!"
                        LanguageCode.DE -> "Der Tag endet und du hast den Plan nicht erfüllt! Los geht's!"
                        else -> "The day is ending and you haven't finished the plan! Move it!"
                    }
                } else {
                    val slacker = when (lang) {
                        LanguageCode.UK -> when (toxicity) {
                            com.gogart.toxictask.settings.ToxicityLevel.LOW -> "ледарем"
                            com.gogart.toxictask.settings.ToxicityLevel.NORMAL -> "лохом"
                            com.gogart.toxictask.settings.ToxicityLevel.EXTREME -> "конченим"
                        }
                        LanguageCode.DE -> when (toxicity) {
                            com.gogart.toxictask.settings.ToxicityLevel.LOW -> "Faulpelz"
                            com.gogart.toxictask.settings.ToxicityLevel.NORMAL -> "Versager"
                            com.gogart.toxictask.settings.ToxicityLevel.EXTREME -> "Abfall"
                        }
                        else -> if (toxicity == com.gogart.toxictask.settings.ToxicityLevel.LOW) "a slacker" else "a loser"
                    }
                    when (lang) {
                        LanguageCode.UK -> "День закінчується, а ти ще не досягнув статусу ГІГАЧАД! Не будь $slacker!"
                        LanguageCode.DE -> "Der Tag endet und du hast den GIGACHAD-Status nicht erreicht! Sei kein $slacker!"
                        else -> "The day is ending and you haven't reached GIGACHAD status! Don't be $slacker!"
                    }
                }
            }
            else -> {
                title = if (lang == LanguageCode.UK) "ЕЙ, ТИ!" else "HEY YOU!"
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

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        fun scheduleNextAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ToxicAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val triggerAt = System.currentTimeMillis() + 60000 

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val info = AlarmManager.AlarmClockInfo(triggerAt, pendingIntent)
                alarmManager.setAlarmClock(info, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        }
    }
}
