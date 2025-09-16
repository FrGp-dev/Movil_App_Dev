package com.example.triqui.pantallas

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.triqui.R
import com.example.triqui.TriquiJuego
import com.example.triqui.navigation.Routes
import kotlinx.coroutines.delay

@Composable
fun PantallaJuego(navController: NavHostController) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // Recuperamos parámetros enviados desde la pantalla anterior
    val savedStateHandle = navController.previousBackStackEntry?.savedStateHandle
    val empiezaJugador = savedStateHandle?.get<Boolean>("empiezaJugador") ?: true
    val dificultad = savedStateHandle?.get<String>("dificultad") ?: "Fácil"

    // Estados principales del juego
    var juego by remember { mutableStateOf(TriquiJuego()) }
    var tablero by remember { mutableStateOf(juego.getTablero()) }
    var textoEstado by remember { mutableStateOf(if (empiezaJugador) "Tu turno" else "Turno de Android") }
    var juegoTerminado by remember { mutableStateOf(false) }

    var victoriasJugador by remember { mutableStateOf(0) }
    var victoriasComputador by remember { mutableStateOf(0) }
    var empates by remember { mutableStateOf(0) }

    var turnoAndroid by remember { mutableStateOf(!empiezaJugador) }

    // Sonidos precargados para evitar lag al reproducir
    val sonidoJugador = remember { MediaPlayer.create(context, R.raw.sonido_pup) }
    val sonidoAndroid = remember { MediaPlayer.create(context, R.raw.sonido_bip) }

    // Función para reproducir sonidos de manera optimizada
    fun reproducirSonido(mp: MediaPlayer) {
        if (mp.isPlaying) mp.seekTo(0) // reinicia si ya estaba sonando
        mp.start()
    }

    // Carga de imágenes (painterResource cacheado por Compose)
    val xImage = painterResource(id = R.drawable.x_jugador)
    val oImage = painterResource(id = R.drawable.o_enemigo)

    // Reiniciar todo el estado del juego
    fun reiniciarJuego() {
        juego = TriquiJuego()
        tablero = juego.getTablero()
        juegoTerminado = false
        turnoAndroid = !empiezaJugador
        textoEstado = if (empiezaJugador) "Tu turno" else "Turno de Android"
        Log.d("Triqui", "Nuevo juego iniciado")
    }

    // Turno del Android: se ejecuta automáticamente usando corutinas
    LaunchedEffect(turnoAndroid) {
        if (turnoAndroid && !juegoTerminado) {
            delay(500) // pequeño retraso para dar sensación de "pensar"
            val jugada = juego.jugadaComputador(dificultad)
            if (juego.ponerJugada(TriquiJuego.COMPUTADOR, jugada)) {
                tablero = juego.getTablero()
                reproducirSonido(sonidoAndroid)
                Log.d("Triqui", "Android jugó en $jugada")
                when (juego.verificarGanador()) {
                    0 -> textoEstado = "Tu turno"
                    1 -> { textoEstado = "¡Empate!"; empates++; juegoTerminado = true }
                    2 -> { textoEstado = "¡Ganaste!"; victoriasJugador++; juegoTerminado = true }
                    3 -> { textoEstado = "Android ganó"; victoriasComputador++; juegoTerminado = true }
                }
            }
            turnoAndroid = false
        }
    }

    // Diseño principal de la pantalla
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Spacer para empujar el contenido hacia el centro de la pantalla
        Spacer(Modifier.weight(1f))

        // Título del juego
        Text("TRIQUI", style = MaterialTheme.typography.headlineLarge,color = MaterialTheme.colorScheme.onSurface)

        Spacer(Modifier.height(12.dp))

        // Tablero de juego
        Box(
            modifier = Modifier
                .size(320.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (!juegoTerminado && !turnoAndroid) {
                            // Convertimos dp a píxeles reales para que el cálculo funcione en cualquier densidad
                            val tableroSizePx = with(density) { 320.dp.toPx() }
                            val cellSize = tableroSizePx / 3

                            val col = (offset.x / cellSize).toInt()
                            val row = (offset.y / cellSize).toInt()

                            // Validamos que el toque esté dentro de las celdas válidas
                            if (col in 0..2 && row in 0..2) {
                                val index = row * 3 + col
                                Log.d("Triqui", "Jugador tocó en fila=$row, col=$col, index=$index")

                                // Si la jugada es válida, actualizamos el estado
                                if (juego.ponerJugada(TriquiJuego.JUGADOR, index)) {
                                    tablero = juego.getTablero()
                                    reproducirSonido(sonidoJugador)
                                    when (juego.verificarGanador()) {
                                        0 -> { textoEstado = "Turno de Android"; turnoAndroid = true }
                                        1 -> { textoEstado = "¡Empate!"; empates++; juegoTerminado = true }
                                        2 -> { textoEstado = "¡Ganaste!"; victoriasJugador++; juegoTerminado = true }
                                        3 -> { textoEstado = "Android ganó"; victoriasComputador++; juegoTerminado = true }
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            // Dibujar las líneas del tablero
            Canvas(modifier = Modifier.matchParentSize()) {
                val cellSize = size.width / 3
                drawLine(Color.Gray, Offset(cellSize, 0f), Offset(cellSize, size.height), strokeWidth = 8f)
                drawLine(Color.Gray, Offset(cellSize * 2, 0f), Offset(cellSize * 2, size.height), strokeWidth = 8f)
                drawLine(Color.Gray, Offset(0f, cellSize), Offset(size.width, cellSize), strokeWidth = 8f)
                drawLine(Color.Gray, Offset(0f, cellSize * 2), Offset(size.width, cellSize * 2), strokeWidth = 8f)
            }

            // Dibujar fichas X y O en las posiciones correspondientes
            for (i in tablero.indices) {
                val row = i / 3
                val col = i % 3
                val offsetX = col * (320f / 3)
                val offsetY = row * (320f / 3)

                if (tablero[i] == TriquiJuego.JUGADOR) {
                    Image(
                        painter = xImage,
                        contentDescription = "Jugador",
                        modifier = Modifier
                            .size(100.dp)
                            .offset(x = offsetX.dp, y = offsetY.dp)
                    )
                } else if (tablero[i] == TriquiJuego.COMPUTADOR) {
                    Image(
                        painter = oImage,
                        contentDescription = "Android",
                        modifier = Modifier
                            .size(100.dp)
                            .offset(x = offsetX.dp, y = offsetY.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(textoEstado, style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Text("Jugador: $victoriasJugador",color = MaterialTheme.colorScheme.onSurface)
            Text("Android: $victoriasComputador",color = MaterialTheme.colorScheme.onSurface)
            Text("Empates: $empates",color = MaterialTheme.colorScheme.onSurface)
        }

        // Spacer para empujar los botones hacia abajo
        Spacer(Modifier.weight(1f))

        // Botones inferiores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(), // Respeta la barra de navegación del sistema
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { reiniciarJuego() }) { Text("Nuevo Juego",color = MaterialTheme.colorScheme.onSurface) }
            OutlinedButton(onClick = {
                navController.popBackStack(Routes.Inicio, inclusive = false)
            }) { Text("Volver al Inicio",color = MaterialTheme.colorScheme.onSurface) }
        }
    }
}
