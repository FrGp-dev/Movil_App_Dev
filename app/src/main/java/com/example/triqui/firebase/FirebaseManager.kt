package com.example.triqui.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject

data class Partida(
    // ... (campos existentes sin cambios) ...
    var tablero: List<Int> = List(9) { 0 },
    var jugador1: String = "",
    var jugador2: String = "", // UID del Jugador 2
    var turno: Int = 1, // 1 para Jugador 1 (X), 2 para Jugador 2 (O)
    var estado: String = "esperando", // "esperando", "en_juego", "terminado", "empate", "abandonada" <- NUEVO ESTADO
    var winner: Int = 0, // 0: nadie, 1: J1, 2: J2

    // CAMPOS NUEVOS PARA EL PUNTAJE:
    var victoriasJugador1: Int = 0,
    var victoriasJugador2: Int = 0
)

class FirebaseManager {
    // Usamos Firebase Firestore
    private val db = FirebaseFirestore.getInstance()
    private val partidasCollection = db.collection("partidas")

    private fun crearObjetoPartida(uid: String) = Partida(
        tablero = List(9) { 0 },
        jugador1 = uid,
        turno = 1,
        estado = "esperando",
        winner = 0,
        jugador2 = "",
        victoriasJugador1 = 0,
        victoriasJugador2 = 0
    )

    // CREAR PARTIDA (Usado por Jugador 1)
    fun crearPartida(uid: String, callback: (String) -> Unit) {
        val nuevaPartida = crearObjetoPartida(uid)
        partidasCollection.add(nuevaPartida).addOnSuccessListener { documentReference ->
            callback(documentReference.id)
        }
    }

    // ESCUCHAR PARTIDA (Obtiene actualizaciones en tiempo real)
    fun escucharPartida(partidaId: String, onUpdate: (Partida) -> Unit): ListenerRegistration {
        return partidasCollection.document(partidaId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Manejar error
                return@addSnapshotListener
            }
            // ✅ CORRECCIÓN CLAVE: Si el documento NO existe (fue borrado),
            // enviamos una partida con estado "eliminada" como bandera.
            if (snapshot != null && snapshot.exists()) {
                // Mapea el documento a la data class Partida
                snapshot.toObject<Partida>()?.let { onUpdate(it) }
            } else if (snapshot != null && !snapshot.exists()) {
                // Documento eliminado: Notifica a la UI para salir.
                onUpdate(Partida(estado = "eliminada"))
            }
        }
    }

    // ACTUALIZAR ESTADO DEL JUEGO (Función completa que maneja todos los campos)
    fun actualizarEstadoDeJuego(
        partidaId: String,
        tablero: List<Int>,
        turno: Int,
        estado: String,
        winner: Int,
        victoriasJ1: Int,
        victoriasJ2: Int
    ) {
        val updates = mapOf(
            "tablero" to tablero,
            "turno" to turno,
            "estado" to estado,
            "winner" to winner,
            "victoriasJugador1" to victoriasJ1,
            "victoriasJugador2" to victoriasJ2
        )
        partidasCollection.document(partidaId).set(updates, SetOptions.merge())
    }

    // ✅ NUEVA FUNCIÓN: Elimina el documento (solo si está en 'esperando')
    fun eliminarPartida(partidaId: String) {
        partidasCollection.document(partidaId).delete()
    }

    // ✅ NUEVA FUNCIÓN: Marca la partida como abandonada
    fun marcarPartidaAbandonada(partidaId: String) {
        val updates = mapOf(
            "estado" to "abandonada",
            "winner" to 0 // No hay ganador, fue abandono
        )
        partidasCollection.document(partidaId).set(updates, SetOptions.merge())
    }
}