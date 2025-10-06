package com.example.triqui.pantallas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.triqui.TriquiJuego
import com.example.triqui.navigation.Routes

/**
 * Componente Composable que renderiza el tablero del Triqui y maneja las interacciones táctiles.
 *
 * @param tablero Lista que representa el estado actual de las celdas (0=vacío, 1=Jugador, 2=Android).
 * @param xImage Painter para la ficha del jugador (X).
 * @param oImage Painter para la ficha del computador (O).
 * @param density Objeto Density para convertir dp a pixeles.
 * @param juegoTerminado Indica si el juego ha finalizado (para bloquear clics).
 * @param turnoAndroid Indica si es el turno de Android (para bloquear clics del jugador).
 * @param onCeldaClick Lambda que se ejecuta al tocar una celda válida, devuelve el índice (0-8).
 */
@Composable
fun TableroTriqui(
    tablero: List<Int>,
    xImage: Painter,
    oImage: Painter,
    density: Density,
    juegoTerminado: Boolean,
    turnoAndroid: Boolean,
    onCeldaClick: (Int) -> Unit
) {
    val boardSizeDp = 320.dp

    Box(
        modifier = Modifier
            .size(boardSizeDp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (!juegoTerminado && !turnoAndroid) {
                        val tableroSizePx = with(density) { boardSizeDp.toPx() }
                        val cellSize = tableroSizePx / 3

                        val col = (offset.x / cellSize).toInt()
                        val row = (offset.y / cellSize).toInt()

                        if (col in 0..2 && row in 0..2) {
                            val index = row * 3 + col
                            onCeldaClick(index)
                        }
                    }
                }
            }
    ) {
        // --- 1. Dibujo de las Líneas del Tablero ---
        Canvas(modifier = Modifier.matchParentSize()) {
            val cellSize = size.width / 3
            drawLine(Color.Gray, Offset(cellSize, 0f), Offset(cellSize, size.height), strokeWidth = 8f)
            drawLine(Color.Gray, Offset(cellSize * 2, 0f), Offset(cellSize * 2, size.height), strokeWidth = 8f)
            drawLine(Color.Gray, Offset(0f, cellSize), Offset(size.width, cellSize), strokeWidth = 8f)
            drawLine(Color.Gray, Offset(0f, cellSize * 2), Offset(size.width, cellSize * 2), strokeWidth = 8f)
        }

        // --- 2. Dibujo de las Fichas (X y O) ---
        for (i in tablero.indices) {
            val row = i / 3
            val col = i % 3
            val offsetDp = 320f / 3

            val offsetX = col * offsetDp
            val offsetY = row * offsetDp

            val imagePainter = when (tablero[i]) {
                TriquiJuego.JUGADOR -> xImage
                TriquiJuego.COMPUTADOR -> oImage
                else -> null
            }

            if (imagePainter != null) {
                Image(
                    painter = imagePainter,
                    contentDescription = if (tablero[i] == TriquiJuego.JUGADOR) "Jugador" else "Android",
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = offsetX.dp, y = offsetY.dp)
                )
            }
        }
    }
}

/**
 * Componente que muestra los puntajes y los botones de control para la vista horizontal.
 */
@Composable
fun ControlesYPuntajesHorizontal(
    textoEstado: String,
    victoriasJugador: Int,
    victoriasComputador: Int,
    empates: Int,
    onReiniciarJuego: () -> Unit,
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 8.dp)
            .fillMaxWidth(0.5f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(textoEstado, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(24.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(8.dp))
            Text("Jugador: $victoriasJugador", color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Text("Android: $victoriasComputador", color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Text("Empates: $empates", color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(Modifier.height(24.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = onReiniciarJuego, modifier = Modifier.fillMaxWidth(0.8f)) {
                Text("Nuevo Juego", color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {
                navController.popBackStack(Routes.Inicio, inclusive = false)
            }, modifier = Modifier.fillMaxWidth(0.8f)) {
                Text("Volver al Inicio", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}