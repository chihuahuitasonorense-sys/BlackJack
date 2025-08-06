package com.blackjack;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.blackjack.database.DatabaseManager;
import com.blackjack.utils.Logger;

/**
 * Clase principal de la aplicación BlackJack
 * Implementa el patrón Singleton para la gestión de la aplicación
 */
public class App extends Application {

    private static App instance;
    private Stage primaryStage;
    private DatabaseManager databaseManager;

    @Override
    public void start(Stage primaryStage) {
        try {
            instance = this;
            this.primaryStage = primaryStage;

            // Inicializar base de datos
            databaseManager = DatabaseManager.getInstance();
            databaseManager.inicializarBaseDatos();

            // Cargar vista principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuPrincipal.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 900, 700);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            primaryStage.setTitle("BlackJack - Casino");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            Logger.log("Aplicación iniciada correctamente");

        } catch (Exception e) {
            Logger.logError("Error al iniciar la aplicación", e);
            mostrarError("Error fatal al iniciar la aplicación");
        }
    }

    public static App getInstance() {
        return instance;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void cambiarEscena(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            primaryStage.getScene().setRoot(root);
        } catch (Exception e) {
            Logger.logError("Error al cambiar escena: " + fxmlFile, e);
            mostrarError("Error al cargar la pantalla");
        }
    }

    private void mostrarError(String mensaje) {
        // Implementar diálogo de error
        System.err.println(mensaje);
    }

    public static void main(String[] args) {
        launch(args);
    }
}