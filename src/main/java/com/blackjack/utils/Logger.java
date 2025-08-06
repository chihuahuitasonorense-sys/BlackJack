package com.blackjack.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad para registro de eventos y errores
 * Principio SRP: Responsabilidad única de logging
 */
public class Logger {

    private static final String LOG_FILE = "blackjack.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String mensaje) {
        escribirLog("INFO", mensaje);
    }

    public static void logError(String mensaje, Exception e) {
        escribirLog("ERROR", mensaje + " - " + e.getMessage());
        e.printStackTrace();
    }

    private static void escribirLog(String nivel, String mensaje) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(formatter);
            writer.println(String.format("[%s] [%s] %s", timestamp, nivel, mensaje));
        } catch (IOException e) {
            System.err.println("Error al escribir en log: " + e.getMessage());
        }
    }
}
