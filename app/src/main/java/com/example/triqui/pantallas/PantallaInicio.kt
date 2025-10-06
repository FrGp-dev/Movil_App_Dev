package com.example.triqui.pantallas

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.triqui.navigation.Routes
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random


@Composable
fun ControlesSeleccion(
    empiezaJugador: Boolean,
    onEmpiezaJugadorChange: (Boolean) -> Unit,
    dificultad: String,
    onDificultadChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val opciones = listOf("Fácil", "Medio", "Difícil")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("¿Quién empieza?", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth(0.9f)) {
            OutlinedButton(
                onClick = { onEmpiezaJugadorChange(true) },
                border = BorderStroke(2.dp, if (empiezaJugador) Color(0xFF4CAF50) else Color.Gray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Jugador")
            }
            OutlinedButton(
                onClick = { onEmpiezaJugadorChange(false) },
                border = BorderStroke(2.dp, if (!empiezaJugador) Color(0xFFD32F2F) else Color.Gray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Android", color = Color.Red)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Dificultad", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        Box {
            Button(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(0.7f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(dificultad)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            onDificultadChange(opcion)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// ---------------------------
// ControlesAccion (añadido param onIniciarMultijugador)
// ---------------------------
@Composable
fun ControlesAccion(
    onIniciarJuego: () -> Unit,
    onMostrarAlerta: () -> Unit,
    onIniciarMultijugador: () -> Unit,
    fillWidthFactor: Float = 0.7f,
    juegoGuardado: Boolean,
    onContinuarJuego: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if (juegoGuardado) {
            Button(
                onClick = onContinuarJuego,
                modifier = Modifier
                    .fillMaxWidth(fillWidthFactor)
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text("Continuar juego", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        Button(
            onClick = onIniciarJuego,
            modifier = Modifier
                .fillMaxWidth(fillWidthFactor)
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("Iniciar juego nuevo", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(Modifier.height(10.dp))

        // Botón Multijugador
        Button(
            onClick = onIniciarMultijugador,
            modifier = Modifier
                .fillMaxWidth(fillWidthFactor)
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA)),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("Modo Multijugador", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onMostrarAlerta,
            modifier = Modifier
                .fillMaxWidth(fillWidthFactor)
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(2.dp, Color(0xFFB71C1C)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Text("Salir", fontSize = 16.sp, color = Color(0xFFB71C1C), fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------------------
// PantallaInicio (completa)
// ---------------------------
@Composable
fun PantallaInicio(navController: NavHostController) {
    var empiezaJugador by rememberSaveable { mutableStateOf(true) }
    var dificultad by rememberSaveable { mutableStateOf("Fácil") }
    val activity = LocalActivity.current
    val context = LocalContext.current
    var mostrar_alerta by remember { mutableStateOf(false) }
    var mostrar_creditos by remember { mutableStateOf(false) }
    var creandoPartida by remember { mutableStateOf(false) }


    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    val sharedPrefs = context.getSharedPreferences("TriquiPrefs", Context.MODE_PRIVATE)
    val KEY_TABLERO = "tablero"
    val juegoGuardado = sharedPrefs.contains(KEY_TABLERO)

    // ✅ CAMBIO CLAVE AQUÍ: Se modificó el bloque de navegación
    val onIniciarJuegoNuevo = {
        sharedPrefs.edit().remove(KEY_TABLERO).apply()

        // 1. Determinar el string de quien inicia ("jugador" o "android")
        val iniciaString = if (empiezaJugador) "jugador" else "android"

        // 2. Navegar a la nueva ruta dinámica: Routes.Juego/iniciaString/dificultad
        navController.navigate("${Routes.Juego}/$iniciaString/$dificultad")
    }

    val onContinuarJuego = { navController.navigate(Routes.Juego) }

    // FUNCIÓN: crear/unirse a partida (se pasa como referencia para evitar devolución Job)
    fun iniciarMultijugador() {
        scope.launch {
            creandoPartida = true
            try {
                // 1) Asegurar autenticación anónima (para identificar jugadores)
                val auth = FirebaseAuth.getInstance()
                var user = auth.currentUser
                if (user == null) {
                    // sign in anonymously (suspend via await)
                    try {
                        auth.signInAnonymously().await()
                    } catch (e: Exception) {
                        // si falla, generamos un id local temporal
                    }
                    user = auth.currentUser
                }
                val uid = user?.uid ?: "anon${Random.nextInt(1000, 9999)}"

                // 2) Buscar partida esperando
                val partidasRef = firestore.collection("partidas")
                val partidaDisponible = partidasRef
                    .whereEqualTo("estado", "esperando")
                    .limit(1)
                    .get()
                    .await()

                if (!partidaDisponible.isEmpty) {
                    // Unirse a la primera partida disponible
                    val doc = partidaDisponible.documents.first()
                    doc.reference.update(mapOf("jugador2" to uid, "estado" to "en_juego")).await()
                    navController.currentBackStackEntry?.savedStateHandle?.set("partidaId", doc.id)
                    navController.currentBackStackEntry?.savedStateHandle?.set("modoMultijugador", true)
                    navController.navigate(Routes.JuegoMultijugador)
                } else {
                    // Crear nueva partida
                    val nuevaPartidaMap = mapOf(
                        "jugador1" to uid,
                        "jugador2" to "",
                        "tablero" to List(9) { 0 }, // lista de Int
                        "turno" to 1,
                        "estado" to "esperando",
                        "winner" to 0
                    )
                    val nuevaPartida = partidasRef.add(nuevaPartidaMap).await()
                    navController.currentBackStackEntry?.savedStateHandle?.set("partidaId", nuevaPartida.id)
                    navController.currentBackStackEntry?.savedStateHandle?.set("modoMultijugador", true)
                    navController.navigate(Routes.JuegoMultijugador)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                creandoPartida = false
            }
        }
    }

    // --------------------------- UI ---------------------------
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF3F51B5), Color(0xFF81D4FA))))
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        val esHorizontal = maxWidth > maxHeight

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .wrapContentHeight(align = Alignment.Top)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.TopCenter)
        ) {
            if (!esHorizontal) Spacer(Modifier.weight(1f))

            AnimatedContent(targetState = "Bienvenido al Triqui") { titulo ->
                Text(
                    text = titulo,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    lineHeight = 40.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth(if (esHorizontal) 0.95f else 0.9f)
                    .wrapContentHeight(align = Alignment.CenterVertically),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                if (esHorizontal) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp)
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            ControlesSeleccion(empiezaJugador, { empiezaJugador = it }, dificultad, { dificultad = it })
                        }

                        Spacer(Modifier.width(32.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            ControlesAccion(
                                onIniciarJuego = onIniciarJuegoNuevo,
                                onMostrarAlerta = { mostrar_alerta = true },
                                onIniciarMultijugador = ::iniciarMultijugador,
                                fillWidthFactor = 1f,
                                juegoGuardado = juegoGuardado,
                                onContinuarJuego = onContinuarJuego
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ControlesSeleccion(empiezaJugador, { empiezaJugador = it }, dificultad, { dificultad = it })
                        Spacer(Modifier.height(24.dp))
                        ControlesAccion(
                            onIniciarJuego = onIniciarJuegoNuevo,
                            onMostrarAlerta = { mostrar_alerta = true },
                            onIniciarMultijugador = ::iniciarMultijugador,
                            fillWidthFactor = 0.7f,
                            juegoGuardado = juegoGuardado,
                            onContinuarJuego = onContinuarJuego
                        )
                    }
                }
            }

            if (!esHorizontal) Spacer(Modifier.weight(1f))
        }

        // Botón de Créditos
        TextButton(
            onClick = { mostrar_creditos = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Créditos", color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        // --- ALERTS Y DIÁLOGOS ---
        if (mostrar_alerta) {
            AlertDialog(
                onDismissRequest = { mostrar_alerta = false },
                title = { Text("Confirmar salida") },
                text = { Text("¿Estás seguro de que deseas salir de la aplicación?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            mostrar_alerta = false
                            activity?.finish()
                        }
                    ) {
                        Text("Sí", color = Color(0xFFEE0A0A), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrar_alerta = false }) {
                        Text("Cancelar", color = Color.Green)
                    }
                }
            )
        }

        if (mostrar_creditos) {
            AlertDialog(
                onDismissRequest = { mostrar_creditos = false },
                title = {
                    Column {
                        Text("Hecho Para")
                        Text("Desarrollo de Aplicaciones para Dispositivos Móviles")
                        Text("Semestre 2025-2")
                    }
                },
                text = {
                    Column {
                        Text("Desarrollado por Fredy Alexander Gonzalez Pobre")
                        Text("Universidad Nacional - Ing. de Sistemas y Computación", fontSize = 14.sp, color = Color.Gray)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { mostrar_creditos = false }) {
                        Text("Volver", color = Color(0xFF1CB72B), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (creandoPartida) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}