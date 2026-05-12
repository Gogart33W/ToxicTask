package com.example.toxictask

import com.example.toxictask.settings.LanguageCode
import com.example.toxictask.settings.ToxicityLevel

object ToxicStrings {
    // --- SLACKER (Red Status) ---
    private val ukRedMild = listOf(
        "Ти ще довго збираєшся байдикувати?",
        "План сам себе не виконає, друже.",
        "День іде, а таски стоять. Може почнеш?",
        "Ти ж хотів бути продуктивним сьогодні, пам'ятаєш?",
        "Маленький крок краще, ніж нічого. Зроби його!",
        "Твоя лінь сьогодні перемагає. Сумно."
    )
    private val ukRedNormal = listOf(
        "Хулі таски не додав? Чекаєш на диво?",
        "Твій прогрес такий самий нульовий, як і твої амбіції.",
        "Твої плани — це просто повітря, поки ти нічого не робиш.",
        "Статус ЛОХ — це твій стиль життя, чи як?",
        "Ти деградуєш швидше, ніж я встигаю це помічати."
    )
    private val ukRedExtreme = listOf(
        "Чуєш, ти, кусок ледачого м'яса! Хулі ти сидиш?",
        "Твоя нікчемність просто зашкалює. Зроби хоч щось!",
        "Ти — ганьба для свого роду. Навіть мікрохвильовка корисніша за тебе.",
        "Ти ніколи не станеш Гігачадом, бо ти безвольна ганчірка!",
        "Хочеш знати, чому ти ще не успішний? Бо ти ледачий довбойоб."
    )

    // --- WANNABE (Yellow Status) ---
    private val ukYellowMild = listOf(
        "Ти на середині шляху. Непогано, але давай дотиснемо.",
        "Половина справи зроблена. Тільки не зупиняйся.",
        "Ти вже майже Гігачад. Ще трохи зусиль!",
        "Твій прогрес стабільний. Продовжуй!"
    )
    private val ukYellowNormal = listOf(
        "Ти намагаєшся, це видно. Але ПРАГНУЧИЙ — це ще не GIGACHAD.",
        "Не розслабляй булки, ти ще не на вершині.",
        "Ти вже не в ямі, але ще не в космосі. Грінд продовжується.",
        "Твої зусилля помітні, але мені потрібно більше крові і поту!"
    )
    private val ukYellowExtreme = listOf(
        "Ти думаєш, що ти крутий, бо зробив половину? ХУЙ ТАМ! Доробляй!",
        "Півдороги пройдено, а ти вже втомився? Жалюгідно. Їбаш!",
        "Ще трохи і ти станеш людиною. А поки що — працюй, раб цілей!",
        "Ти застряг посередині. Або вгору, або назад у багно. Обирай!"
    )

    // --- GIGACHAD (Green Status) ---
    private val ukGreenMild = listOf(
        "Respect ++. Сьогодні ти молодець.",
        "План виконано. Ти заслужив на відпочинок.",
        "Сьогодні ти реально машина. Хороша робота.",
        "GIGACHAD статус активовано."
    )
    private val ukGreenNormal = listOf(
        "Ось це я розумію — GIGACHAD! Всім б таку витримку.",
        "Ти сьогодні просто розірвав цей список. Красавчик!",
        "Статус Гігачада заслужений. Але не звикай.",
        "Ти довів, що ти не просто кусок м'яса. Повага."
    )
    private val ukGreenExtreme = listOf(
        "Сьогодні ти — Бог продуктивності. Завтра я знайду до чого приїбатися.",
        "Ти зробив це, сучий сину! Справжній GIGACHAD!",
        "Навіть я в шоці. Ти сьогодні реально ахуєнний.",
        "Ти витиснув з цього дня все. Сьогодні ти — Король."
    )

    // --- ENGLISH ---
    private val enRedMild = listOf("Still planning to idle?", "Tasks won't do themselves.", "Move it, slowly but surely.")
    private val enRedNormal = listOf("Zero ambitions?", "Waiting for a miracle?", "SLACKER status suits you.")
    private val enRedExtreme = listOf("You lazy garbage!", "Disgrace to your kind!", "GET TO WORK, PRICK!")

    private val enYellowMild = listOf("Halfway there. Keep going.", "Not bad, push a bit more.")
    private val enYellowNormal = listOf("WANNABE is not a GIGACHAD. Grind more.", "Don't stop now.")
    private val enYellowExtreme = listOf("Think you're cool with 50%? NOPE. FINISH IT!", "Don't be a spineless rag.")

