package com.blackjack.utils;

/**
 * Clase de utilidad para validaciones
 * Principio OCP: Abierto para extensión
 */
public class Validator {

    public static boolean esNombreValido(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        // Nombre debe tener entre 3 y 20 caracteres
        if (nombre.length() < 3 || nombre.length() > 20) {
            return false;
        }

        // Solo permitir letras, números y espacios
        return nombre.matches("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]+$");
    }

    public static boolean esApuestaValida(double apuesta, double saldo, double minimo, double maximo) {
        if (apuesta <= 0) {
            return false;
        }

        if (apuesta > saldo) {
            return false;
        }

        if (apuesta < minimo || apuesta > maximo) {
            return false;
        }

        return true;
    }

    public static boolean esSaldoValido(double saldo) {
        return saldo >= 0;
    }
}
