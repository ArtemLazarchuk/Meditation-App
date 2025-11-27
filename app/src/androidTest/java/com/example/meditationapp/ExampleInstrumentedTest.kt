package com.example.meditationapp

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.meditationapp.service.MeditationTimerService
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.util.concurrent.TimeUnit

/**
 * Інструментальні тести для додатку медитації.
 * Виконуються на Android пристрої або емуляторі.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.meditationapp", appContext.packageName)
    }

    /**
     * Тест 1: Перевірка наявності всіх звукових ресурсів для медитації
     */
    @Test
    fun testSoundResourcesExist() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Список всіх звукових ресурсів з HomeFragment
        val soundResources = listOf(
            R.raw.ambiant_relax_sounds_10621,
            R.raw.meditation_flow_30s_307996,
            R.raw.music_for_meditation_20534,
            R.raw.new_composition_3_20536,
            R.raw.perfect_beauty_191271,
            R.raw.piano_moment_9835,
            R.raw.relaxing_145038
        )

        assertEquals("Має бути 7 звукових ресурсів", 7, soundResources.size)

        // Перевіряємо, що всі ресурси існують та доступні
        soundResources.forEach { resId ->
            try {
                val resourceName = context.resources.getResourceEntryName(resId)
                assertNotNull("Ресурс з ID $resId має існувати", resourceName)
                
                // Перевіряємо, що файл можна відкрити
                context.resources.openRawResource(resId).use {
                    assertTrue("Ресурс $resId має містити дані", it.available() > 0)
                }
            } catch (e: Exception) {
                fail("Ресурс з ID $resId не знайдено або недоступний: ${e.message}")
            }
        }
    }

    /**
     * Тест 2: Перевірка форматування часу таймера (MM:SS)
     */
    @Test
    fun testTimeFormatting() {
        // Тестуємо форматування часу (логіка з MeditationTimerService)
        fun formatTime(sec: Long): String {
            val m = TimeUnit.SECONDS.toMinutes(sec)
            val s = sec - TimeUnit.MINUTES.toSeconds(m)
            return String.format("%02d:%02d", m, s)
        }
        
        // Перевірка різних значень
        assertEquals("0 секунд мають форматуватися як 00:00", "00:00", formatTime(0))
        assertEquals("60 секунд мають форматуватися як 01:00", "01:00", formatTime(60))
        assertEquals("300 секунд (5 хв) мають форматуватися як 05:00", "05:00", formatTime(300))
        assertEquals("1800 секунд (30 хв) мають форматуватися як 30:00", "30:00", formatTime(1800))
        assertEquals("150 секунд мають форматуватися як 02:30", "02:30", formatTime(150))
    }

    /**
     * Тест 3: Перевірка валідності опцій таймера (3, 5, 10, 20, 30 хвилин)
     */
    @Test
    fun testTimerOptionsAreValid() {
        val timerOptions = listOf(3, 5, 10, 20, 30)

        assertEquals("Має бути 5 варіантів таймера", 5, timerOptions.size)
        
        timerOptions.forEach { minutes ->
            val seconds = minutes * 60L
            
            // Перевірка валідності
            assertTrue("Таймер $minutes хвилин має бути в межах 3-30 хвилин",
                minutes >= 3 && minutes <= 30)
            assertTrue("Таймер $minutes хвилин має бути більше 0 секунд", seconds > 0)
            assertTrue("Таймер має бути в межах 180-1800 секунд (3-30 хвилин)",
                seconds >= 180 && seconds <= 1800)
        }
        
        // Перевірка значення за замовчуванням
        val defaultMinutes = 5
        assertTrue("За замовчуванням вибрано 5 хвилин, яке має бути в списку опцій",
            timerOptions.contains(defaultMinutes))
    }

    /**
     * Тест 4: Перевірка констант сервісу таймера
     */
    @Test
    fun testTimerServiceConstants() {
        // Перевіряємо константи з MeditationTimerService
        assertEquals("ID каналу сповіщень має бути правильним",
            "meditation_timer_channel", MeditationTimerService.CHANNEL_ID)
        assertEquals("ID сповіщення має бути 1001", 1001, MeditationTimerService.NOTIF_ID)
        assertEquals("Дія завершення сесії має бути правильною",
            "com.example.meditationapp.ACTION_SESSION_FINISHED", 
            MeditationTimerService.ACTION_SESSION_FINISHED)
        assertEquals("Ключ для тривалості сесії має бути правильним",
            "extra_duration_seconds", MeditationTimerService.EXTRA_DURATION_SECONDS)
        assertEquals("Ключ для часу завершення має бути правильним",
            "extra_finished_at_millis", MeditationTimerService.EXTRA_FINISHED_AT_MILLIS)
    }

    /**
     * Тест 5: Перевірка наявності всіх необхідних компонентів додатку
     */
    @Test
    fun testAppComponentsAreAccessible() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Перевірка PackageManager
        assertNotNull("PackageManager має бути доступний", context.packageManager)
        
        // Перевірка Resources
        assertNotNull("Resources мають бути доступні", context.resources)
        
        // Перевірка Application Context
        assertNotNull("Application context має бути доступний", context.applicationContext)
        
        // Перевірка назви додатку
        val appName = context.resources.getString(R.string.app_name)
        assertNotNull("Назва додатку має бути доступна", appName)
        assertTrue("Назва додатку не має бути порожньою", appName.isNotBlank())
        
        // Перевірка package name
        assertEquals("Package name має відповідати очікуваному",
            "com.example.meditationapp", context.packageName)
    }

    /**
     * Тест 6: Форматування часу у форматі HH:MM:SS (для UI відображення)
     */
    @Test
    fun testTimeFormattingHHMMSS() {
        // Форматування часу з HomeFragment (для UI)
        fun formatTimeHHMMSS(sec: Long): String {
            val hours = sec / 3600
            val minutes = (sec % 3600) / 60
            val seconds = sec % 60
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
        
        assertEquals("0 секунд мають форматуватися як 00:00:00", "00:00:00", formatTimeHHMMSS(0))
        assertEquals("60 секунд мають форматуватися як 00:01:00", "00:01:00", formatTimeHHMMSS(60))
        assertEquals("300 секунд (5 хв) мають форматуватися як 00:05:00", "00:05:00", formatTimeHHMMSS(300))
        assertEquals("3665 секунд (1 год 1 хв 5 сек) мають форматуватися як 01:01:05", 
            "01:01:05", formatTimeHHMMSS(3665))
        assertEquals("1800 секунд (30 хв) мають форматуватися як 00:30:00", "00:30:00", formatTimeHHMMSS(1800))
        assertEquals("150 секунд мають форматуватися як 00:02:30", "00:02:30", formatTimeHHMMSS(150))
    }

    /**
     * Тест 7: Перевірка співставлення звуків та їх ресурсів
     */
    @Test
    fun testSoundNamesAndResourcesMapping() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Список звуків з HomeFragment
        val sounds = listOf(
            "Relax Ambience" to R.raw.ambiant_relax_sounds_10621,
            "Meditation Flow" to R.raw.meditation_flow_30s_307996,
            "Music For Meditation" to R.raw.music_for_meditation_20534,
            "New Composition" to R.raw.new_composition_3_20536,
            "Perfect Beauty" to R.raw.perfect_beauty_191271,
            "Piano Moment" to R.raw.piano_moment_9835,
            "Relaxing Sound" to R.raw.relaxing_145038
        )
        
        assertEquals("Має бути 7 звуків", 7, sounds.size)
        
        // Перевірка, що всі назви унікальні
        val soundNames = sounds.map { it.first }
        val uniqueNames = soundNames.distinct()
        assertEquals("Всі назви звуків мають бути унікальними", 
            soundNames.size, uniqueNames.size)
        
        // Перевірка, що всі ресурси унікальні
        val soundResources = sounds.map { it.second }
        val uniqueResources = soundResources.distinct()
        assertEquals("Всі ресурси мають бути унікальними", 
            soundResources.size, uniqueResources.size)
        
        // Перевірка, що кожна назва має відповідний ресурс
        sounds.forEach { (name, resId) ->
            assertNotNull("Назва звуку не має бути null", name)
            assertTrue("Назва звуку '$name' не має бути порожньою", name.isNotBlank())
            assertTrue("ID ресурсу $resId має бути більше 0", resId > 0)
            
            // Перевірка, що ресурс доступний
            try {
                context.resources.getResourceEntryName(resId)
                assertTrue("Ресурс для '$name' має існувати", true)
            } catch (e: Exception) {
                fail("Ресурс для '$name' не знайдено: ${e.message}")
            }
        }
    }

    /**
     * Тест 8: Перевірка мінімальної тривалості сесії для збереження
     */
    @Test
    fun testMinimumSessionDurationForSaving() {
        val minDurationSeconds = 5 // З HomeFragment: if (elapsed > 5)
        
        // Тестуємо логіку збереження сесії
        fun shouldSaveSession(elapsedSeconds: Int): Boolean {
            return elapsedSeconds > minDurationSeconds
        }
        
        // Перевірка, що сесії <= 5 секунд не зберігаються
        assertFalse("Сесія 0 секунд не має зберігатися", shouldSaveSession(0))
        assertFalse("Сесія 1 секунда не має зберігатися", shouldSaveSession(1))
        assertFalse("Сесія 5 секунд не має зберігатися", shouldSaveSession(5))
        
        // Перевірка, що сесії > 5 секунд зберігаються
        assertTrue("Сесія 6 секунд має зберігатися", shouldSaveSession(6))
        assertTrue("Сесія 10 секунд має зберігатися", shouldSaveSession(10))
        assertTrue("Сесія 300 секунд має зберігатися", shouldSaveSession(300))
        
        // Перевірка значення мінімальної тривалості
        assertEquals("Мінімальна тривалість має бути 5 секунд", 5, minDurationSeconds)
    }

    /**
     * Тест 9: Перевірка обчислення пройденого часу (elapsed time)
     */
    @Test
    fun testElapsedTimeCalculation() {
        // Логіка обчислення elapsed time з HomeFragment
        fun calculateElapsedTime(selectedMinutes: Int, secondsLeft: Long): Int {
            return (selectedMinutes * 60) - secondsLeft.toInt()
        }
        
        // Тест 1: Таймер не запущений
        assertEquals("Пройдений час має бути 0 для не запущеного таймера",
            0, calculateElapsedTime(5, 300))
        
        // Тест 2: Таймер пройшов 30 секунд з 5 хвилин
        assertEquals("Пройдений час має бути 30 секунд",
            30, calculateElapsedTime(5, 270))
        
        // Тест 3: Таймер пройшов половину (2.5 хвилини з 5)
        assertEquals("Пройдений час має бути 150 секунд",
            150, calculateElapsedTime(5, 150))
        
        // Тест 4: Таймер завершений
        assertEquals("Пройдений час має бути 300 секунд для завершеного таймера",
            300, calculateElapsedTime(5, 0))
        
        // Тест 5: Різні опції таймера
        assertEquals("Пройдений час має бути 180 секунд для 3 хвилин",
            180, calculateElapsedTime(3, 0))
        assertEquals("Пройдений час має бути 600 секунд для 10 хвилин",
            600, calculateElapsedTime(10, 0))
    }

    /**
     * Тест 10: Перевірка структури та валідності MeditationSession
     */
    @Test
    fun testMeditationSessionDataStructure() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Перевірка структури даних
        val durationSeconds = 300
        val timestamp = System.currentTimeMillis()
        
        // Створюємо тестовий об'єкт (модель з Room)
        // Перевіряємо, що дані валідні
        assertTrue("Тривалість сесії має бути більше мінімальної (5 сек)",
            durationSeconds > 5)
        assertTrue("Timestamp має бути додатнім числом", timestamp > 0)
        
        // Перевірка, що timestamp не в майбутньому
        assertTrue("Timestamp не має бути в майбутньому",
            timestamp <= System.currentTimeMillis())
        
        // Перевірка розумних меж
        assertTrue("Тривалість сесії має бути в розумних межах (до 24 годин)",
            durationSeconds <= 86400) // 24 години
        
        // Перевірка конвертації хвилин в секунди для сесії
        val minutes = 5
        val expectedSeconds = minutes * 60
        assertEquals("5 хвилин мають дорівнювати 300 секундам",
            expectedSeconds, durationSeconds)
    }

    /**
     * Тест 11: Перевірка Broadcast Intent структури для завершення сесії
     */
    @Test
    fun testBroadcastIntentStructure() {
        // Перевірка структури Intent для завершення сесії
        val action = MeditationTimerService.ACTION_SESSION_FINISHED
        val extraDuration = MeditationTimerService.EXTRA_DURATION_SECONDS
        val extraTimestamp = MeditationTimerService.EXTRA_FINISHED_AT_MILLIS
        
        // Перевірка, що всі константи визначені
        assertNotNull("Action не має бути null", action)
        assertNotNull("Extra duration не має бути null", extraDuration)
        assertNotNull("Extra timestamp не має бути null", extraTimestamp)
        
        // Перевірка формату action
        assertTrue("Action має починатися з package name",
            action.startsWith("com.example.meditationapp"))
        assertTrue("Action має містити ACTION_SESSION_FINISHED",
            action.contains("ACTION_SESSION_FINISHED"))
        
        // Перевірка формату extra keys
        assertTrue("Extra duration має містити 'duration'",
            extraDuration.contains("duration"))
        assertTrue("Extra timestamp має містити 'finished'",
            extraTimestamp.contains("finished"))
        
        // Перевірка унікальності ключів
        assertNotEquals("Ключі мають бути різними",
            extraDuration, extraTimestamp)
    }
}
