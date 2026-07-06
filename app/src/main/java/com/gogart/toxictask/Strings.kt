package com.gogart.toxictask

import com.gogart.toxictask.settings.LanguageCode
import com.gogart.toxictask.settings.ToxicityLevel

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
    private val enRedNormal = listOf("Zero ambitions?", "Waiting for a miracle?", "LOSER status suits you.")
    private val enRedExtreme = listOf("You lazy garbage!", "Disgrace to your kind!", "GET TO WORK, PRICK!")

    private val enYellowMild = listOf("Halfway there. Keep going.", "Not bad, push a bit more.")
    private val enYellowNormal = listOf("WANNABE is not a GIGACHAD. Grind more.", "Don't stop now.")
    private val enYellowExtreme = listOf("Think you're cool with 50%? NOPE. FINISH IT!", "Don't be a spineless rag.")

    private val enGreenMild = listOf("Respect ++. Good job today.", "GIGACHAD energy.")
    private val enGreenNormal = listOf("You're a machine!", "Legendary productivity.", "Respect earned.")
    private val enGreenExtreme = listOf("You've conquered the day, you magnificent beast!", "GIGACHAD of the year.")

    // --- GERMAN ---
    private val deRedMild = listOf("Willst du noch lange faulenzen?", "Der Plan erledigt sich nicht von selbst.", "Komm in die Gänge, langsam aber sicher.")
    private val deRedNormal = listOf("Null Ambitionen?", "Wartest du auf ein Wunder?", "VERSAGER-Status steht dir gut.")
    private val deRedExtreme = listOf("Du faules Stück Fleisch!", "Schande für deine Art!", "AN DIE ARBEIT, DU PFEIFE!")

    private val deYellowMild = listOf("Halbzeit. Mach weiter.", "Nicht schlecht, gib noch etwas Gas.")
    private val deYellowNormal = listOf("WANNABE ist kein GIGACHAD. Arbeite härter.", "Hör jetzt nicht auf.")
    private val deYellowExtreme = listOf("Denkst du, 50% sind cool? NIX DA! MACH ES ZU ENDE!", "Sei kein rückgratloser Waschlappen.")

    private val deGreenMild = listOf("Respekt ++. Gute Arbeit heute.", "GIGACHAD-Energie.")
    private val deGreenNormal = listOf("Du bist eine Maschine!", "Legendäre Produktivität.", "Respekt verdient.")
    private val deGreenExtreme = listOf("Du hast den Tag bezwungen, du prachtvolles Biest!", "GIGACHAD des Jahres.")

    fun getTooFewTasksInsult(count: Int, lang: LanguageCode, level: ToxicityLevel): String {
        return when (lang) {
            LanguageCode.UK -> {
                when (level) {
                    ToxicityLevel.LOW -> "Всього $count таски? Додай ще хоча б одну для продуктивності."
                    ToxicityLevel.NORMAL -> "Всього $count таски? Тобі не здається, що цього замало для результату?"
                    ToxicityLevel.EXTREME -> "Всього $count таски? Ти реально думаєш що цього достатньо? Мінімум 3 треба для поваги, ЛОХ!"
                }
            }
            LanguageCode.DE -> {
                when (level) {
                    ToxicityLevel.LOW -> "Nur $count Aufgaben? Füge mindestens eine weitere für die Produktivität hinzu."
                    ToxicityLevel.NORMAL -> "Nur $count Aufgaben? Findest du nicht, dass das zu wenig ist?"
                    ToxicityLevel.EXTREME -> "Nur $count Aufgaben? Glaubst du wirklich, das reicht? Mindestens 3 für Respekt, du LOSER!"
                }
            }
            else -> {
                when (level) {
                    ToxicityLevel.LOW -> "Only $count tasks? Add at least one more for productivity."
                    ToxicityLevel.NORMAL -> "Only $count tasks? Don't you think that's too little?"
                    ToxicityLevel.EXTREME -> "Only $count tasks? You think that's enough? 3 tasks minimum for respect, LOSER!"
                }
            }
        }
    }

    fun getInsults(lang: LanguageCode, level: ToxicityLevel, status: String): List<String> {
        val st = if (status == "SLACKER") "LOX" else status
        return when (lang) {
            LanguageCode.UK -> {
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
            }
            LanguageCode.DE -> {
                when (st) {
                    "LOX" -> when (level) {
                        ToxicityLevel.LOW -> deRedMild
                        ToxicityLevel.NORMAL -> deRedNormal
                        ToxicityLevel.EXTREME -> deRedExtreme
                    }
                    "WANNABE" -> when (level) {
                        ToxicityLevel.LOW -> deYellowMild
                        ToxicityLevel.NORMAL -> deYellowNormal
                        ToxicityLevel.EXTREME -> deYellowExtreme
                    }
                    else -> when (level) {
                        ToxicityLevel.LOW -> deGreenMild
                        ToxicityLevel.NORMAL -> deGreenNormal
                        ToxicityLevel.EXTREME -> deGreenExtreme
                    }
                }
            }
            else -> {
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
    }

    fun getEmptyInsults(lang: LanguageCode, level: ToxicityLevel): List<String> {
        return when (lang) {
            LanguageCode.UK -> {
                when (level) {
                    ToxicityLevel.LOW -> listOf("Де таски, друже?", "Додай хоч щось на сьогодні.")
                    ToxicityLevel.NORMAL -> listOf("Екран такий же порожній, як і твоя голова?", "Деградуєш, да?")
                    ToxicityLevel.EXTREME -> listOf("Хулі порожньо? Пиши таски, ледаче м'ясо!", "Твій список - дзеркало твого нікчемного життя.")
                }
            }
            LanguageCode.DE -> {
                when (level) {
                    ToxicityLevel.LOW -> listOf("Wo sind die Aufgaben, Freund?", "Füge heute etwas hinzu.")
                    ToxicityLevel.NORMAL -> listOf("Ist der Bildschirm so leer wie dein Kopf?", "Du baust ab, oder?")
                    ToxicityLevel.EXTREME -> listOf("Warum so leer? Schreib Aufgaben, du faules Stück!", "Deine Liste ist ein Spiegel deines wertlosen Lebens.")
                }
            }
            else -> {
                when (level) {
                    ToxicityLevel.LOW -> listOf("Where are the tasks?", "Add a goal for today.")
                    ToxicityLevel.NORMAL -> listOf("Screen as empty as your mind?", "Rotting again?")
                    ToxicityLevel.EXTREME -> listOf("Add some tasks, you lazy garbage!", "Your list is a void, just like your future.")
                }
            }
        }
    }

    fun getTimeFixMessage(lang: LanguageCode): String {
        return when (lang) {
            LanguageCode.UK -> "Я виправив це на 23:59, бо ти не здатний навіть час налаштувати."
            LanguageCode.DE -> "Auf 23:59 korrigiert. Sogar mein Code kennt die Zeit besser als du, Faulpelz."
            else -> "Changed to 23:59. Even my code knows time better than you, slacker."
        }
    }

    fun getTimeErrorMessage(startTime: String, endTime: String, lang: LanguageCode, level: ToxicityLevel): String {
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
                    ToxicityLevel.EXTREME -> "Zeit $endTime ist kleiner als $startTime. Bist du wirklich ein LOSER oder tust du nur so?"
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

    fun getStreakText(days: Int, lang: LanguageCode): String {
        return when (lang) {
            LanguageCode.UK -> {
                val lastDigit = days % 10
                val lastTwoDigits = days % 100
                val word = when {
                    lastTwoDigits in 11..19 -> "днів"
                    lastDigit == 1 -> "день"
                    lastDigit in 2..4 -> "дні"
                    else -> "днів"
                }
                "Стрік: $days $word"
            }
            LanguageCode.DE -> "Streak: $days ${if (days == 1) "Tag" else "Tage"}"
            else -> "Streak: $days ${if (days == 1) "day" else "days"}"
        }
    }

    fun getNotificationStrings(lang: LanguageCode, type: String, taskTitle: String = "", deadline: String = ""): Pair<String, String> {
        return when (lang) {
            LanguageCode.UK -> when (type) {
                "INACTIVE" -> "ТИТУЛ ЛОХА ПІДТВЕРДЖЕНО!" to "Ти вже 3 дні нічого не робиш! Твій список тасків такий же порожній, як і твоє майбутнє!"
                "EXPIRED" -> "ДЕДЛАЙН МИНУВ!" to "Місія '$taskTitle' провалена! Дедлайн був о $deadline."
                "LAST_CHANCE" -> "ОСТАННІЙ ШАНС!" to "Останній шанс виконати '$taskTitle'!"
                "URGENT" -> "ЧАС ПІДЖИМАЄ!" to "Ти ще не виконав '$taskTitle'!"
                "END_OF_DAY" -> "ДЕНЬ ЗАКІНЧУЄТЬСЯ!" to "День закінчується, а ти ще не добив план! Живо за роботу!"
                else -> "ЕЙ, ТИ!" to "Вставай і працюй!"
            }
            LanguageCode.DE -> when (type) {
                "INACTIVE" -> "VERSAGER-TITEL BESTÄTIGT!" to "Du hast seit 3 Tagen nichts getan! Deine Aufgabenliste ist so leer wie deine Zukunft!"
                "EXPIRED" -> "DEADLINE ABGELAUFEN!" to "Mission '$taskTitle' fehlgeschlagen! Deadline war um $deadline."
                "LAST_CHANCE" -> "LETZTE CHANCE!" to "Letzte Chance, '$taskTitle' zu erledigen!"
                "URGENT" -> "DIE ZEIT LÄUFT AB!" to "Du hast '$taskTitle' noch nicht erledigt!"
                "END_OF_DAY" -> "TAG ENDET!" to "Der Tag endet und du hast den Plan nicht erfüllt! Los geht's!"
                else -> "HEY DU!" to "Steh auf und arbeite!"
            }
            else -> when (type) {
                "INACTIVE" -> "LOSER TITLE CONFIRMED!" to "You haven't done anything for 3 days! Your task list is as empty as your future!"
                "EXPIRED" -> "DEADLINE EXPIRED!" to "Mission '$taskTitle' failed! Deadline was at $deadline."
                "LAST_CHANCE" -> "LAST CHANCE!" to "Last chance to complete '$taskTitle'!"
                "URGENT" -> "TIME IS RUNNING OUT!" to "You haven't finished '$taskTitle'!"
                "END_OF_DAY" -> "DAY IS ENDING!" to "The day is ending and you haven't finished the plan! Move it!"
                else -> "HEY YOU!" to "Get up and work!"
            }
        }
    }
}
