package com.example.triqui.pantallas


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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.triqui.navigation.Routes

@Composable
fun PantallaInicio(navController: NavHostController) {
    var empiezaJugador by rememberSaveable { mutableStateOf(true) }
    var dificultad by rememberSaveable { mutableStateOf("Fácil") }
    var expanded by remember { mutableStateOf(false) }
    val opciones = listOf("Fácil", "Medio", "Difícil")
    val activity = LocalActivity.current
    var mostrar_alerta by remember { mutableStateOf(false) }
    var mostrar_creditos by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF3F51B5), Color(0xFF81D4FA))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            AnimatedContent(targetState = "Bienvenido al Triqui") { titulo ->
                Text(
                    text = titulo,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Selector: quién empieza
                    Text("¿Quién empieza?", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        OutlinedButton(
                            onClick = { empiezaJugador = true },
                            border = BorderStroke(2.dp, if (empiezaJugador) Color(0xFF4CAF50) else Color.Gray),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(6.dp)
                        ) {
                            Text("Jugador")
                        }
                        OutlinedButton(
                            onClick = { empiezaJugador = false },
                            border = BorderStroke(2.dp, if (!empiezaJugador) Color(0xFFD32F2F) else Color.Gray),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(6.dp)
                        ) {
                            Text("Android")
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Selector de dificultad
                    Text("Dificultad", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
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
                                        dificultad = opcion
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Botón principal: Iniciar Juego
                    Button(
                        onClick = {
                            navController.currentBackStackEntry?.savedStateHandle?.set("empiezaJugador", empiezaJugador)
                            navController.currentBackStackEntry?.savedStateHandle?.set("dificultad", dificultad)
                            navController.navigate(Routes.Juego)
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Iniciar juego", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { mostrar_alerta=true},
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(2.dp, Color(0xFFB71C1C)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Text("Salir", fontSize = 16.sp, color = Color(0xFFB71C1C), fontWeight = FontWeight.Bold)
                    }
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
                                    Text("Sí", color = Color(0xFFB71C1C), fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { mostrar_alerta = false }) {
                                    Text("Cancelar", color = Color.DarkGray)
                                }
                            }
                        )
                    }
                }
            }
        }
        Button(onClick = { mostrar_creditos=true},
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Creditos")
        }
        if (mostrar_creditos) {
            AlertDialog(
                onDismissRequest = { mostrar_creditos = false },
                title = {  Column {
                    Text("Hecho Para")
                    Text("Desarrollo de Aplicaciones para Dispositivos Móviles")
                    Text("Semestre 2025-2")
                } },
                text = { Column {
                    Text("Desarrollado por Fredy Alexander Gonzalez Pobre")
                    Text("Universidad Naciona Ing. De Sistemas Y Computacion", fontSize = 14.sp, color = Color.Gray)
                } },
                confirmButton = {
                    TextButton(
                        onClick = {
                            mostrar_creditos = false
                        }
                    ) {
                        Text("Volver", color = Color(0xFF1CB72B), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
