// ComponentesJuego.kt (Crear este archivo)

package com.example.triqui.pantallas

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.triqui.R // Asegúrate de que esta importación sea correcta


fun reproducirSonidoMovimiento(context: Context, simboloLocal: Int, valorCasilla: Int) {
    if (valorCasilla == 0) return // No reproducir si no hay movimiento

    val esMovimientoPropio = (valorCasilla == simboloLocal)
    // Asume R.raw.move_propio y R.raw.move_oponente son tus IDs de recursos de sonido
    val sonidoResId = if (esMovimientoPropio) {
        R.raw.sonido_bip
    } else {
        R.raw.sonido_pup
    }

    try {
        // Ejecutar sonido
        MediaPlayer.create(context, sonidoResId)?.start()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// 🖼️ Composable reutilizable de la casilla (Tu CajaCasilla con iconos y lógica de click)
@Composable
fun CajaCasilla(
    valor: Int, // 0: vacía, 1: Jugador 1 (X), 2: Jugador 2 (O)
    simboloJugadorLocal: Int, // 1 o 2
    onClick: () -> Unit
) {
    // Reemplaza con tus IDs de recursos de imágenes
    val recursoIcono = when (valor) {
        1 -> R.drawable.o_enemigo
        2 -> R.drawable.x_jugador
        else -> null
    }

    Box(
        modifier = Modifier
            .size(100.dp)
            .padding(4.dp)
            .background(Color.DarkGray)
            .clickable {
                // Solo permite el click si la casilla está vacía
                if (valor == 0) onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        recursoIcono?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(0.8f)
            )
        }
    }
}

// 🖼️ Composable reutilizable del Tablero
@Composable
fun TableroTriquiReutilizable(
    tablero: List<Int>,
    simboloJugadorLocal: Int, // 1 o 2 (Necesario para la celda)
    onCasillaClick: (pos: Int) -> Unit
) {
    // Aquí puedes incluir el Canvas y la lógica de dibujo de líneas de tu TableroTriqui original
    // Por ahora, solo usamos la estructura de Column/Row
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (i in 0 until 3) {
            Row {
                for (j in 0 until 3) {
                    val index = i * 3 + j
                    CajaCasilla(
                        valor = tablero[index],
                        simboloJugadorLocal = simboloJugadorLocal,
                        onClick = { onCasillaClick(index) }
                    )
                }
            }
        }
    }
}

// 🧠 Función de verificación de ganador
fun verificarGanadorInt(tablero: List<Int>): Int {
    val lineas = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Filas
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columnas
        listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonales
    )

    for (linea in lineas) {
        if (tablero[linea[0]] != 0 &&
            tablero[linea[0]] == tablero[linea[1]] &&
            tablero[linea[1]] == tablero[linea[2]]) {
            return tablero[linea[0]]
        }
    }
    return 0
}