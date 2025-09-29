package com.example.triqui;

import java.util.Random;

public class TriquiJuego {
    // CAMBIO 1: Usamos INTs en lugar de CHARs para compatibilidad con Compose/rememberSaveable
    public static final int JUGADOR = 1;
    public static final int COMPUTADOR = 2;
    public static final int VACIO = 0;
    public static final int TABLERO_TAM = 9;

    private int[] tablero; // CAMBIO 2: Tablero ahora es de tipo int[]
    private Random aleatorio;

    public TriquiJuego() {
        tablero = new int[TABLERO_TAM];
        aleatorio = new Random();
        limpiarTablero();
    }

    public void limpiarTablero() {
        for (int i = 0; i < TABLERO_TAM; i++) {
            tablero[i] = VACIO;
        }
    }

    // MÉTODO REQUERIDO 1: getTablero ahora devuelve int[]
    public int[] getTablero() {
        return tablero.clone();
    }

    // MÉTODO REQUERIDO 2: setTablero para restaurar el estado después de rotar
    public void setTablero(int[] nuevoTablero) {
        if (nuevoTablero.length == TABLERO_TAM) {
            System.arraycopy(nuevoTablero, 0, this.tablero, 0, TABLERO_TAM);
        } else {
            // Manejo de error si el array es de tamaño incorrecto
            limpiarTablero();
        }
    }

    public boolean ponerJugada(int jugador, int posicion) { // CAMBIO 3: 'jugador' es de tipo int
        if (posicion >= 0 && posicion < TABLERO_TAM && tablero[posicion] == VACIO) {
            tablero[posicion] = jugador;
            return true;
        }
        return false;
    }

    /**
     * Jugada del computador según la dificultad seleccionada
     */
    public int jugadaComputador(String dificultad) {
        // ... (La lógica de dificultad se mantiene igual, ya que usa los métodos auxiliares)
        if ("Fácil".equalsIgnoreCase(dificultad)) {
            return jugadaAleatoria();
        } else if ("Medio".equalsIgnoreCase(dificultad)) {
            int winMove = buscarJugadaGanadora(COMPUTADOR);
            if (winMove != -1) return winMove;
            return jugadaAleatoria();
        } else if ("Difícil".equalsIgnoreCase(dificultad)) {
            int winMove = buscarJugadaGanadora(COMPUTADOR);
            if (winMove != -1) return winMove;

            int blockMove = buscarJugadaGanadora(JUGADOR);
            if (blockMove != -1) return blockMove;

            return jugadaAleatoria();
        }
        return jugadaAleatoria();
    }

    // --- Métodos auxiliares ---

    private int jugadaAleatoria() {
        int movimiento;
        do {
            movimiento = aleatorio.nextInt(TABLERO_TAM);
        } while (tablero[movimiento] != VACIO);
        return movimiento;
    }

    /**
     * Busca si el jugador puede ganar en la siguiente jugada.
     */
    private int buscarJugadaGanadora(int jugador) { // CAMBIO 4: 'jugador' es de tipo int
        for (int i = 0; i < TABLERO_TAM; i++) {
            if (tablero[i] == VACIO) {
                tablero[i] = jugador;
                int ganador = verificarGanador();
                tablero[i] = VACIO;
                if ((jugador == JUGADOR && ganador == 2) ||
                        (jugador == COMPUTADOR && ganador == 3)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Verifica el estado del juego
     * @return 0 = nadie, 1 = empate, 2 = gana jugador, 3 = gana compu
     */
    public int verificarGanador() {
        // Filas
        for (int i = 0; i <= 6; i += 3) {
            if (tablero[i] == tablero[i + 1] && tablero[i + 1] == tablero[i + 2] && tablero[i] != VACIO) {
                return (tablero[i] == JUGADOR) ? 2 : 3;
            }
        }

        // Columnas
        for (int i = 0; i < 3; i++) {
            if (tablero[i] == tablero[i + 3] && tablero[i + 3] == tablero[i + 6] && tablero[i] != VACIO) {
                return (tablero[i] == JUGADOR) ? 2 : 3;
            }
        }

        // Diagonales
        if ((tablero[0] == tablero[4] && tablero[4] == tablero[8] && tablero[0] != VACIO) ||
                (tablero[2] == tablero[4] && tablero[4] == tablero[6] && tablero[2] != VACIO)) {
            return (tablero[4] == JUGADOR) ? 2 : 3;
        }

        // ¿Empate?
        for (int c : tablero) { // CAMBIO 5: Iteramos sobre int
            if (c == VACIO) return 0;
        }
        return 1;
    }
}