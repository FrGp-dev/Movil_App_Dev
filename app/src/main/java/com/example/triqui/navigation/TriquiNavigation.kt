package com.example.triqui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.triqui.pantallas.PantallaInicio
import com.example.triqui.pantallas.PantallaJuego
import com.example.triqui.pantallas.PantallaJuegoMultijugador

object Routes {
    const val Inicio = "inicio"
    const val Juego = "juego"
    const val JuegoMultijugador = "juego_multijugador"
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

        // ✅ RUTA CORREGIDA: Define los argumentos "inicia" y "dificultad"
        composable(
            route = "${Routes.Juego}/{inicia}/{dificultad}",
            arguments = listOf(
                navArgument("inicia") { type = NavType.StringType },
                navArgument("dificultad") { type = NavType.StringType }
            )
        ) {
            PantallaJuego(navController = navController)
        }

        composable(Routes.JuegoMultijugador) {
            PantallaJuegoMultijugador(navController = navController)
        }
    }
}