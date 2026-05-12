package com.example.toxictask.worker

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.toxictask.MainActivity
import com.example.toxictask.ToxicStrings
import com.example.toxictask.data.AppDatabase
import com.example.toxictask.settings.LanguageCode
import com.example.toxictask.settings.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

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

        if (!settings.enabled) return

        val currentTime = LocalTime.now()
        val startTime = LocalTime.of(settings.startHour, settings.startMinute)
        val endTime = LocalTime.of(settings.endHour, settings.endMinute)

        val isWithinRange = if (startTime.isBefore(endTime)) {
            currentTime.isAfter(startTime) && currentTime.isBefore(endTime)
        } else {
            currentTime.isAfter(startTime) || currentTime.isBefore(endTime)
        }

        if (!isWithinRange) return

        val db = AppDatabase.getDatabase(context)
        val tasks = db.taskDao().getTasksByDate(LocalDate.now().toString()).first()
        val uncompletedTasks = tasks.filter { !it.isCompleted }
        
        if (tasks.isNotEmpty() && uncompletedTasks.isEmpty()) return

        val progress = if (tasks.isEmpty()) 0f else tasks.count { it.isCompleted }.toFloat() / tasks.size
        val isGigachad = tasks.size >= 3 && progress >= 0.75f
        
        val urgentTask = uncompletedTasks.find { 
            it.deadlineTime != null && LocalTime.parse(it.deadlineTime).isBefore(currentTime.plusMinutes(60))
        }
        val isEndOfDayPressure = currentTime.isAfter(endTime.minusMinutes(60)) && currentTime.isBefore(endTime)
        
        val isAggressive = urgentTask != null || (isEndOfDayPressure && (settings.nagUntilFinish || !isGigachad))
        
        val intervalMillis = if (isAggressive) 15 * 60 * 1000L else settings.intervalMinutes * 60 * 1000L
        if (System.currentTimeMillis() - lastNotify < intervalMillis - 5000) return

        // Notification building logic (same as ToxicWorker)
        val title: String
        val message: String

        when {
            urgentTask != null -> {
                val deadline = LocalTime.parse(urgentTask.deadlineTime)
                val isExpired = deadline.isBefore(currentTime)
                title = if (lang == LanguageCode.UK) (if (isExpired) "ДЕДЛАЙН МИНУВ!" else "ЧАС ПІДЖИМАЄ!") else (if (isExpired) "DEADLINE EXPIRED!" else "TIME IS RUNNING OUT!")
                message = if (lang == LanguageCode.UK) {
                    if (isExpired) "Місія '${urgentTask.title}' провалена! Дедлайн був о ${urgentTask.deadlineTime}."
                    else "Ти ще не виконав '${urgentTask.title}'! Залишилось всього ${java.time.Duration.between(currentTime, deadline).toMinutes()} хв."
                } else {
                    if (isExpired) "Mission '${urgentTask.title}' failed! Deadline was at ${urgentTask.deadlineTime}."
                    else "You haven't finished '${urgentTask.title}'! Only ${java.time.Duration.between(currentTime, deadline).toMinutes()} mins left."
                }
            }
            isEndOfDayPressure && (settings.nagUntilFinish && uncompletedTasks.isNotEmpty()) -> {
                title = if (lang == LanguageCode.UK) "ДЕНЬ ЗАКІНЧУЄТЬСЯ!" else "DAY IS ENDING!"
                message = if (lang == LanguageCode.UK) "День закінчується, а ти ще не виконав всі таски! Живо за роботу!" else "The day is ending and you haven't finished all tasks! Move it!"
            }
            isEndOfDayPressure && !isGigachad -> {
                title = if (lang == LanguageCode.UK) "ДЕНЬ ЗАКІНЧУЄТЬСЯ!" else "DAY IS ENDING!"
                val slacker = if (lang == LanguageCode.UK) (if (toxicity == com.example.toxictask.settings.ToxicityLevel.LOW) "ледарем" else "лохом") else (if (toxicity == com.example.toxictask.settings.ToxicityLevel.LOW) "a slacker" else "a loser")
                message = if (lang == LanguageCode.UK) "День закінчується, а ти ще не досягнув статусу ГІГАЧАД! Не будь $slacker!" else "The day is ending and you haven't reached GIGACHAD status! Don't be $slacker!"
            }
            else -> {
                title = if (lang == LanguageCode.UK) "ЕЙ, ТИ!" else "HEY YOU!"
                message = if (tasks.isEmpty()) ToxicStrings.getEmptyInsults(lang, toxicity).random()
                          else if (tasks.size < 3) ToxicStrings.getTooFewTasksInsult(tasks.size, lang, toxicity)
                          else ToxicStrings.getInsults(lang, toxicity, if (progress < 0.35f) "LOX" else if (progress < 0.75f) "WANNABE" else "GIGACHAD").random()
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
                // The most precise way: setAlarmClock shows an icon in status bar and 
                // wakes up the device exactly on time.
                val info = AlarmManager.AlarmClockInfo(triggerAt, pendingIntent)
                alarmManager.setAlarmClock(info, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        }
    }
}
