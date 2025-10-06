package com.example.triqui.pantallas

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.triqui.R
import com.example.triqui.firebase.FirebaseManager
import com.example.triqui.firebase.Partida
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration


// Constantes de compatibilidad con TableroTriqui y Firebase
private const val JUGADOR_X = 1 // Simbolo Jugador 1 en Firebase
private const val JUGADOR_O = 2 // Simbolo Jugador 2 en Firebase

// --- Funciones Utilitarias (Sin cambios) ---
fun verificarGanadorInt(tablero: List<Int>): Int {
    val lineasGanadoras = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
        listOf(0, 4, 8), listOf(2, 4, 6)
    )
    for (linea in lineasGanadoras) {
        val (a, b, c) = linea
        if (tablero[a] != 0 && tablero[a] == tablero[b] && tablero[a] == tablero[c]) {
            return tablero[a]
        }
    }
    return 0
}

fun reproducirSonidoMovimiento(context: Context, simboloJugado: Int) {
    val sonidoId = if (simboloJugado == JUGADOR_X) R.raw.sonido_pup else R.raw.sonido_bip
    val mp = MediaPlayer.create(context, sonidoId)
    if (mp.isPlaying) mp.seekTo(0)
    mp.start()
    mp.setOnCompletionListener { it.release() }
}
// -----------------------------------------------------------------


