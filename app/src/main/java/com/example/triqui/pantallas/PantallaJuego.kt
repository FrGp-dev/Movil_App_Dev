package com.example.triqui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.triqui.TriquiJuego
import com.example.triqui.navigation.Routes
import kotlinx.coroutines.delay

@Composable
fun PantallaJuego(navController: NavHostController) {
    val savedStateHandle = navController.previousBackStackEntry?.savedStateHandle
    val empiezaJugador = savedStateHandle?.get<Boolean>("empiezaJugador") ?: true
    val dificultad = savedStateHandle?.get<String>("dificultad") ?: "Fácil"

    var juego by remember { mutableStateOf(TriquiJuego()) }
    var tablero by remember { mutableStateOf(juego.getTablero()) }
    var textoEstado by remember { mutableStateOf(if (empiezaJugador) "Tu turno" else "Turno de Android") }
    var juegoTerminado by remember { mutableStateOf(false) }

    var victoriasJugador by remember { mutableStateOf(0) }
    var victoriasComputador by remember { mutableStateOf(0) }
    var empates by remember { mutableStateOf(0) }


    var turnoAndroid by remember { mutableStateOf(!empiezaJugador) }

    fun reiniciarJuego() {
        juego = TriquiJuego()
        tablero = juego.getTablero()
        juegoTerminado = false
        turnoAndroid = !empiezaJugador
        textoEstado = if (empiezaJugador) "Tu turno" else "Turno de Android"
    }

    LaunchedEffect(turnoAndroid) {
        if (turnoAndroid && !juegoTerminado) {
            delay(700)
            val jugada = juego.jugadaComputador(dificultad)
            juego.ponerJugada(TriquiJuego.COMPUTADOR, jugada)
            tablero = juego.getTablero()
            when (juego.verificarGanador()) {
                0 -> textoEstado = "Tu turno"
                1 -> { textoEstado = "¡Empate!"; empates++; juegoTerminado = true }
                2 -> { textoEstado = "¡Ganaste!"; victoriasJugador++; juegoTerminado = true }
                3 -> { textoEstado = "Android ganó"; victoriasComputador++; juegoTerminado = true }
            }
            turnoAndroid = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("TRIQUI", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(12.dp))

        // Tablero
        for (fila in 0..2) {
            Row {
                for (col in 0..2) {
                    val indice = fila * 3 + col
                    Button(
                        onClick = {
                            if (!juegoTerminado && juego.ponerJugada(TriquiJuego.JUGADOR, indice)) {
                                tablero = juego.getTablero()
                                when (juego.verificarGanador()) {
                                    0 -> { textoEstado = "Turno de Android"; turnoAndroid = true }
                                    1 -> { textoEstado = "¡Empate!"; empates++; juegoTerminado = true }
                                    2 -> { textoEstado = "¡Ganaste!"; victoriasJugador++; juegoTerminado = true }
                                    3 -> { textoEstado = "Android ganó"; victoriasComputador++; juegoTerminado = true }
                                }
                            }
                        },
                        enabled = !juegoTerminado && tablero[indice] == TriquiJuego.VACIO,
                        modifier = Modifier.size(100.dp).padding(4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (tablero[indice] == TriquiJuego.VACIO) "" else tablero[indice].toString(),
                            fontSize = 32.sp,
                            color = when (tablero[indice]) {
                                TriquiJuego.JUGADOR -> Color(0xFF2E7D32)
                                TriquiJuego.COMPUTADOR -> Color(0xFFC62828)
                                else -> Color.Unspecified
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(textoEstado, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Text("Jugador: $victoriasJugador")
            Text("Android: $victoriasComputador")
            Text("Empates: $empates")
        }

        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { reiniciarJuego() }) {
                Text("Nuevo Juego")
            }
            OutlinedButton(onClick = {
                navController.popBackStack(
                    Routes.Inicio,
                    inclusive = false
                )
            }) {
                Text("Volver al Inicio")
            }

        }
    }
}
