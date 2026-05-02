package com.unitbv.speedy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.unitbv.speedy.data.RunDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileStats(
    val totalKm: Float = 0f,
    val totalRuns: Int = 0,
    val totalCal: Int = 0,
    val bestPace: String = "--:--"
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val db = RunDatabase.getInstance(application)

    private val _stats = MutableStateFlow(ProfileStats())
    val stats = _stats.asStateFlow()

    private val _displayName = MutableStateFlow("Runner")
    val displayName = _displayName.asStateFlow()

    private val _initials = MutableStateFlow("R")
    val initials = _initials.asStateFlow()

    init {
        loadProfile()
        loadStats()
    }

    private fun loadProfile() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val name = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@") ?: "Runner"
        _displayName.value = name
        _initials.value = name.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .take(2)
            .joinToString("")
            .ifEmpty { "R" }
    }

    private fun loadStats() {
        viewModelScope.launch {
            val runs = db.runDao().getAllRuns()
            if (runs.isEmpty()) return@launch

            val bestPaceSeconds = runs
                .filter { it.distanceMeters > 100f }
                .minOfOrNull { it.durationSeconds.toFloat() / (it.distanceMeters / 1000f) }

            val bestPaceStr = if (bestPaceSeconds != null) {
                val min = (bestPaceSeconds / 60).toInt()
                val sec = (bestPaceSeconds % 60).toInt()
                "%d:%02d".format(min, sec)
            } else "--:--"

            _stats.value = ProfileStats(
                totalKm = runs.sumOf { it.distanceMeters.toDouble() }.toFloat() / 1000f,
                totalRuns = runs.size,
                totalCal = runs.sumOf { it.caloriesBurned },
                bestPace = bestPaceStr
            )
        }
    }
}