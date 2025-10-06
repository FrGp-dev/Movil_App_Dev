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
import com.example.triqui.R
import com.example.triqui.TriquiJuego
import com.example.triqui.navigation.Routes
import kotlinx.coroutines.delay

@Composable
fun PantallaJuego(navController: NavHostController) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // Inicialización de SharedPreferences
    val sharedPrefs = context.getSharedPreferences("TriquiPrefs", Context.MODE_PRIVATE)

    // Constantes para las claves de SharedPreferences
    val KEY_TABLERO = "tablero"
    val KEY_VICTORIAS_JUGADOR = "victoriasJugador"
    val KEY_VICTORIAS_COMPUTADOR = "victoriasComputador"
    val KEY_EMPATES = "empates"
    val KEY_JUEGO_TERMINADO = "juegoTerminado"
    val KEY_TEXTO_ESTADO = "textoEstado"
    val KEY_TURNO_ANDROID = "turnoAndroid"

    // ✅ INICIO DE LA SOLUCIÓN: OBTENCIÓN DE PARÁMETROS DE RUTA
    val navBackStackEntry = remember(navController.currentBackStackEntry) {
        navController.currentBackStackEntry
    }

    // Argumentos de ruta (Solo están presentes si se usó la ruta con parámetros para juego nuevo)
    val iniciaString = navBackStackEntry?.arguments?.getString("inicia")
    val dificultadRuta = navBackStackEntry?.arguments?.getString("dificultad")

    // Si iniciaString NO es nulo, significa que se navegó desde el botón "Iniciar Juego Nuevo"
    val esJuegoNuevoPorRuta = !iniciaString.isNullOrEmpty()

    // Derivar los parámetros para la inicialización (solo se usan si es nuevo)
    val empiezaJugador = iniciaString == "jugador"
    val dificultad = dificultadRuta ?: "Fácil"
    // ✅ FIN DE LA SOLUCIÓN: OBTENCIÓN DE PARÁMETROS DE RUTA

    // --- ESTADOS PRINCIPALES: Inicializados para la rotación (rememberSaveable) ---
    var juego by remember { mutableStateOf(TriquiJuego()) }
    // Inicializamos con un tablero vacío; se llenará al cargar o al iniciar un nuevo juego.
    var tablero by rememberSaveable { mutableStateOf(MutableList(9) { 0 }) }
    var textoEstado by rememberSaveable { mutableStateOf("") }
    var juegoTerminado by rememberSaveable { mutableStateOf(false) }

    var victoriasJugador by rememberSaveable { mutableStateOf(0) }
    var victoriasComputador by rememberSaveable { mutableStateOf(0) }
    var empates by rememberSaveable { mutableStateOf(0) }

    var turnoAndroid by rememberSaveable { mutableStateOf(!empiezaJugador) }

    // Bandera para asegurar que la carga inicial desde disco solo se haga una vez
    var cargadoInicialmente by rememberSaveable { mutableStateOf(false) }

    // --- FUNCIONES DE PERSISTENCIA ---

    fun guardarEstadoJuego() {
        sharedPrefs.edit().apply {
            // Guardamos el tablero como una sola cadena de 9 dígitos (ej: "120102000")
            putString(KEY_TABLERO, tablero.joinToString(""))

            // Guardamos los puntajes y estados
            putInt(KEY_VICTORIAS_JUGADOR, victoriasJugador)
            putInt(KEY_VICTORIAS_COMPUTADOR, victoriasComputador)
            putInt(KEY_EMPATES, empates)
            putBoolean(KEY_JUEGO_TERMINADO, juegoTerminado)
            putString(KEY_TEXTO_ESTADO, textoEstado)
            putBoolean(KEY_TURNO_ANDROID, turnoAndroid)

            apply()
        }
        Log.d("Triqui", "Estado de juego guardado.")
    }

    fun cargarEstadoJuego(): Boolean {
        val tableroString = sharedPrefs.getString(KEY_TABLERO, null)

        if (tableroString != null && tableroString.length == TriquiJuego.TABLERO_TAM) {
            // Cargar y actualizar los estados
            tablero = tableroString.map { it.toString().toInt() }.toMutableList()
            victoriasJugador = sharedPrefs.getInt(KEY_VICTORIAS_JUGADOR, 0)
            victoriasComputador = sharedPrefs.getInt(KEY_VICTORIAS_COMPUTADOR, 0)
            empates = sharedPrefs.getInt(KEY_EMPATES, 0)
            juegoTerminado = sharedPrefs.getBoolean(KEY_JUEGO_TERMINADO, false)
            textoEstado = sharedPrefs.getString(KEY_TEXTO_ESTADO, "Tu turno") ?: "Tu turno"
            turnoAndroid = sharedPrefs.getBoolean(KEY_TURNO_ANDROID, !empiezaJugador)

            return true // Indica que se cargó un estado guardado
        }
        return false // No hay juego guardado
    }

    // --- EFECTOS DE CICLO DE VIDA ---

    // 1. Carga Inicial del Estado desde disco o inicio de juego nuevo
    LaunchedEffect(Unit) {
        if (!cargadoInicialmente) {
            // ✅ INICIO DE LA SOLUCIÓN: LÓGICA DE CARGA
            // Si NO es un juego nuevo por ruta y logramos cargar el estado, CONTINUAR JUEGO.
            if (!esJuegoNuevoPorRuta && cargarEstadoJuego()) {
                Log.d("Triqui", "Juego restaurado desde SharedPreferences (Continuar).")
            } else {
                // Si es un juego nuevo por ruta O falló la carga (no había guardado): INICIAR NUEVO
                juego = TriquiJuego()
                tablero = juego.getTablero().toMutableList()
                textoEstado = if (empiezaJugador) "Tu turno" else "Turno de Android"
                turnoAndroid = !empiezaJugador
                Log.d("Triqui", "Juego nuevo iniciado.")
            }
            // ✅ FIN DE LA SOLUCIÓN: LÓGICA DE CARGA
            cargadoInicialmente = true
        }
    }

    // 2. Sincronización de la Lógica (para manejar la ROTACIÓN)
    LaunchedEffect(tablero) {
        if (tablero.any { it != 0 }) {
            // Sincroniza el objeto de lógica 'juego' con el estado persistido/actual
            juego.setTablero(tablero.toIntArray())
        }
    }

    // 3. Guardar el estado al salir de la pantalla (Cierre de App o navegación atrás)
    DisposableEffect(Unit) {
        onDispose {
            guardarEstadoJuego()
        }
    }

    // --- RESTO DEL CÓDIGO (Igual que antes) ---

    val sonidoJugador = remember { MediaPlayer.create(context, R.raw.sonido_pup) }
    val sonidoAndroid = remember { MediaPlayer.create(context, R.raw.sonido_bip) }

    fun reproducirSonido(mp: MediaPlayer) {
        if (mp.isPlaying) mp.seekTo(0)
        mp.start()
    }

    val xImage = painterResource(id = R.drawable.x_jugador)
    val oImage = painterResource(id = R.drawable.o_enemigo)

    // Reiniciar
    fun reiniciarJuego() {
        juego = TriquiJuego()
        tablero = juego.getTablero().toMutableList()
        juegoTerminado = false
        turnoAndroid = !empiezaJugador
        textoEstado = if (empiezaJugador) "Tu turno" else "Turno de Android"
        Log.d("Triqui", "Nuevo juego iniciado")
        // No borramos el SharedPreferences aquí, simplemente el estado de Compose se reinicia.
    }

    LaunchedEffect(turnoAndroid) {
        // Ejecución de la jugada de Android (lógica idéntica)
        if (turnoAndroid && !juegoTerminado) {
            delay(500)
            val jugada = juego.jugadaComputador(dificultad)
            if (juego.ponerJugada(TriquiJuego.COMPUTADOR, jugada)) {
                tablero = juego.getTablero().toMutableList()
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

    // --- Componente para el Tablero ---
    @Composable
    fun TableroTriqui() {
        Box(
            modifier = Modifier
                .size(320.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (!juegoTerminado && !turnoAndroid) {
                            val tableroSizePx = with(density) { 320.dp.toPx() }
                            val cellSize = tableroSizePx / 3

                            val col = (offset.x / cellSize).toInt()
                            val row = (offset.y / cellSize).toInt()

                            if (col in 0..2 && row in 0..2) {
                                val index = row * 3 + col
                                if (juego.ponerJugada(TriquiJuego.JUGADOR, index)) {
                                    tablero = juego.getTablero().toMutableList()
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
            // ... (Dibujo de líneas y fichas) ...
            Canvas(modifier = Modifier.matchParentSize()) {
                val cellSize = size.width / 3
                drawLine(Color.Gray, Offset(cellSize, 0f), Offset(cellSize, size.height), strokeWidth = 8f)
                drawLine(Color.Gray, Offset(cellSize * 2, 0f), Offset(cellSize * 2, size.height), strokeWidth = 8f)
                drawLine(Color.Gray, Offset(0f, cellSize), Offset(size.width, cellSize), strokeWidth = 8f)
                drawLine(Color.Gray, Offset(0f, cellSize * 2), Offset(size.width, cellSize * 2), strokeWidth = 8f)
            }

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
    }

    @Composable
    fun ControlesYPuntajesHorizontal() {
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
                Button(onClick = { reiniciarJuego() }, modifier = Modifier.fillMaxWidth(0.8f)) {
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


    // Diseño principal: usamos BoxWithConstraints para detectar la orientación
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        val esHorizontal = maxWidth > maxHeight

        if (esHorizontal) {
            // Diseño Horizontal (Row)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableroTriqui()
                Spacer(Modifier.width(24.dp))
                ControlesYPuntajesHorizontal()
            }
        } else {
            // Diseño Vertical (Column)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.weight(1f))

                Text("TRIQUI", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))

                TableroTriqui()

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
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { reiniciarJuego() }) { Text("Nuevo Juego", color = MaterialTheme.colorScheme.onSurface) }
                    OutlinedButton(onClick = {
                        navController.popBackStack(Routes.Inicio, inclusive = false)
                    }) { Text("Volver al Inicio", color = MaterialTheme.colorScheme.onSurface) }
                }
            }
        }
    }
}