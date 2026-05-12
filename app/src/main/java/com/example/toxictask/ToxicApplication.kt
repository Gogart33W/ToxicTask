package com.example.toxictask

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ToxicApplication : Application() {
    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // We only use AlarmManager now for "clock-like" precision
        com.example.toxictask.worker.ToxicAlarmReceiver.scheduleNextAlarm(this)
    }
}
