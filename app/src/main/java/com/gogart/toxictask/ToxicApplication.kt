package com.gogart.toxictask

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ToxicApplication : Application() {
    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        
        // Basic Global Exception Handler (Minimum Crash Reporting)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("ToxicTaskCrash", "CRASH DETECTED in thread ${thread.name}", throwable)
            // Here you could save the stacktrace to a file for later "export log" feature
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // We only use AlarmManager now for "clock-like" precision
        applicationScope.launch {
            com.gogart.toxictask.worker.ToxicAlarmReceiver.scheduleNextAlarm(this@ToxicApplication)
        }
    }
}