@Composable
fun PantallaJuegoMultijugador(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: "anon"
    val context = LocalContext.current
    val density = LocalDensity.current

    val firebaseManager = remember { FirebaseManager() }

    var partidaId by remember { mutableStateOf<String?>(null) }
    var partida by remember { mutableStateOf(Partida()) }
    var esperandoOponente by remember { mutableStateOf(true) }
    var listenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }

    // Estado clave para que el JUGADOR RESTANTE navegue fuera
    var partidaEliminada by remember { mutableStateOf(false) }

    val xImage = painterResource(id = R.drawable.x_jugador)
    val oImage = painterResource(id = R.drawable.o_enemigo)

    val simboloJugadorLocal = remember(partida.jugador1, uid) {
        if (uid == partida.jugador1) JUGADOR_X else JUGADOR_O
    }

    val juegoTerminado = partida.estado != "en_juego" && partida.estado != "esperando"
    val esMiTurno = partida.turno == simboloJugadorLocal

    // 🔹 Lógica de Desconexión / Abandono
    val manejarSalida: () -> Unit = {
        val id = partidaId

        // ✅ CORRECCIÓN CRÍTICA: Remover el listener ANTES de eliminar y ANTES de navegar
        listenerRegistration?.remove()
        listenerRegistration = null

        if (id != null) {
            // Eliminar la partida de Firestore. Esto es robusto para forzar la salida del rival.
            firebaseManager.eliminarPartida(id)
        }

        // Navegamos directamente al salir por el botón
        navController.popBackStack()
    }


    // 🔹 Inicialización y Listener
    LaunchedEffect(Unit) {
        val idPasado = navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<String>("partidaId")

        if (idPasado != null) {
            partidaId = idPasado

            listenerRegistration = firebaseManager.escucharPartida(idPasado) { part ->

                // Si el listener recibe el estado "eliminada" (por borrado de documento)
                if (part.estado == "eliminada") {
                    partidaEliminada = true // Activa el LaunchedEffect de navegación
                    // No hacemos return aquí, el listener se removerá al salir
                }

                partida = part
                esperandoOponente = part.jugador2.isEmpty()
            }
        }
    }

    // ✅ LaunchedEffect: Ejecuta la navegación cuando la bandera es activada por el listener
    LaunchedEffect(partidaEliminada) {
        if (partidaEliminada) {
            // Aseguramos que el listener esté inactivo antes de navegar (para el JUGADOR RESTANTE)
            listenerRegistration?.remove()
            listenerRegistration = null
            navController.popBackStack()
        }
    }


    // 🔹 DisposableEffect: Limpieza del listener
    DisposableEffect(key1 = Unit) {
        onDispose {
            // El listener se remueve aquí solo si la salida fue forzada por el sistema (ej. App cerrada)
            // En el caso del botón, ya se habrá removido en manejarSalida
            listenerRegistration?.remove()
        }
    }

    // ✅ Acción: Reiniciar ronda
    fun reiniciarRonda() {
        partidaId?.let {
            firebaseManager.actualizarEstadoDeJuego(
                partidaId = it,
                tablero = List(9) { 0 },
                turno = JUGADOR_X,
                estado = "en_juego",
                winner = 0,
                victoriasJ1 = partida.victoriasJugador1,
                victoriasJ2 = partida.victoriasJugador2
            )
        }
    }

    // 🔹 Acción: marcar casilla y actualizar puntaje
    fun jugar(pos: Int) {
        val miNumero = simboloJugadorLocal
        if (!esMiTurno || juegoTerminado || partida.tablero[pos] != 0) return

        val nuevoTablero = partida.tablero.toMutableList()
        nuevoTablero[pos] = miNumero

        reproducirSonidoMovimiento(context, miNumero)

        val ganador = verificarGanadorInt(nuevoTablero)
        val terminado = ganador != 0 || nuevoTablero.none { it == 0 }

        val siguienteTurno = if (miNumero == JUGADOR_X) JUGADOR_O else JUGADOR_X

        val nuevoEstado = when {
            ganador != 0 -> "terminado"
            terminado -> "empate"
            else -> "en_juego"
        }

        val turnoFinal = if (terminado) partida.turno else siguienteTurno

        var newWinner = 0
        var newVictoriasJ1 = partida.victoriasJugador1
        var newVictoriasJ2 = partida.victoriasJugador2

        if (ganador != 0) {
            newWinner = ganador
            if (ganador == JUGADOR_X) newVictoriasJ1++
            if (ganador == JUGADOR_O) newVictoriasJ2++
        }

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

    // --- Componentes Reutilizables de UI ---

    @Composable
    fun EstadoYBotones(esHorizontal: Boolean = false) {
        val mod = if (esHorizontal) Modifier.fillMaxWidth(0.5f).padding(horizontal = 16.dp) else Modifier.fillMaxWidth()

        Column(
            modifier = mod,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (esHorizontal) Arrangement.Center else Arrangement.Top
        ) {
            // Mostrar estado del juego
            Text(
                text = when {
                    partida.estado == "abandonada" || partidaEliminada -> "Partida Terminada (Rival Desconectado)"
                    partida.winner != 0 -> "¡Ganó Jugador ${partida.winner}!"
                    partida.estado == "empate" -> "¡Empate!"
                    juegoTerminado -> "Ronda Terminada"
                    partida.estado == "en_juego" -> {
                        val miNumero = simboloJugadorLocal
                        if (esMiTurno) "¡Es tu turno (${if (miNumero == JUGADOR_X) "X" else "O"})!"
                        else "Turno del oponente"
                    }
                    else -> "Estado desconocido"
                },
                color = Color.White,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(12.dp))

            // Mostrar Puntajes (Scoreboard)
            val esJugador1 = uid == partida.jugador1
            val miSimbolo = if (esJugador1) "X" else "O"
            val miPuntaje = if (esJugador1) partida.victoriasJugador1 else partida.victoriasJugador2
            val rivalPuntaje = if (esJugador1) partida.victoriasJugador2 else partida.victoriasJugador1

            if (esHorizontal) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Yo ($miSimbolo): $miPuntaje", color = Color.Cyan, fontSize = 16.sp)
                    Text("Rival: $rivalPuntaje", color = Color.Red, fontSize = 16.sp)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text("Yo ($miSimbolo): $miPuntaje", color = Color.Cyan, fontSize = 16.sp)
                    Text("Rival: $rivalPuntaje", color = Color.Red, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Botón de Reinicio (solo si el juego terminó o hay empate)
            if (partida.estado != "en_juego" && partida.estado != "esperando") {
                Button(
                    onClick = { reiniciarRonda() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    modifier = Modifier.fillMaxWidth(if (esHorizontal) 0.8f else 1f),
                    enabled = !partidaEliminada
                ) {
                    Text("Jugar de Nuevo", color = Color.White)
                }
                Spacer(Modifier.height(12.dp))
            }

            // Botón de Volver al inicio
            Button(
                onClick = manejarSalida,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.fillMaxWidth(if (esHorizontal) 0.8f else 1f)
            ) {
                Text("Volver al inicio", color = Color.White)
            }
        }
    }


    // -----------------------------------------------------
    //                  Diseño Principal
    // -----------------------------------------------------
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFF1E1E1E))
    ) {
        val esHorizontal = maxWidth > maxHeight

        if (esperandoOponente && !partidaEliminada) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Triqui Multijugador", color = Color.White, fontSize = 26.sp)
                Spacer(Modifier.height(20.dp))
                Text(
                    "Esperando a que otro jugador se una... ID Partida: ${partidaId ?: "Cargando..."}",
                    color = Color.Yellow,
                    fontSize = 18.sp
                )
            }
        } else if (esHorizontal) {
            // Diseño Horizontal (Row)
            Row(
                modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tablero
                TableroTriqui(
                    tablero = partida.tablero,
                    xImage = xImage,
                    oImage = oImage,
                    density = density,
                    juegoTerminado = juegoTerminado,
                    turnoAndroid = !esMiTurno,
                    onCeldaClick = ::jugar
                )
                Spacer(Modifier.width(24.dp))
                // Estado y Controles
                EstadoYBotones(esHorizontal = true)
            }
        } else {
            // Diseño Vertical (Column)
            Column(
                modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.weight(1f))
                Text("Triqui Multijugador", color = Color.White, fontSize = 26.sp)
                Spacer(Modifier.height(12.dp))

                // Tablero
                TableroTriqui(
                    tablero = partida.tablero,
                    xImage = xImage,
                    oImage = oImage,
                    density = density,
                    juegoTerminado = juegoTerminado,
                    turnoAndroid = !esMiTurno,
                    onCeldaClick = ::jugar
                )

                Spacer(Modifier.height(24.dp))
                EstadoYBotones(esHorizontal = false)
                Spacer(Modifier.weight(1f))
            }
        }
    }
}