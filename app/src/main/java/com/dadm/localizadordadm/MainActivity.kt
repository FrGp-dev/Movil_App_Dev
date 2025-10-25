package com.dadm.localizadordadm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dadm.localizadordadm.Interface.MapScreen
import com.dadm.localizadordadm.Interface.SettingsScreen
import com.dadm.localizadordadm.ui.theme.LocalizadorDADMTheme
import com.dadm.localizadordadm.viewmodel.MapViewModel
import org.osmdroid.config.Configuration
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        Configuration.getInstance().load(
            applicationContext,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        val cacheDir = File(applicationContext.filesDir, "osmdroid/tiles")
        cacheDir.mkdirs()


        Configuration.getInstance().osmdroidTileCache = cacheDir

        setContent {
            LocalizadorDADMTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Map : Screen("map")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Instancia única del ViewModel compartida entre pantallas
    val mapViewModel: MapViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Map.route) {
        composable(Screen.Map.route) {
            MapScreen(
                viewModel = mapViewModel,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = mapViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}