package com.example.meditationapp.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditationapp.data.MeditationDatabase
import com.example.meditationapp.data.repository.MeditationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = MeditationRepository(
        MeditationDatabase.getInstance(application)
    )

    val totalSessions = repo.sessionsCount
    val totalDuration = repo.totalDurationSeconds

    private val _streak = MutableStateFlow(0)
    val streak = _streak.asStateFlow()

    init {
        viewModelScope.launch {
            repo.uniqueDatesIso.collect { dates ->
                _streak.value = repo.calculateStreak(dates)
            }
        }
    }

    fun saveSession(durationSeconds: Int, finishedAtMillis: Long) {
        viewModelScope.launch {
            repo.addSession(durationSeconds, finishedAtMillis)
        }
    }
}
