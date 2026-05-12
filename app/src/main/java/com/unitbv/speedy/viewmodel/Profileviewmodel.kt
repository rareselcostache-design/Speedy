package com.unitbv.speedy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.unitbv.speedy.data.RunDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileStats(
    val totalKm: Float = 0f,
    val totalRuns: Int = 0,
    val totalCal: Int = 0,
    val bestPace: String = "--:--"
)

data class NotificationSettings(
    val reminders: Boolean = true,
    val weekly: Boolean = true,
    val achievements: Boolean = false,
    val tips: Boolean = false
)

data class UnitSettings(
    val distance: String = "km",
    val language: String = "EN"
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val db = RunDatabase.getInstance(application)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _stats = MutableStateFlow(ProfileStats())
    val stats = _stats.asStateFlow()

    private val _displayName = MutableStateFlow("Runner")
    val displayName = _displayName.asStateFlow()

    private val _initials = MutableStateFlow("R")
    val initials = _initials.asStateFlow()

    private val _notifications = MutableStateFlow(NotificationSettings())
    val notifications = _notifications.asStateFlow()

    private val _goals = MutableStateFlow("intermediate")
    val goals = _goals.asStateFlow()

    private val _units = MutableStateFlow(UnitSettings())
    val units = _units.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    init {
        loadProfile()
        loadStats()
        loadUserSettings()
    }

    private fun loadProfile() {
        val auth = FirebaseAuth.getInstance()

        auth.currentUser?.let { user ->
            setNameState(
                user.displayName?.takeIf { it.isNotBlank() }
                    ?: user.email?.substringBefore("@")
                    ?: "Runner"
            )
            return
        }

        auth.addAuthStateListener(object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
                val user = firebaseAuth.currentUser ?: return
                setNameState(
                    user.displayName?.takeIf { it.isNotBlank() }
                        ?: user.email?.substringBefore("@")
                        ?: "Runner"
                )
                firebaseAuth.removeAuthStateListener(this)
            }
        })
    }

    private fun setNameState(name: String) {
        _displayName.value = name
        _initials.value = name.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .take(2)
            .joinToString("")
            .ifEmpty { "R" }
    }

    private fun loadUserSettings() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val notifMap = snapshot.get("notifications") as? Map<*, *>
                if (notifMap != null) {
                    _notifications.value = NotificationSettings(
                        reminders = notifMap["reminders"] as? Boolean ?: true,
                        weekly = notifMap["weekly"] as? Boolean ?: true,
                        achievements = notifMap["achievements"] as? Boolean ?: false,
                        tips = notifMap["tips"] as? Boolean ?: false
                    )
                }

                val goalsVal = snapshot.getString("goals")
                if (goalsVal != null) _goals.value = goalsVal

                val unitsMap = snapshot.get("units") as? Map<*, *>
                if (unitsMap != null) {
                    _units.value = UnitSettings(
                        distance = unitsMap["distance"] as? String ?: "km",
                        language = unitsMap["language"] as? String ?: "EN"
                    )
                }
            }
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

    fun updateDisplayName(newName: String) {
        val user = auth.currentUser ?: return
        val uid = user.uid
        viewModelScope.launch {
            _isSaving.value = true
            try {
                // Actualizează Firebase Auth
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()
                user.updateProfile(profileUpdates).await()

                // Actualizează Firestore
                firestore.collection("users").document(uid)
                    .set(mapOf("displayName" to newName), com.google.firebase.firestore.SetOptions.merge())
                    .await()

                setNameState(newName)
            } catch (e: Exception) {
                // poți expune eroarea dacă vrei
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun updateNotifications(newSettings: NotificationSettings) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestore.collection("users").document(uid)
                .set(
                    mapOf("notifications" to mapOf(
                        "reminders" to newSettings.reminders,
                        "weekly" to newSettings.weekly,
                        "achievements" to newSettings.achievements,
                        "tips" to newSettings.tips
                    )),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()
            _notifications.value = newSettings
        }
    }

    fun updateGoals(goal: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestore.collection("users").document(uid)
                .set(mapOf("goals" to goal), com.google.firebase.firestore.SetOptions.merge())
                .await()
            _goals.value = goal
        }
    }

    fun updateUnits(newUnits: UnitSettings) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestore.collection("users").document(uid)
                .set(
                    mapOf("units" to mapOf(
                        "distance" to newUnits.distance,
                        "language" to newUnits.language
                    )),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()
            _units.value = newUnits
        }
    }
}