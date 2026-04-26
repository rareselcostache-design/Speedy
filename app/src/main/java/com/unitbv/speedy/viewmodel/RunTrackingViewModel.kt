package com.unitbv.speedy.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.unitbv.speedy.data.RunDatabase
import com.unitbv.speedy.data.RunEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class RunTrackingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = RunDatabase.getInstance(application)
    private val fusedClient = LocationServices.getFusedLocationProviderClient(application)

    private val _seconds = MutableStateFlow(0)
    val seconds = _seconds.asStateFlow()

    private val _distanceMeters = MutableStateFlow(0f)
    val distanceMeters = _distanceMeters.asStateFlow()

    private val _isRunning = MutableStateFlow(true)
    val isRunning = _isRunning.asStateFlow()

    private var lastLocation: Location? = null
    private var runType: String = "Easy"

    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    val currentLocation = _currentLocation.asStateFlow()
    private val _routePoints = MutableStateFlow<List<GeoPoint>>(emptyList())
    val routePoints = _routePoints.asStateFlow()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val newLocation = result.lastLocation ?: return
            val geoPoint = GeoPoint(newLocation.latitude, newLocation.longitude)

            _currentLocation.value = geoPoint
            _routePoints.value = _routePoints.value + geoPoint  // adaugă punctul la traseu

            lastLocation?.let { prev ->
                if (_isRunning.value) {
                    _distanceMeters.value += prev.distanceTo(newLocation)
                }
            }
            lastLocation = newLocation
        }
    }


    init {
        startTimer()
        startLocationUpdates()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_isRunning.value) _seconds.value++
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 2000L
        ).setMinUpdateDistanceMeters(5f).build()

        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    fun togglePause() {
        _isRunning.value = !_isRunning.value
    }

    fun setRunType(type: String) { runType = type }

    fun finishRun(onDone: () -> Unit) {
        viewModelScope.launch {
            val dist = _distanceMeters.value
            val secs = _seconds.value.toLong()
            val cal = (dist / 1000f * 70).toInt()

            if (dist > 10f) { // salvăm doar dacă s-a alergat ceva real
                db.runDao().insertRun(
                    RunEntity(
                        dateMillis = System.currentTimeMillis(),
                        distanceMeters = dist,
                        durationSeconds = secs,
                        runType = runType,
                        caloriesBurned = cal
                    )
                )
            }
            fusedClient.removeLocationUpdates(locationCallback)
            onDone()
        }
    }

    override fun onCleared() {
        super.onCleared()
        fusedClient.removeLocationUpdates(locationCallback)
    }
}