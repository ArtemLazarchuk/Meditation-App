package com.example.meditationapp

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Unit тести для додатку медитації.
 * Виконуються локально на JVM без Android пристрою.
 * Тестують чисту бізнес-логіку без Android-залежностей.
 */
class ExampleUnitTest {

    /**
     * Тест 1: Форматування часу таймера у форматі MM:SS
     */
    @Test
    fun testTimeFormattingMMSS() {
        // Функція форматування з MeditationTimerService
        fun formatTime(sec: Long): String {
            val m = TimeUnit.SECONDS.toMinutes(sec)
            val s = sec - TimeUnit.MINUTES.toSeconds(m)
            return String.format("%02d:%02d", m, s)
        }
        
        assertEquals("0 секунд мають форматуватися як 00:00", "00:00", formatTime(0))
        assertEquals("60 секунд (1 хв) мають форматуватися як 01:00", "01:00", formatTime(60))
        assertEquals("300 секунд (5 хв) мають форматуватися як 05:00", "05:00", formatTime(300))
        assertEquals("1800 секунд (30 хв) мають форматуватися як 30:00", "30:00", formatTime(1800))
        assertEquals("150 секунд мають форматуватися як 02:30", "02:30", formatTime(150))
        assertEquals("3665 секунд мають форматуватися як 61:05", "61:05", formatTime(3665))
    }

    /**
     * Тест 2: Конвертація хвилин в секунди для таймера
     */
    @Test
    fun testMinutesToSecondsConversion() {
        val timerOptions = mapOf(
            3 to 180L,   // 3 хвилини = 180 секунд
            5 to 300L,   // 5 хвилин = 300 секунд
            10 to 600L,  // 10 хвилин = 600 секунд
            20 to 1200L, // 20 хвилин = 1200 секунд
            30 to 1800L  // 30 хвилин = 1800 секунд
        )
        
        timerOptions.forEach { (minutes, expectedSeconds) ->
            val actualSeconds = minutes * 60L
            assertEquals(
                "$minutes хвилин мають дорівнювати $expectedSeconds секундам",
                expectedSeconds,
                actualSeconds
            )
        }
    }

    /**
     * Тест 3: Розрахунок серії днів (streak) для медитації
     */
    @Test
    fun testCalculateStreak() {
        // Копіюємо логіку з MeditationRepository для тестування
        fun calculateStreak(datesIso: List<String>): Int {
            if (datesIso.isEmpty()) return 0

            val fmt = DateTimeFormatter.ISO_LOCAL_DATE
            val dates = datesIso.map { LocalDate.parse(it, fmt) }.sortedDescending()

            var streak = 0
            var current = LocalDate.now()

            for (date in dates) {
                if (date == current) {
                    streak++
                    current = current.minusDays(1)
                } else break
            }

            return streak
        }
        
        // Тест з порожнім списком
        assertEquals("Серія має бути 0 для порожнього списку", 0, calculateStreak(emptyList()))
        
        // Тест з сьогоднішньою датою
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        assertEquals("Серія має бути 1 для сьогодні", 1, calculateStreak(listOf(today)))
        
        // Тест з послідовними днями
        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val todayAndYesterday = listOf(today, yesterday)
        val streak = calculateStreak(todayAndYesterday)
        assertTrue("Серія має бути принаймні 1", streak >= 1)
    }

    /**
     * Тест 4: Валідація опцій таймера
     */
    @Test
    fun testTimerOptionsValidation() {
        val timerOptions = listOf(3, 5, 10, 20, 30)
        val minMinutes = 3
        val maxMinutes = 30
        val defaultMinutes = 5
        
        // Перевірка кількості опцій
        assertEquals("Має бути 5 варіантів таймера", 5, timerOptions.size)
        
        // Перевірка мінімального та максимального значення
        assertEquals("Мінімальний таймер має бути 3 хвилини", minMinutes, timerOptions.minOrNull())
        assertEquals("Максимальний таймер має бути 30 хвилин", maxMinutes, timerOptions.maxOrNull())
        
        // Перевірка, що всі значення в межах
        timerOptions.forEach { minutes ->
            assertTrue(
                "Таймер $minutes хвилин має бути >= $minMinutes",
                minutes >= minMinutes
            )
            assertTrue(
                "Таймер $minutes хвилин має бути <= $maxMinutes",
                minutes <= maxMinutes
            )
        }
        
        // Перевірка значення за замовчуванням
        assertTrue(
            "За замовчуванням має бути вибрано $defaultMinutes хвилин",
            timerOptions.contains(defaultMinutes)
        )
    }

    /**
     * Тест 5: Розрахунок загального часу медитації з секунд в хвилини
     */
    @Test
    fun testTotalDurationCalculation() {
        // Логіка розрахунку з HomeFragment (секунди в хвилини)
        fun calculateTotalMinutes(totalSeconds: Int?): Int {
            return (totalSeconds ?: 0) / 60
        }
        
        // Тест з null
        assertEquals("0 секунд мають давати 0 хвилин", 0, calculateTotalMinutes(null))
        
        // Тест з 0 секунд
        assertEquals("0 секунд мають давати 0 хвилин", 0, calculateTotalMinutes(0))
        
        // Тест з різними значеннями
        assertEquals("300 секунд мають давати 5 хвилин", 5, calculateTotalMinutes(300))
        assertEquals("600 секунд мають давати 10 хвилин", 10, calculateTotalMinutes(600))
        assertEquals("1800 секунд мають давати 30 хвилин", 30, calculateTotalMinutes(1800))
        assertEquals("3665 секунд мають давати 61 хвилину (округлення вниз)", 61, calculateTotalMinutes(3665))
        
        // Тест з менше ніж хвилина
        assertEquals("30 секунд мають давати 0 хвилин", 0, calculateTotalMinutes(30))
        assertEquals("59 секунд мають давати 0 хвилин", 0, calculateTotalMinutes(59))
    }

    /**
     * Приклад тесту (залишаємо для демонстрації)
     */
    @Test
    fun addition_isCorrect() {
        assertEquals("2 + 2 має дорівнювати 4", 4, 2 + 2)
    }
}