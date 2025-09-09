package com.example.triqui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.triqui.ui.theme.TriquiTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TriquiTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    TriquiApp()
                }
            }
        }
    }
}

@Composable
fun TriquiApp() {
    var juego by remember { mutableStateOf(TriquiJuego()) }
    var tablero by remember { mutableStateOf(juego.tablero) }
    var textoEstado by remember { mutableStateOf("Tu turno") }
    var juegoTerminado by remember { mutableStateOf(false) }

    var victoriasJugador by remember { mutableStateOf(0) }
    var victoriasComputador by remember { mutableStateOf(0) }
    var empates by remember { mutableStateOf(0) }

    var empiezaJugador by remember { mutableStateOf(true) }

    // Turno de android
    var turnoAndroid by remember { mutableStateOf(false) }

    fun reiniciarJuego() {
        juego = TriquiJuego()
        tablero = juego.tablero
        juegoTerminado = false
        empiezaJugador = !empiezaJugador

        if (empiezaJugador) {
            textoEstado = "Tu turno"
        } else {
            textoEstado = "Turno de Android"
            turnoAndroid = true
        }
    }

    LaunchedEffect(turnoAndroid) {
        if (turnoAndroid && !juegoTerminado) {
            delay(800) // esperar pa poner la jugada
            val jugada = juego.jugadaComputador()
            juego.ponerJugada(TriquiJuego.COMPUTADOR, jugada)
            tablero = juego.tablero
            val ganador = juego.verificarGanador()
            when (ganador) {
                0 -> textoEstado = "Tu turno"
                1 -> {
                    textoEstado = "¡Empate!"
                    empates++
                    juegoTerminado = true
                }
                2 -> {
                    textoEstado = "¡Ganaste!"
                    victoriasJugador++
                    juegoTerminado = true
                }
                3 -> {
                    textoEstado = "Android ganó"
                    victoriasComputador++
                    juegoTerminado = true
                }
            }
            turnoAndroid = false // mira de quien es el turno
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("TRIQUI", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(16.dp))

        // Tablero 3x3
        for (fila in 0..2) {
            Row {
                for (col in 0..2) {
                    val indice = fila * 3 + col
                    Button(
                        onClick = {
                            if (!juegoTerminado && juego.ponerJugada(TriquiJuego.JUGADOR, indice)) {
                                tablero = juego.tablero
                                var ganador = juego.verificarGanador()
                                if (ganador == 0) {
                                    textoEstado = "Turno de Android"
                                    turnoAndroid = true
                                } else {
                                    when (ganador) {
                                        1 -> {
                                            textoEstado = "¡Empate!"
                                            empates++
                                            juegoTerminado = true
                                        }
                                        2 -> {
                                            textoEstado = "¡Ganaste!"
                                            victoriasJugador++
                                            juegoTerminado = true
                                        }
                                        3 -> {
                                            textoEstado = "Android ganó"
                                            victoriasComputador++
                                            juegoTerminado = true
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !juegoTerminado && tablero[indice] == TriquiJuego.VACIO,
                        modifier = Modifier.size(100.dp).padding(4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (tablero[indice] == TriquiJuego.VACIO) "" else tablero[indice].toString(),
                            fontSize = 32.sp,
                            color = when (tablero[indice]) {
                                TriquiJuego.JUGADOR -> Color.Green
                                TriquiJuego.COMPUTADOR -> Color.Red
                                else -> Color.Unspecified
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(textoEstado, fontSize = 20.sp)

        Spacer(Modifier.height(16.dp))

        // Marcador
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Text("Jugador: $victoriasJugador", fontSize = 18.sp)
            Text("Android: $victoriasComputador", fontSize = 18.sp)
            Text("Empates: $empates", fontSize = 18.sp)
        }

        Spacer(Modifier.height(16.dp))

        // Boton de iniciar el jeugo
        Button(onClick = { reiniciarJuego() }) {
            Text("Nuevo Juego")
        }
    }
}

