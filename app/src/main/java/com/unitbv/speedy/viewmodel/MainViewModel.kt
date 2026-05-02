package com.unitbv.speedy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.unitbv.speedy.data.RunDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeStats(
    val totalKm: Float = 0f,
    val totalCal: Int = 0,
    val totalRuns: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = RunDatabase.getInstance(application)

    private val _stats = MutableStateFlow(HomeStats())
    val stats = _stats.asStateFlow()

    private val _userName = MutableStateFlow("there")
    val userName = _userName.asStateFlow()

    init {
        loadStats()
        loadUserName()
    }

    fun loadStats() {
        viewModelScope.launch {
            val runs = db.runDao().getAllRuns()
            _stats.value = HomeStats(
                totalKm = runs.sumOf { it.distanceMeters.toDouble() }.toFloat() / 1000f,
                totalCal = runs.sumOf { it.caloriesBurned },
                totalRuns = runs.size
            )
        }
    }

    private fun loadUserName() {
        val user = FirebaseAuth.getInstance().currentUser
        _userName.value = user?.displayName?.split(" ")?.firstOrNull()
            ?: user?.email?.substringBefore("@")
                    ?: "there"
    }
}