package com.gogart.toxictask.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnalyticsManager(context: Context) {
    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    suspend fun logTaskRollover(taskCount: Int) {
        withContext(Dispatchers.IO) {
            val bundle = Bundle().apply {
                putInt("task_count", taskCount)
            }
            firebaseAnalytics.logEvent("task_rollover", bundle)
        }
    }

    suspend fun logTaskCreated(type: String, priority: Int) {
        withContext(Dispatchers.IO) {
            val bundle = Bundle().apply {
                putString("task_type", type)
                putInt("task_priority", priority)
            }
            firebaseAnalytics.logEvent("task_created", bundle)
        }
    }
}
