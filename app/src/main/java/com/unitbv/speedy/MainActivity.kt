package com.unitbv.speedy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unitbv.speedy.screens.HistoryScreen
import com.unitbv.speedy.screens.LoginScreen
import com.unitbv.speedy.screens.MainScreen
import com.unitbv.speedy.screens.ProfileScreen
import com.unitbv.speedy.screens.RunSetupScreen
import com.unitbv.speedy.screens.RunTrackingScreen
import com.unitbv.speedy.screens.SignUpScreen
import com.unitbv.speedy.ui.theme.SpeedyTheme
import org.osmdroid.config.Configuration
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        Configuration.getInstance().apply {
            load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
            userAgentValue = packageName
            osmdroidTileCache = File(cacheDir, "osmdroid")
            tileFileSystemCacheTrimBytes = 100L * 1024 * 1024
            tileFileSystemCacheMaxBytes = 200L * 1024 * 1024
            tileDownloadThreads = 4
            tileFileSystemThreads = 4
        }

        setContent {
            SpeedyTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginClick = { _, _ -> navController.navigate("main") },
                onSignUpClick = { navController.navigate("signup") }
            )
        }
        composable("main") {
            MainScreen(
                onRunClick = { navController.navigate("run_setup") },
                onHistoryClick = { navController.navigate("history") },
                onProfileClick = { navController.navigate("profile") }
            )
        }
        composable("signup") {
            SignUpScreen(
                onSignUpClick = { _, _, _ -> navController.navigate("main") },
                onLoginClick = { navController.popBackStack() }
            )
        }
        composable("run_setup") {
            RunSetupScreen(
                onStartRun = { navController.navigate("run_tracking") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("run_tracking") {
            RunTrackingScreen(
                onFinish = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = false }
                    }
                }
            )
        }
        composable("history") {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("history") {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }


    }
}