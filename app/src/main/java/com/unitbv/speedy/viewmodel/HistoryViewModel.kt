package com.unitbv.speedy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unitbv.speedy.data.RunDatabase
import com.unitbv.speedy.data.RunEntity
import com.unitbv.speedy.screens.RunEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = RunDatabase.getInstance(application)

    private val _runs = MutableStateFlow<List<RunEntity>>(emptyList())
    val runs = _runs.asStateFlow()

    init {
        viewModelScope.launch {
            _runs.value = db.runDao().getAllRuns()
        }
    }
}

fun RunEntity.toDisplayEntry(): RunEntry {
    val sdf = SimpleDateFormat("EEE, HH:mm", Locale.getDefault())
    val date = sdf.format(Date(dateMillis))
    val km = distanceMeters / 1000f
    val min = durationSeconds / 60
    val sec = durationSeconds % 60
    val paceMin = if (km > 0) (durationSeconds / 60f / km).toInt() else 0
    val paceSec = if (km > 0) ((durationSeconds / 60f / km - paceMin) * 60).toInt() else 0

    return RunEntry(
        id = id,
        date = date,
        distance = "%.1f km".format(km),
        duration = "%d:%02d".format(min, sec),
        pace = "%d:%02d/km".format(paceMin, paceSec),
        calories = caloriesBurned,
        type = runType
    )
}