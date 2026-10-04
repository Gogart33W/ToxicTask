package com.gogart.toxictask

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.gogart.toxictask.settings.LanguageCode
import com.gogart.toxictask.settings.ToxicityLevel
import java.util.Locale

object ToxicStrings {
    private var lastEmptyInsult = ""
    private var lastTaskInsult = ""
    private var lastTooFewTasksRaw = ""

    fun getLocalizedResources(context: Context, lang: LanguageCode): Resources {
        val locale = Locale.forLanguageTag(lang.code)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config).resources
    }

    fun getTooFewTasksInsult(context: Context, count: Int, minTasks: Int, lang: LanguageCode, level: ToxicityLevel): String {
        val res = getLocalizedResources(context, lang)
        val arrayId = when (level) {
            ToxicityLevel.LOW -> R.array.too_few_tasks_mild
            ToxicityLevel.NORMAL -> R.array.too_few_tasks_normal
            ToxicityLevel.EXTREME -> R.array.too_few_tasks_extreme
        }
        val raw = getRandomAvoidRepeat(res.getStringArray(arrayId), lastTooFewTasksRaw)
        lastTooFewTasksRaw = raw
        // %1$d = current count, %2$d = configured minimum
        return String.format(Locale.forLanguageTag(lang.code), raw, count, minTasks)
    }

    fun getInsults(context: Context, lang: LanguageCode, level: ToxicityLevel, status: String): String {
        val res = getLocalizedResources(context, lang)
        val st = if (status == "SLACKER") "LOX" else status
        
        val arrayId = when (st) {
            "LOX" -> when (level) {
                ToxicityLevel.LOW -> R.array.slacker_insults_mild
                ToxicityLevel.NORMAL -> R.array.slacker_insults_normal
                ToxicityLevel.EXTREME -> R.array.slacker_insults_extreme
            }
            "WANNABE" -> when (level) {
                ToxicityLevel.LOW -> R.array.wannabe_insults_mild
                ToxicityLevel.NORMAL -> R.array.wannabe_insults_normal
                ToxicityLevel.EXTREME -> R.array.wannabe_insults_extreme
            }
            else -> when (level) { // GIGACHAD
                ToxicityLevel.LOW -> R.array.gigachad_insults_mild
                ToxicityLevel.NORMAL -> R.array.gigachad_insults_normal
                ToxicityLevel.EXTREME -> R.array.gigachad_insults_extreme
            }
        }
        
        val array = res.getStringArray(arrayId)
        val randomString = getRandomAvoidRepeat(array, lastTaskInsult)
        lastTaskInsult = randomString
        return randomString
    }

    fun getEmptyInsults(context: Context, lang: LanguageCode, level: ToxicityLevel): String {
        val res = getLocalizedResources(context, lang)
        val arrayId = when (level) {
            ToxicityLevel.LOW -> R.array.empty_insults_mild
            ToxicityLevel.NORMAL -> R.array.empty_insults_normal
            ToxicityLevel.EXTREME -> R.array.empty_insults_extreme
        }
        val array = res.getStringArray(arrayId)
        val randomString = getRandomAvoidRepeat(array, lastEmptyInsult)
        lastEmptyInsult = randomString
        return randomString
    }

    fun getTimeFixMessage(context: Context, lang: LanguageCode): String {
        return getLocalizedResources(context, lang).getString(R.string.time_fix_msg)
    }

    fun getTimeErrorMessage(context: Context, startTime: String, endTime: String, lang: LanguageCode, level: ToxicityLevel): String {
        // Fallback for simplicity. A proper solution would be putting these in strings.xml per level too, 
        // but to not break existing logic heavily, we keep the hardcoded for now, or just use the generic ones.
        return when (lang) {
            LanguageCode.UK -> {
                when (level) {
                    ToxicityLevel.LOW -> "Час закінчення не може бути раніше початку."
                    ToxicityLevel.NORMAL -> "Навіть мій код розуміє математику краще за тебе, ледарю!"
                    ToxicityLevel.EXTREME -> "Час $endTime менший за $startTime. Ти реально ЛОХ чи прикидаєшся?"
                }
            }
            LanguageCode.DE -> {
                when (level) {
                    ToxicityLevel.LOW -> "Die Endzeit kann nicht vor der Startzeit liegen."
                    ToxicityLevel.NORMAL -> "Sogar mein Code versteht Mathe besser als du, Faulpelz!"
                    ToxicityLevel.EXTREME -> "Zeit $endTime ist kleiner als $startTime. Bist du wirklich ein VERSAGER oder tust du nur so?"
                }
            }
            else -> {
                when (level) {
                    ToxicityLevel.LOW -> "End time cannot be earlier than start time."
                    ToxicityLevel.NORMAL -> "My code understands math better than you, slacker!"
                    ToxicityLevel.EXTREME -> "Time $endTime is less than $startTime. Are you really a LOSER or just pretending?"
                }
            }
        }
    }

    fun getStreakText(context: Context, days: Int, lang: LanguageCode): String {
        val res = getLocalizedResources(context, lang)
        return res.getQuantityString(R.plurals.streak_days, days, days)
    }

    fun getNotificationStrings(context: Context, lang: LanguageCode, toxicity: ToxicityLevel, type: String, taskTitle: String = "", deadline: String = "", timeLeft: Long = 0): Pair<String, String> {
        val res = getLocalizedResources(context, lang)
        
        val titleArrayId: Int
        val bodyArrayId: Int

        when (type) {
            "INACTIVE" -> {
                titleArrayId = when (toxicity) {
                    ToxicityLevel.LOW -> R.array.notif_inactive_title_mild
                    ToxicityLevel.NORMAL -> R.array.notif_inactive_title_normal
                    ToxicityLevel.EXTREME -> R.array.notif_inactive_title_extreme
                }
                bodyArrayId = when (toxicity) {
                    ToxicityLevel.LOW -> R.array.notif_inactive_body_mild
                    ToxicityLevel.NORMAL -> R.array.notif_inactive_body_normal
                    ToxicityLevel.EXTREME -> R.array.notif_inactive_body_extreme
                }
                return Pair(res.getStringArray(titleArrayId).random(), res.getStringArray(bodyArrayId).random())
            }
            "EXPIRED" -> {
                titleArrayId = when (toxicity) {
                    ToxicityLevel.LOW -> R.array.notif_expired_title_mild
                    ToxicityLevel.NORMAL -> R.array.notif_expired_title_normal
                    ToxicityLevel.EXTREME -> R.array.notif_expired_title_extreme
                }
                bodyArrayId = when (toxicity) {
                    ToxicityLevel.LOW -> R.array.notif_expired_body_mild
                    ToxicityLevel.NORMAL -> R.array.notif_expired_body_normal
                    ToxicityLevel.EXTREME -> R.array.notif_expired_body_extreme
                }
                val rawBody = res.getStringArray(bodyArrayId).random()
                return Pair(res.getStringArray(titleArrayId).random(), String.format(locale = Locale.forLanguageTag(lang.code), rawBody, taskTitle, deadline))
            }
            "LAST_CHANCE", "URGENT" -> {
                titleArrayId = when (toxicity) {
                    ToxicityLevel.LOW -> R.array.notif_urgent_title_mild
                    ToxicityLevel.NORMAL -> R.array.notif_urgent_title_normal
                    ToxicityLevel.EXTREME -> R.array.notif_urgent_title_extreme
                }
                bodyArrayId = when (toxicity) {
                    ToxicityLevel.LOW -> R.array.notif_urgent_body_mild
                    ToxicityLevel.NORMAL -> R.array.notif_urgent_body_normal
                    ToxicityLevel.EXTREME -> R.array.notif_urgent_body_extreme
                }
                val rawBody = res.getStringArray(bodyArrayId).random()
                return Pair(res.getStringArray(titleArrayId).random(), String.format(locale = Locale.forLanguageTag(lang.code), rawBody, taskTitle, timeLeft))
            }
            "END_OF_DAY" -> {
                titleArrayId = when (toxicity) {
                    ToxicityLevel.LOW -> R.array.notif_end_of_day_title_mild
                    ToxicityLevel.NORMAL -> R.array.notif_end_of_day_title_normal
                    ToxicityLevel.EXTREME -> R.array.notif_end_of_day_title_extreme
                }
                bodyArrayId = when (toxicity) {
                    ToxicityLevel.LOW -> R.array.notif_end_of_day_body_mild
                    ToxicityLevel.NORMAL -> R.array.notif_end_of_day_body_normal
                    ToxicityLevel.EXTREME -> R.array.notif_end_of_day_body_extreme
                }
                return Pair(res.getStringArray(titleArrayId).random(), res.getStringArray(bodyArrayId).random())
            }
            else -> { // DEFAULT
                titleArrayId = when (toxicity) {
                    ToxicityLevel.LOW -> R.array.notif_default_title_mild
                    ToxicityLevel.NORMAL -> R.array.notif_default_title_normal
                    ToxicityLevel.EXTREME -> R.array.notif_default_title_extreme
                }
                return Pair(res.getStringArray(titleArrayId).random(), res.getString(R.string.do_something))
            }
        }
    }

    private fun getRandomAvoidRepeat(array: Array<String>, lastValue: String): String {
        if (array.size <= 1) return array.firstOrNull() ?: ""
        var choice = array.random()
        var attempts = 0
        while (choice == lastValue && attempts < 5) {
            choice = array.random()
            attempts++
        }
        return choice
    }
}
