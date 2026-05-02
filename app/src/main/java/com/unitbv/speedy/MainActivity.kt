package com.unitbv.speedy

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.unitbv.speedy.screens.*
import com.unitbv.speedy.ui.theme.SpeedyTheme
import org.osmdroid.config.Configuration
import java.io.File

class MainActivity : ComponentActivity() {

    // Instanța Firebase Auth pentru Backend
    private lateinit var auth: FirebaseAuth

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (!fineLocationGranted) {
            Toast.makeText(this, "Aplicația are nevoie de GPS pentru tracking!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inițializare Firebase
        auth = FirebaseAuth.getInstance()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        Configuration.getInstance().apply {
            load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
            userAgentValue = packageName
            osmdroidTileCache = File(cacheDir, "osmdroid")
        }

        requestPermissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        setContent {
            SpeedyTheme {
                // Pasăm instanța auth către navigație
                AppNavigation(auth)
            }
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Backend Logic: Dacă user-ul e deja logat, mergem direct la Main
    val startDestination = if (auth.currentUser != null) "main" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onLoginClick = { email, password ->
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    Toast.makeText(context, "Eroare Login: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Completează toate câmpurile!", Toast.LENGTH_SHORT).show()
                    }
                },
                onSignUpClick = { navController.navigate("signup") }
            )
        }

        composable("signup") {
            SignUpScreen(
                onSignUpClick = { name, email, password ->
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navController.navigate("main") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                } else {
                                    Toast.makeText(context, "Eroare Sign Up: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                },
                onLoginClick = { navController.popBackStack() }
            )
        }

        composable("main") {
            MainScreen(
                onRunClick = { navController.navigate("run_setup") },
                onHistoryClick = { navController.navigate("history") },
                onProfileClick = { navController.navigate("profile") }
            )
        }

        composable("run_setup") {
            RunSetupScreen(
                onStartRun = { distance, type ->
                    navController.navigate("run_tracking/$distance/$type")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "run_tracking/{distance}/{type}",
            arguments = listOf(
                navArgument("distance") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dist = backStackEntry.arguments?.getString("distance") ?: "5 km"
            val type = backStackEntry.arguments?.getString("type") ?: "Easy"
            RunTrackingScreen(
                targetDistance = dist,
                runType = type,
                onFinish = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = false }
                    }
                }
            )
        }

        composable("history") {
            HistoryScreen(onBack = { navController.popBackStack() })
        }

        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}