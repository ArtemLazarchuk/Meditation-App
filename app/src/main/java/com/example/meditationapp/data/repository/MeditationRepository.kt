package com.example.meditationapp.data.repository

import com.example.meditationapp.data.MeditationDatabase
import com.example.meditationapp.data.models.MeditationSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MeditationRepository(private val db: MeditationDatabase) {

    private val dao = db.meditationSessionDao()

    suspend fun addSession(durationSeconds: Int, finishedAtMillis: Long) {
        val session = MeditationSession(
            durationSeconds = durationSeconds,
            timestamp = finishedAtMillis
        )
        dao.insert(session)
    }

    val totalDurationSeconds: Flow<Int?> = dao.getTotalDurationSeconds()
    val sessionsCount: Flow<Int> = dao.getSessionsCount()
    val uniqueDatesIso: Flow<List<String>> = dao.getAllUniqueDates()

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
}
