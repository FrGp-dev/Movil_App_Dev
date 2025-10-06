package com.example.triqui.pantallas

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.triqui.R
import com.example.triqui.TriquiJuego
import com.example.triqui.navigation.Routes
import kotlinx.coroutines.delay

// NOTA: Para que funcione, DEBES asegurarte que ComponentesJuego.kt esté en el MISMO paquete
// (com.example.triqui.pantallas) o cambiar el 'package' y la importación del nuevo archivo.
// Se asume que el nuevo archivo también usa 'package com.example.triqui.pantallas'

@Composable
fun PantallaJuego(navController: NavHostController) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // Inicialización de SharedPreferences y Constantes
    val sharedPrefs = context.getSharedPreferences("TriquiPrefs", Context.MODE_PRIVATE)
    val KEY_TABLERO = "tablero"
    val KEY_VICTORIAS_JUGADOR = "victoriasJugador"
    val KEY_VICTORIAS_COMPUTADOR = "victoriasComputador"
    val KEY_EMPATES = "empates"
    val KEY_JUEGO_TERMINADO = "juegoTerminado"
    val KEY_TEXTO_ESTADO = "textoEstado"
    val KEY_TURNO_ANDROID = "turnoAndroid"

    // OBTENCIÓN DE PARÁMETROS DE RUTA (Lógica de Navegación)
    val navBackStackEntry = remember(navController.currentBackStackEntry) {
        navController.currentBackStackEntry
    }

    val iniciaString = navBackStackEntry?.arguments?.getString("inicia")
    val dificultadRuta = navBackStackEntry?.arguments?.getString("dificultad")
    val esJuegoNuevoPorRuta = !iniciaString.isNullOrEmpty()
    val empiezaJugador = iniciaString == "jugador"
    val dificultad = dificultadRuta ?: "Fácil"

    // --- ESTADOS PRINCIPALES ---
    var juego by remember { mutableStateOf(TriquiJuego()) }
    var tablero by rememberSaveable { mutableStateOf(MutableList(9) { 0 }) }
    var textoEstado by rememberSaveable { mutableStateOf("") }
    var juegoTerminado by rememberSaveable { mutableStateOf(false) }
    var victoriasJugador by rememberSaveable { mutableStateOf(0) }
    var victoriasComputador by rememberSaveable { mutableStateOf(0) }
    var empates by rememberSaveable { mutableStateOf(0) }
    var turnoAndroid by rememberSaveable { mutableStateOf(!empiezaJugador) }
    var cargadoInicialmente by rememberSaveable { mutableStateOf(false) }

    // --- FUNCIONES DE PERSISTENCIA ---
    fun guardarEstadoJuego() {
        sharedPrefs.edit().apply {
            putString(KEY_TABLERO, tablero.joinToString(""))
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
            tablero = tableroString.map { it.toString().toInt() }.toMutableList()
            victoriasJugador = sharedPrefs.getInt(KEY_VICTORIAS_JUGADOR, 0)
            victoriasComputador = sharedPrefs.getInt(KEY_VICTORIAS_COMPUTADOR, 0)
            empates = sharedPrefs.getInt(KEY_EMPATES, 0)
            juegoTerminado = sharedPrefs.getBoolean(KEY_JUEGO_TERMINADO, false)
            textoEstado = sharedPrefs.getString(KEY_TEXTO_ESTADO, "Tu turno") ?: "Tu turno"
            turnoAndroid = sharedPrefs.getBoolean(KEY_TURNO_ANDROID, !empiezaJugador)
            return true
        }
        return false
    }

    // --- EFECTOS DE CICLO DE VIDA (Carga y Persistencia) ---
    LaunchedEffect(Unit) {
        if (!cargadoInicialmente) {
            if (!esJuegoNuevoPorRuta && cargarEstadoJuego()) {
                Log.d("Triqui", "Juego restaurado desde SharedPreferences (Continuar).")
            } else {
                juego = TriquiJuego()
                tablero = juego.getTablero().toMutableList()
                textoEstado = if (empiezaJugador) "Tu turno" else "Turno de Android"
                turnoAndroid = !empiezaJugador
                Log.d("Triqui", "Juego nuevo iniciado.")
            }
            cargadoInicialmente = true
        }
    }

    LaunchedEffect(tablero) {
        if (tablero.any { it != 0 }) {
            juego.setTablero(tablero.toIntArray())
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            guardarEstadoJuego()
        }
    }

    // --- FUNCIONES Y RECURSOS DEL JUEGO ---
    val sonidoJugador = remember { MediaPlayer.create(context, R.raw.sonido_pup) }
    val sonidoAndroid = remember { MediaPlayer.create(context, R.raw.sonido_bip) }

    fun reproducirSonido(mp: MediaPlayer) {
        if (mp.isPlaying) mp.seekTo(0)
        mp.start()
    }

    val xImage = painterResource(id = R.drawable.x_jugador)
    val oImage = painterResource(id = R.drawable.o_enemigo)

    fun reiniciarJuego() {
        juego = TriquiJuego()
        tablero = juego.getTablero().toMutableList()
        juegoTerminado = false
        turnoAndroid = !empiezaJugador
        textoEstado = if (empiezaJugador) "Tu turno" else "Turno de Android"
        Log.d("Triqui", "Nuevo juego iniciado")
    }

    // Lógica del Turno de Android
    LaunchedEffect(turnoAndroid) {
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

    /**
     * Función que maneja el clic del jugador en una celda, actualiza el estado y verifica el ganador.
     * Esta función se pasa como callback al componente TableroTriqui.
     */
    fun onCeldaClick(index: Int) {
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

    // Diseño principal: usamos BoxWithConstraints para detectar la orientación
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        val esHorizontal = maxWidth > maxHeight

        // Función que encapsula la llamada al componente TableroTriqui (del archivo ComponentesJuego.kt)
        @Composable
        fun TableroTriquiInstancia() {
            TableroTriqui(
                tablero = tablero,
                xImage = xImage,
                oImage = oImage,
                density = density,
                juegoTerminado = juegoTerminado,
                turnoAndroid = turnoAndroid,
                onCeldaClick = ::onCeldaClick
            )
        }

        if (esHorizontal) {
            // Diseño Horizontal (Row)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableroTriquiInstancia()
                Spacer(Modifier.width(24.dp))
                // Usamos el componente ControlesYPuntajesHorizontal (del archivo ComponentesJuego.kt)
                ControlesYPuntajesHorizontal(
                    textoEstado = textoEstado,
                    victoriasJugador = victoriasJugador,
                    victoriasComputador = victoriasComputador,
                    empates = empates,
                    onReiniciarJuego = ::reiniciarJuego,
                    navController = navController
                )
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

                TableroTriquiInstancia()

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