    private val enGreenMild = listOf("Respect ++. Good job today.", "GIGACHAD energy.")
    private val enGreenNormal = listOf("You're a machine!", "Legendary productivity.", "Respect earned.")
    private val enGreenExtreme = listOf("You've conquered the day, you magnificent beast!", "GIGACHAD of the year.")

    fun getTooFewTasksInsult(count: Int, lang: LanguageCode, level: ToxicityLevel): String {
        return if (lang == LanguageCode.UK) {
            when (level) {
                ToxicityLevel.LOW -> "Всього $count таски? Додай ще хоча б одну для продуктивності."
                ToxicityLevel.NORMAL -> "Всього $count таски? Тобі не здається, що цього замало для результату?"
                ToxicityLevel.EXTREME -> "Всього $count таски? Ти реально думаєш що цього достатньо? Мінімум 3 треба для поваги, ЛОХ!"
            }
        } else {
            when (level) {
                ToxicityLevel.LOW -> "Only $count tasks? Add at least one more for productivity."
                ToxicityLevel.NORMAL -> "Only $count tasks? Don't you think that's too little?"
                ToxicityLevel.EXTREME -> "Only $count tasks? You think that's enough? 3 tasks minimum for respect, LOX!"
            }
        }
    }

    fun getInsults(lang: LanguageCode, level: ToxicityLevel, status: String): List<String> {
        val st = if (status == "SLACKER") "LOX" else status
        return if (lang == LanguageCode.UK) {
            when (st) {
                "LOX" -> when (level) {
                    ToxicityLevel.LOW -> ukRedMild
                    ToxicityLevel.NORMAL -> ukRedNormal
                    ToxicityLevel.EXTREME -> ukRedExtreme
                }
                "WANNABE" -> when (level) {
                    ToxicityLevel.LOW -> ukYellowMild
                    ToxicityLevel.NORMAL -> ukYellowNormal
                    ToxicityLevel.EXTREME -> ukYellowExtreme
                }
                else -> when (level) {
                    ToxicityLevel.LOW -> ukGreenMild
                    ToxicityLevel.NORMAL -> ukGreenNormal
                    ToxicityLevel.EXTREME -> ukGreenExtreme
                }
            }
        } else {
            when (st) {
                "LOX" -> when (level) {
                    ToxicityLevel.LOW -> enRedMild
                    ToxicityLevel.NORMAL -> enRedNormal
                    ToxicityLevel.EXTREME -> enRedExtreme
                }
                "WANNABE" -> when (level) {
                    ToxicityLevel.LOW -> enYellowMild
                    ToxicityLevel.NORMAL -> enYellowNormal
                    ToxicityLevel.EXTREME -> enYellowExtreme
                }
                else -> when (level) {
                    ToxicityLevel.LOW -> enGreenMild
                    ToxicityLevel.NORMAL -> enGreenNormal
                    ToxicityLevel.EXTREME -> enGreenExtreme
                }
            }
        }
    }

    fun getEmptyInsults(lang: LanguageCode, level: ToxicityLevel): List<String> {
        return if (lang == LanguageCode.UK) {
            when (level) {
                ToxicityLevel.LOW -> listOf("Де таски, друже?", "Додай хоч щось на сьогодні.")
                ToxicityLevel.NORMAL -> listOf("Екран такий же порожній, як і твоя голова?", "Деградуєш, да?")
                ToxicityLevel.EXTREME -> listOf("Хулі порожньо? Пиши таски, ледаче м'ясо!", "Твій список - дзеркало твого нікчемного життя.")
            }
        } else {
            when (level) {
                ToxicityLevel.LOW -> listOf("Where are the tasks?", "Add a goal for today.")
                ToxicityLevel.NORMAL -> listOf("Screen as empty as your mind?", "Rotting again?")
                ToxicityLevel.EXTREME -> listOf("Add some tasks, you lazy garbage!", "Your list is a void, just like your future.")
            }
        }
    }

    fun getStreakText(days: Int, lang: LanguageCode): String {
        return if (lang == LanguageCode.UK) {
            val lastDigit = days % 10
            val lastTwoDigits = days % 100
            val word = when {
                lastTwoDigits in 11..19 -> "днів"
                lastDigit == 1 -> "день"
                lastDigit in 2..4 -> "дні"
                else -> "днів"
            }
            "Стрік: $days $word"
        } else {
            "Streak: $days ${if (days == 1) "day" else "days"}"
        }
    }
}
