package com.example.meditationapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI тести для HomeFragment.
 * Перевіряють взаємодію користувача з інтерфейсом.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class HomeFragmentUITest {

    @Before
    fun setUp() {
        // Запускаємо MainActivity перед кожним тестом
        ActivityScenario.launch(MainActivity::class.java)
    }

    /**
     * Тест 1: Перевірка наявності основних елементів UI
     */
    @Test
    fun testMainUIElementsAreVisible() {
        // Перевірка заголовка
        onView(withId(R.id.tvSessionTitle))
            .check(matches(isDisplayed()))
            .check(matches(withText("Медитація")))

        // Перевірка спінера для вибору звуку
        onView(withId(R.id.spinnerSound))
            .check(matches(isDisplayed()))

        // Перевірка опцій вибору часу
        onView(withId(R.id.time3))
            .check(matches(isDisplayed()))
            .check(matches(withText("3 хв")))

        onView(withId(R.id.time5))
            .check(matches(isDisplayed()))
            .check(matches(withText("5 хв")))

        onView(withId(R.id.time10))
            .check(matches(isDisplayed()))
            .check(matches(withText("10 хв")))

        // Перевірка таймера
        onView(withId(R.id.tvTimer))
            .check(matches(isDisplayed()))
            .check(matches(withText("00:00:00")))

        // Перевірка ProgressBar
        onView(withId(R.id.progressTimer))
            .check(matches(isDisplayed()))

        // Перевірка кнопок
        onView(withId(R.id.btnStart))
            .check(matches(isDisplayed()))
            .check(matches(withText("START")))

        onView(withId(R.id.btnPause))
            .check(matches(isDisplayed()))
            .check(matches(withText("PAUSE")))

        onView(withId(R.id.btnResume))
            .check(matches(isDisplayed()))
            .check(matches(withText("RESUME")))

        onView(withId(R.id.btnStop))
            .check(matches(isDisplayed()))
            .check(matches(withText("STOP")))
    }

    /**
     * Тест 2: Перевірка статистики (інформаційна панель)
     */
    @Test
    fun testStatisticsPanelIsVisible() {
        // Перевірка тексту загальної кількості сесій
        onView(withId(R.id.tvTotalSessions))
            .check(matches(isDisplayed()))
            .check(matches(withText("Всього сесій: 0"))) // Перевірка початкового тексту

        // Перевірка тексту сумарного часу
        onView(withId(R.id.tvTotalTime))
            .check(matches(isDisplayed()))
            .check(matches(withText("Сумарний час: 0 хв"))) // Перевірка початкового тексту

        // Перевірка тексту серії
        onView(withId(R.id.tvStreak))
            .check(matches(isDisplayed()))
            .check(matches(withText("Серія: 0 днів"))) // Перевірка початкового тексту
    }

    /**
     * Тест 3: Перевірка кліків по опціям вибору часу
     */
    @Test
    fun testTimeOptionsClickable() {
        // Перевірка, що всі опції часу клікабельні
        onView(withId(R.id.time3))
            .check(matches(isClickable()))
            .perform(click())

        onView(withId(R.id.time5))
            .check(matches(isClickable()))
            .perform(click())

        onView(withId(R.id.time10))
            .check(matches(isClickable()))
            .perform(click())

        onView(withId(R.id.time20))
            .check(matches(isClickable()))
            .perform(click())

        onView(withId(R.id.time30))
            .check(matches(isClickable()))
            .perform(click())
    }

    /**
     * Тест 4: Перевірка спінера для вибору звуку
     */
    @Test
    fun testSoundSpinnerIsInteractive() {
        // Перевірка, що спінер відображається
        onView(withId(R.id.spinnerSound))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))

        // Відкриваємо спінер
        onView(withId(R.id.spinnerSound))
            .perform(click())

        // Чекаємо, поки відкриється dropdown
        Thread.sleep(500)

        // Перевірка, що можна вибрати елемент
        // (вибираємо перший елемент зі списку)
        onData(anything())
            .atPosition(0)
            .perform(click())
    }

    /**
     * Тест 5: Перевірка кнопок контролю таймера
     */
    @Test
    fun testTimerControlButtonsAreClickable() {
        // Перевірка кнопки Start
        onView(withId(R.id.btnStart))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
            .perform(click())

        // Перевірка кнопки Pause (може бути неактивна до старту)
        onView(withId(R.id.btnPause))
            .check(matches(isDisplayed()))

        // Перевірка кнопки Resume
        onView(withId(R.id.btnResume))
            .check(matches(isDisplayed()))

        // Перевірка кнопки Stop
        onView(withId(R.id.btnStop))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    /**
     * Тест 6: Перевірка формату таймера
     */
    @Test
    fun testTimerFormat() {
        // Перевірка, що таймер відображається
        onView(withId(R.id.tvTimer))
            .check(matches(isDisplayed()))
        
        // Перевірка, що текст таймера не порожній
        onView(withId(R.id.tvTimer))
            .check(matches(not(withText(""))))
        
        // Перевірка, що початкове значення 00:00:00
        onView(withId(R.id.tvTimer))
            .check(matches(withText("00:00:00")))
    }

    /**
     * Тест 7: Перевірка опцій часу (всі опції присутні)
     */
    @Test
    fun testAllTimeOptionsPresent() {
        val timeOptions = listOf(
            R.id.time3 to "3 хв",
            R.id.time5 to "5 хв",
            R.id.time10 to "10 хв",
            R.id.time20 to "20 хв",
            R.id.time30 to "30 хв"
        )

        timeOptions.forEach { (id, text) ->
            onView(withId(id))
                .check(matches(isDisplayed()))
                .check(matches(withText(text)))
                .check(matches(isClickable()))
        }
    }

    /**
     * Тест 8: Перевірка Layout для опцій часу
     */
    @Test
    fun testTimeOptionsLayout() {
        // Перевірка, що контейнер для опцій часу існує
        onView(withId(R.id.timeOptionsLayout))
            .check(matches(isDisplayed()))
            .check(matches(hasChildCount(5))) // 5 опцій часу
    }

    /**
     * Тест 9: Перевірка тексту кнопок
     */
    @Test
    fun testButtonTexts() {
        val buttons = mapOf(
            R.id.btnStart to "START",
            R.id.btnPause to "PAUSE",
            R.id.btnResume to "RESUME",
            R.id.btnStop to "STOP"
        )

        buttons.forEach { (id, expectedText) ->
            onView(withId(id))
                .check(matches(withText(expectedText)))
        }
    }

    /**
     * Тест 10: Перевірка, що всі основні елементи в одному View
     */
    @Test
    fun testAllMainElementsInScrollView() {
        // Перевірка, що всі основні елементи відображаються
        // (вони мають бути в ScrollView, тому можна прокрутити)

        // Заголовок
        onView(withId(R.id.tvSessionTitle))
            .check(matches(isDisplayed()))

        // Спінер
        onView(withId(R.id.spinnerSound))
            .check(matches(isDisplayed()))

        // Таймер
        onView(withId(R.id.tvTimer))
            .check(matches(isDisplayed()))

        // Кнопки
        onView(withId(R.id.btnStart))
            .check(matches(isDisplayed()))

        // Статистика
        onView(withId(R.id.tvTotalSessions))
            .check(matches(isDisplayed()))
    }
}