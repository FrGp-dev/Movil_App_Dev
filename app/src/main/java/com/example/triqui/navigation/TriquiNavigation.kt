package com.example.triqui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.triqui.pantallas.PantallaInicio
import com.example.triqui.pantallas.PantallaJuego

object Routes {
    const val Inicio = "inicio"
    const val Juego = "juego"
}

@Composable
fun TriquiNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Inicio
    ) {
        composable(Routes.Inicio) {
            PantallaInicio(navController = navController)
        }
        composable(Routes.Juego) {
            PantallaJuego(navController = navController)
        }
    }
}
