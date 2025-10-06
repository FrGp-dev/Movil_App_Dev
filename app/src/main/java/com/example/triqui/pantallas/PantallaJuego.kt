package com.example.triqui.pantallas

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState // Importación necesaria
import com.example.triqui.R
import com.example.triqui.TriquiJuego
import com.example.triqui.navigation.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
// Asumo que estas funciones existen:
import com.example.triqui.pantallas.TableroTriquiReutilizable
import com.example.triqui.pantallas.reproducirSonidoMovimiento

@Composable
fun PantallaJuego(navController: NavHostController) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // Obtener la entrada de la pila de navegación para leer argumentos
    val backStackEntry = navController.currentBackStackEntryAsState().value

    // ✅ LECTURA DE ARGUMENTOS: Quien inicia y dificultad
    val inicia = backStackEntry?.arguments?.getString("inicia") ?: "jugador"
    val dificultadArg = backStackEntry?.arguments?.getString("dificultad") ?: "Difícil"

    // Inicialización de SharedPreferences
    val sharedPrefs = context.getSharedPreferences("TriquiPrefs", Context.MODE_PRIVATE)

    // Constantes
    val KEY_VICTORIAS_JUGADOR = "victoriasJugador"
    val KEY_VICTORIAS_COMPUTADOR = "victoriasComputador"
    val KEY_EMPATES = "empates"
    val JUGADOR_LOCAL = TriquiJuego.JUGADOR

    // ----------------------------------------------------
    // ESTADO DEL JUEGO
    // ----------------------------------------------------
    var triqui by remember { mutableStateOf(TriquiJuego()) }
    var tablero by rememberSaveable { mutableStateOf(triqui.getTablero().toList()) }
    var juegoTerminado by rememberSaveable { mutableStateOf(false) }

    // ✅ ESTADO INICIAL CORREGIDO: Inicia según el argumento
    var empiezaJugador by rememberSaveable { mutableStateOf(inicia == "jugador") }
    var esTurnoJugador by rememberSaveable { mutableStateOf(empiezaJugador) }
    val dificultad = remember { dificultadArg } // Dificultad fija por partida

    var textoEstado by rememberSaveable { mutableStateOf("Toca para empezar") }

    // Puntajes persistentes
    var victoriasJugador by rememberSaveable { mutableStateOf(sharedPrefs.getInt(KEY_VICTORIAS_JUGADOR, 0)) }
    var victoriasComputador by rememberSaveable { mutableStateOf(sharedPrefs.getInt(KEY_VICTORIAS_COMPUTADOR, 0)) }
    var empates by rememberSaveable { mutableStateOf(sharedPrefs.getInt(KEY_EMPATES, 0)) }

    val scope = rememberCoroutineScope()

    // ----------------------------------------------------
    // LÓGICA DE ESTADO (Ordenada para evitar "Unresolved reference")
    // ----------------------------------------------------

    // ✅ 1. ACTUALIZAR ESTADO (Fix: usa códigos 2, 3, 1 de TriquiJuego.java)
    fun actualizarEstado(ganador: Int) {
        juegoTerminado = true
        when (ganador) {
            2 -> { // Código 2: Gana Jugador
                textoEstado = "¡Ganaste!"
                victoriasJugador++
                sharedPrefs.edit().putInt(KEY_VICTORIAS_JUGADOR, victoriasJugador).apply()
            }
            3 -> { // Código 3: Gana Computador
                textoEstado = "¡Perdiste!"
                victoriasComputador++
                sharedPrefs.edit().putInt(KEY_VICTORIAS_COMPUTADOR, victoriasComputador).apply()
            }
            1 -> { // Código 1: Empate
                textoEstado = "¡Empate!"
                empates++
                sharedPrefs.edit().putInt(KEY_EMPATES, empates).apply()
            }
            else -> textoEstado = "Fin de juego"
        }
    }

    // 2. MOVIMIENTO COMPUTADORA (Función suspendida para usar delay)
    suspend fun ejecutarJugadaComputador() {
        if (juegoTerminado) return

        delay(500L)

        // Usa el método y la dificultad correctos
        val posComputador = triqui.jugadaComputador(dificultad)

        // Usa el método correcto
        if (posComputador != -1 && triqui.ponerJugada(TriquiJuego.COMPUTADOR, posComputador)) {
            tablero = triqui.getTablero().toList()
            reproducirSonidoMovimiento(context, JUGADOR_LOCAL, TriquiJuego.COMPUTADOR)

            val ganadorComputador = triqui.verificarGanador()
            if (ganadorComputador != 0) {
                actualizarEstado(ganadorComputador)
            } else {
                esTurnoJugador = true
                textoEstado = "Tu turno"
            }
        } else if (posComputador == -1) {
            actualizarEstado(1)
        }
    }

    // 3. INICIAR TURNO
    fun iniciarTurnoInicial() {
        esTurnoJugador = empiezaJugador
        juegoTerminado = false
        if (!esTurnoJugador) {
            textoEstado = "Turno de Android..."
            scope.launch {
                ejecutarJugadaComputador()
            }
        } else {
            textoEstado = "Tu turno"
        }
    }

    // 4. REINICIAR JUEGO (Alterna quien empieza)
    fun reiniciarJuego() {
        triqui.limpiarTablero()
        tablero = triqui.getTablero().toList()

        // Lógica de alternancia: el que sigue en la próxima ronda es el opuesto al que inició esta.
        empiezaJugador = !empiezaJugador

        iniciarTurnoInicial()
    }

    // 5. JUGAR (Maneja el click del jugador)
    fun jugar(pos: Int) {
        if (juegoTerminado || !esTurnoJugador || tablero[pos] != TriquiJuego.VACIO) return

        // Usa el método correcto
        if (triqui.ponerJugada(TriquiJuego.JUGADOR, pos)) {
            tablero = triqui.getTablero().toList()
            reproducirSonidoMovimiento(context, JUGADOR_LOCAL, TriquiJuego.JUGADOR)
            esTurnoJugador = false

            val ganador = triqui.verificarGanador()
            if (ganador != 0) {
                actualizarEstado(ganador)
                return
            }

            textoEstado = "Turno de Android..."
            scope.launch {
                ejecutarJugadaComputador()
            }
        }
    }

    // Inicia la lógica al cargar la pantalla
    LaunchedEffect(Unit) {
        iniciarTurnoInicial()
    }

    // ----------------------------------------------------
    // UI (Tu código original de apariencia se mantiene intacto)
    // ----------------------------------------------------

    Scaffold { paddingValues ->
        Surface(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Aquí va tu UI original
                Text("TRIQUI", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))

                // Aquí deberías tener tu TableroTriqui o TableroTriquiReutilizable
                // He usado TableroTriquiReutilizable basado en snippets anteriores.
                // Si usas TableroTriqui() solo, cámbialo.
                TableroTriquiReutilizable(
                    tablero = tablero.toList(),
                    simboloJugadorLocal = JUGADOR_LOCAL,
                    onCasillaClick = ::jugar
                )

                Spacer(Modifier.height(12.dp))

                Text(textoEstado, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text("Jugador: $victoriasJugador", color = MaterialTheme.colorScheme.onSurface)
                    Text("Android: $victoriasComputador", color = MaterialTheme.colorScheme.onSurface)
                    Text("Empates: $empates", color = MaterialTheme.colorScheme.onSurface)
                }

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // El botón solo se habilita al finalizar el juego
                    Button(onClick = { reiniciarJuego() }, enabled = juegoTerminado) {
                        Text("Nuevo Juego", color = MaterialTheme.colorScheme.onSurface)
                    }
                    OutlinedButton(onClick = {
                        navController.popBackStack(Routes.Inicio, inclusive = false)
                    }) { Text("Volver al Inicio", color = MaterialTheme.colorScheme.onSurface) }
                }
            }
        }
    }
}