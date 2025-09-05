package com.example.triqui;

import java.util.Random;

public class TriquiJuego {
    public static final char JUGADOR = 'X';
    public static final char COMPUTADOR = 'O';
    public static final char VACIO = ' ';
    public static final int TABLERO_TAM = 9;

    private char[] tablero;
    private Random aleatorio;

    public TriquiJuego() {
        tablero = new char[TABLERO_TAM];
        aleatorio = new Random();
        limpiarTablero();
    }

    public void limpiarTablero() {
        for (int i = 0; i < TABLERO_TAM; i++) {
            tablero[i] = VACIO;
        }
    }

    public char[] getTablero() {
        return tablero.clone();
    }

    public boolean ponerJugada(char jugador, int posicion) {
        if (tablero[posicion] == VACIO) {
            tablero[posicion] = jugador;
            return true;
        }
        return false;
    }

    public int jugadaComputador() {
        int movimiento;
        do {
            movimiento = aleatorio.nextInt(TABLERO_TAM);
        } while (tablero[movimiento] != VACIO);
        return movimiento;
    }

    public int verificarGanador() {
        // 0 = nadie, 1 = empate, 2 = gana jugador, 3 = gana compu

        // Filas
        for (int i = 0; i <= 6; i += 3) {
            if (tablero[i] == tablero[i+1] && tablero[i+1] == tablero[i+2] && tablero[i] != VACIO) {
                return (tablero[i] == JUGADOR) ? 2 : 3;
            }
        }

        // Columnas
        for (int i = 0; i < 3; i++) {
            if (tablero[i] == tablero[i+3] && tablero[i+3] == tablero[i+6] && tablero[i] != VACIO) {
                return (tablero[i] == JUGADOR) ? 2 : 3;
            }
        }

        // Diagonales
        if ((tablero[0] == tablero[4] && tablero[4] == tablero[8] && tablero[0] != VACIO) ||
                (tablero[2] == tablero[4] && tablero[4] == tablero[6] && tablero[2] != VACIO)) {
            return (tablero[4] == JUGADOR) ? 2 : 3;
        }

        // ¿Empate?
        for (char c : tablero) {
            if (c == VACIO) return 0;
        }
        return 1;
    }
}
