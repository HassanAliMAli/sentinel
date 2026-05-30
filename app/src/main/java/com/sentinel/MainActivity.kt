package com.sentinel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sentinel.ui.dashboard.DashboardScreen
import com.sentinel.ui.theme.SentinelTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SentinelTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SentinelNavigation()
                }
            }
        }
    }
}

@Composable
fun SentinelNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController, 
        startDestination = "dashboard",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("onboarding") { /* TODO: Implement OnboardingScreen */ }
        composable("dashboard") { DashboardScreen() }
        composable("settings") { /* TODO: Implement SettingsScreen */ }
        composable("logs") { /* TODO: Implement LogViewerScreen */ }
    }
}
