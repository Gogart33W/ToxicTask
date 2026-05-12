package com.example.toxictask.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.toxictask.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class LanguageCode(val code: String) {
    EN("en"),
    UK("uk")
}

enum class ToxicityLevel {
    LOW,
    NORMAL,
    EXTREME
}

data class NotificationSettings(
    val enabled: Boolean,
    val intervalMinutes: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val nagUntilFinish: Boolean,
    val toxicityLevel: ToxicityLevel
)

class SettingsManager(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme_mode")
    private val langKey = stringPreferencesKey("language_code")
    private val notifyEnabledKey = booleanPreferencesKey("notify_enabled")
    private val notifyIntervalKey = intPreferencesKey("notify_interval")
    private val notifyStartHourKey = intPreferencesKey("notify_start_hour")
    private val notifyStartMinuteKey = intPreferencesKey("notify_start_minute")
    private val notifyEndHourKey = intPreferencesKey("notify_end_hour")
    private val notifyEndMinuteKey = intPreferencesKey("notify_end_minute")
    private val nagUntilFinishKey = booleanPreferencesKey("nag_until_finish")
    private val toxicityLevelKey = stringPreferencesKey("toxicity_level")
    private val lastNotifyTimeKey = longPreferencesKey("last_notify_time")

    val lastNotifyTime: Flow<Long> = context.dataStore.data.map { it[lastNotifyTimeKey] ?: 0L }

    suspend fun setLastNotifyTime(time: Long) {
        context.dataStore.edit { it[lastNotifyTimeKey] = time }
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { pref ->
        ThemeMode.valueOf(pref[themeKey] ?: ThemeMode.SYSTEM.name)
    }

    val languageCode: Flow<LanguageCode> = context.dataStore.data.map { pref ->
        LanguageCode.valueOf(pref[langKey] ?: LanguageCode.EN.name)
    }

    val notificationSettings: Flow<NotificationSettings> = context.dataStore.data.map { pref ->
        NotificationSettings(
            enabled = pref[notifyEnabledKey] ?: true,
            intervalMinutes = pref[notifyIntervalKey] ?: 60,
            startHour = pref[notifyStartHourKey] ?: 9,
            startMinute = pref[notifyStartMinuteKey] ?: 0,
            endHour = pref[notifyEndHourKey] ?: 21,
            endMinute = pref[notifyEndMinuteKey] ?: 0,
            nagUntilFinish = pref[nagUntilFinishKey] ?: true,
            toxicityLevel = ToxicityLevel.valueOf(pref[toxicityLevelKey] ?: ToxicityLevel.LOW.name)
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { pref -> pref[themeKey] = mode.name }
    }

    suspend fun setLanguage(lang: LanguageCode) {
        context.dataStore.edit { pref -> pref[langKey] = lang.name }
    }

    suspend fun updateNotificationSettings(settings: NotificationSettings) {
        context.dataStore.edit { pref ->
            pref[notifyEnabledKey] = settings.enabled
            pref[notifyIntervalKey] = settings.intervalMinutes
            pref[notifyStartHourKey] = settings.startHour
            pref[notifyStartMinuteKey] = settings.startMinute
            pref[notifyEndHourKey] = settings.endHour
            pref[notifyEndMinuteKey] = settings.endMinute
            pref[nagUntilFinishKey] = settings.nagUntilFinish
            pref[toxicityLevelKey] = settings.toxicityLevel.name
        }
    }
}
