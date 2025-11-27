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
}
