package com.blackjack.exceptions;

/**
 * Excepción general para errores del juego
 */
public class GameException extends Exception {

    public GameException(String mensaje) {
        super(mensaje);
    }

    public GameException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
