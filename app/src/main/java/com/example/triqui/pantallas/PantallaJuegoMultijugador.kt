// PantallaJuegoMultijugador.kt

package com.example.triqui.pantallas

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.triqui.firebase.FirebaseManager
import com.example.triqui.firebase.Partida
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
// ✅ Importaciones de componentes compartidos
import com.example.triqui.pantallas.TableroTriquiReutilizable
import com.example.triqui.pantallas.verificarGanadorInt
import com.example.triqui.pantallas.reproducirSonidoMovimiento

@Composable
fun PantallaJuegoMultijugador(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: "anon"
    val context = LocalContext.current // Contexto para sonidos

    val firebaseManager = remember { FirebaseManager() }

    var partidaId by remember { mutableStateOf<String?>(null) }
    var partida by remember { mutableStateOf(Partida()) }
    var esperandoOponente by remember { mutableStateOf(true) }
    var listenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }

    // Determinar qué símbolo representa este jugador (1: X o 2: O)
    val simboloJugadorLocal = remember(partida.jugador1, uid) {
        if (uid == partida.jugador1) 1 else 2
    }

    // 🔹 Inicialización: Obtener ID de Navegación y empezar a escuchar
    LaunchedEffect(Unit) {
        val idPasado = navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<String>("partidaId")

        if (idPasado != null) {
            partidaId = idPasado

            listenerRegistration = firebaseManager.escucharPartida(idPasado) {
                partida = it
                // El juego empieza cuando jugador2 no está vacío
                esperandoOponente = it.jugador2.isEmpty()
            }
        }
    }

    // 🔹 Limpieza del Listener al salir de la pantalla
    DisposableEffect(key1 = Unit) {
        onDispose {
            listenerRegistration?.remove()
        }
    }

    // ✅ Acción: Reiniciar ronda (mantiene puntajes)
    fun reiniciarRonda() {
        partidaId?.let {
            // Reiniciar el tablero, turno (siempre J1) y estado
            firebaseManager.actualizarEstadoDeJuego(
                partidaId = it,
                tablero = List(9) { 0 }, // Tablero vacío
                turno = 1, // Siempre inicia el Jugador 1
                estado = "en_juego", // Vuelve al estado de juego
                winner = 0, // Reiniciar ganador
                victoriasJ1 = partida.victoriasJugador1,
                victoriasJ2 = partida.victoriasJugador2
            )
        }
    }

    // 🔹 Acción: marcar casilla y actualizar puntaje
    fun jugar(pos: Int) {
        val miNumero = simboloJugadorLocal
        val esMiTurno = partida.turno == miNumero

        // Comprobación de reglas
        if (!esMiTurno || partida.estado != "en_juego" || partida.tablero[pos] != 0) return

        // Simulación de movimiento
        val nuevoTablero = partida.tablero.toMutableList()
        nuevoTablero[pos] = miNumero

        // ✅ Reproducir el sonido propio
        reproducirSonidoMovimiento(context, miNumero, miNumero)

        val ganador = verificarGanadorInt(nuevoTablero)
        val terminado = ganador != 0 || nuevoTablero.none { it == 0 }

        // Determinar siguiente estado y turno
        val siguienteTurno = if (miNumero == 1) 2 else 1

        val nuevoEstado = when {
            ganador != 0 -> "terminado"
            terminado -> "empate"
            else -> "en_juego"
        }

        // El turno no debe cambiar si el juego terminó
        val turnoFinal = if (terminado) partida.turno else siguienteTurno

        // 5. Actualización de puntaje y ganador
        var newWinner = 0
        var newVictoriasJ1 = partida.victoriasJugador1
        var newVictoriasJ2 = partida.victoriasJugador2

        if (ganador != 0) {
            newWinner = ganador // 1 o 2
            if (ganador == 1) newVictoriasJ1++
            if (ganador == 2) newVictoriasJ2++
        }

        // 6. Actualizar Firebase
        partidaId?.let {
            firebaseManager.actualizarEstadoDeJuego(
                partidaId = it,
                tablero = nuevoTablero,
                turno = turnoFinal,
                estado = nuevoEstado,
                winner = newWinner,
                victoriasJ1 = newVictoriasJ1,
                victoriasJ2 = newVictoriasJ2
            )
        }
    }

    // 🔹 Interfaz principal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Triqui Multijugador",
            color = Color.White,
            fontSize = 26.sp
        )

        Spacer(Modifier.height(20.dp))

        if (esperandoOponente) {
            Text(
                "Esperando a que otro jugador se una... ID Partida: ${partidaId ?: "Cargando..."}",
                color = Color.Yellow,
                fontSize = 18.sp
            )
        } else {
            // Mostrar estado del juego
            Text(
                text = when {
                    partida.winner != 0 -> "¡Ganó Jugador ${partida.winner}!"
                    partida.estado == "empate" -> "¡Empate!"
                    partida.estado == "terminado" -> "Ronda Terminada"
                    partida.estado == "en_juego" -> {
                        val miNumero = simboloJugadorLocal
                        val turnoEsMiNumero = partida.turno == miNumero

                        if (turnoEsMiNumero) "¡Es tu turno (${if (miNumero == 1) "X" else "O"})!"
                        else "Turno del oponente"
                    }
                    else -> "Estado desconocido"
                },
                color = Color.White,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(12.dp))

            // ✅ Llama al componente reutilizable del tablero
            TableroTriquiReutilizable(
                tablero = partida.tablero,
                simboloJugadorLocal = simboloJugadorLocal,
                onCasillaClick = ::jugar
            )

            Spacer(Modifier.height(24.dp))

            // ✅ Mostrar Puntajes (Scoreboard)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val esJugador1 = uid == partida.jugador1
                val miSimbolo = if (esJugador1) "X" else "O"
                val miPuntaje = if (esJugador1) partida.victoriasJugador1 else partida.victoriasJugador2
                val rivalPuntaje = if (esJugador1) partida.victoriasJugador2 else partida.victoriasJugador1

                Text("Yo ($miSimbolo): $miPuntaje", color = Color.Cyan, fontSize = 16.sp)
                Text("Rival: $rivalPuntaje", color = Color.Red, fontSize = 16.sp)
            }

            Spacer(Modifier.height(24.dp))

            // Botón de Reinicio (solo si el juego terminó o hay empate)
            if (partida.estado != "en_juego" && !esperandoOponente) {
                Button(
                    onClick = { reiniciarRonda() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("Jugar de Nuevo", color = Color.White)
                }
                Spacer(Modifier.height(12.dp))
            }

            // Botón de Volver al inicio
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Volver al inicio", color = Color.White)
            }
        }
    }
}