package com.unitbv.speedy.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.unitbv.speedy.data.RunDatabase
import com.unitbv.speedy.data.RunEntity
import com.unitbv.speedy.service.RunTrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class RunTrackingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = RunDatabase.getInstance(application)
    private val fusedClient = LocationServices.getFusedLocationProviderClient(application)

    // Timer vine din Service
    private val _seconds = MutableStateFlow(0)
    val seconds = _seconds.asStateFlow()

    private val _isRunning = MutableStateFlow(true)
    val isRunning = _isRunning.asStateFlow()

    private val _distanceMeters = MutableStateFlow(0f)
    val distanceMeters = _distanceMeters.asStateFlow()

    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private val _routePoints = MutableStateFlow<List<GeoPoint>>(emptyList())
    val routePoints = _routePoints.asStateFlow()

    private var lastLocation: Location? = null
    private var runType: String = "Easy"
    private var boundService: RunTrackingService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RunTrackingService.RunBinder
            boundService = binder.getService()
            isBound = true

            // Sincronizează seconds și isRunning din Service
            viewModelScope.launch {
                binder.getService().seconds.collect { secs ->
                    _seconds.value = secs
                    // Actualizează notificarea cu distanța curentă
                    boundService?.updateNotification(secs, _distanceMeters.value / 1000f)
                }
            }
            viewModelScope.launch {
                binder.getService().isRunning.collect { running ->
                    _isRunning.value = running
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            boundService = null
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val newLocation = result.lastLocation ?: return
            val geoPoint = GeoPoint(newLocation.latitude, newLocation.longitude)

            _currentLocation.value = geoPoint
            _routePoints.value = _routePoints.value + geoPoint

            lastLocation?.let { prev ->
                if (_isRunning.value) {
                    _distanceMeters.value += prev.distanceTo(newLocation)
                }
            }
            lastLocation = newLocation
        }
    }

    init {
        startService()
        startLocationUpdates()
    }

    private fun startService() {
        val context = getApplication<Application>()
        val intent = Intent(context, RunTrackingService::class.java)
        context.startForegroundService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 2000L
        ).setMinUpdateDistanceMeters(5f).build()

        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    fun togglePause() {
        boundService?.togglePause()
    }

    fun setRunType(type: String) {
        runType = type
    }

    fun finishRun(onDone: () -> Unit) {
        viewModelScope.launch {
            val dist = _distanceMeters.value
            val secs = _seconds.value.toLong()
            val cal = (dist / 1000f * 70).toInt()

            if (dist > 10f) {
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
            stopServiceAndCleanup()
            onDone()
        }
    }

    private fun stopServiceAndCleanup() {
        fusedClient.removeLocationUpdates(locationCallback)
        val context = getApplication<Application>()
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
        context.stopService(Intent(context, RunTrackingService::class.java))
    }

    override fun onCleared() {
        super.onCleared()
        fusedClient.removeLocationUpdates(locationCallback)
        val context = getApplication<Application>()
        if (isBound) {
            context.unbindService(serviceConnection)
        }
    }
}