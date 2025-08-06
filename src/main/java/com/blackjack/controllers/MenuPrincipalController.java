package com.blackjack.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.blackjack.App;
import com.blackjack.services.GameService;
import com.blackjack.utils.Validator;
import com.blackjack.utils.Logger;

/**
 * Controlador para el menú principal
 * Principio ISP: Interfaz segregada para cada vista
 */
public class MenuPrincipalController {

    @FXML private Label lblTitulo;
    @FXML private Button btnJugar;
    @FXML private Button btnReglas;
    @FXML private Button btnSalir;
    @FXML private TextField txtNombre;
    @FXML private Label lblError;

    private GameService gameService;

    @FXML
    public void initialize() {
        gameService = GameService.getInstance();
        lblError.setVisible(false);

        // Configurar eventos
        btnJugar.setOnAction(e -> iniciarJuego());
        btnReglas.setOnAction(e -> mostrarReglas());
        btnSalir.setOnAction(e -> salir());
    }

    private void iniciarJuego() {
        String nombre = txtNombre.getText().trim();

        if (!Validator.esNombreValido(nombre)) {
            mostrarError("Por favor ingrese un nombre válido (3-20 caracteres)");
            return;
        }

        try {
            gameService.iniciarJugador(nombre);

            // Cambiar a la pantalla del juego
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PantallaJuego.fxml"));
            Parent root = loader.load();

            Scene scene = btnJugar.getScene();
            scene.setRoot(root);

        } catch (Exception e) {
            Logger.logError("Error al iniciar juego", e);
            mostrarError("Error al iniciar el juego: " + e.getMessage());
        }
    }

    private void mostrarReglas() {
        try {
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Reglas del BlackJack");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnReglas.getScene().getWindow());

            VBox vbox = new VBox(10);
            vbox.setStyle("-fx-padding: 20;");

            TextArea textArea = new TextArea();
            textArea.setText(obtenerReglas());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefSize(500, 400);

            Button btnCerrar = new Button("Cerrar");
            btnCerrar.setOnAction(e -> dialogStage.close());

            vbox.getChildren().addAll(textArea, btnCerrar);

            Scene scene = new Scene(vbox);
            dialogStage.setScene(scene);
            dialogStage.show();

        } catch (Exception e) {
            Logger.logError("Error al mostrar reglas", e);
        }
    }

    private String obtenerReglas() {
        return "REGLAS DEL BLACKJACK\n\n" +
                "OBJETIVO:\n" +
                "Conseguir 21 puntos o acercarse lo más posible sin pasarse.\n\n" +

                "VALOR DE LAS CARTAS:\n" +
                "• Cartas numéricas (2-10): Su valor nominal\n" +
                "• J, Q, K: Valen 10 puntos\n" +
                "• As: Vale 1 u 11 puntos (lo más conveniente)\n\n" +

                "DESARROLLO DEL JUEGO:\n" +
                "1. Realiza tu apuesta (mínimo $10, máximo $1000)\n" +
                "2. Recibes 2 cartas iniciales\n" +
                "3. El dealer recibe 2 cartas (una oculta)\n" +
                "4. Puedes pedir más cartas o plantarte\n" +
                "5. El dealer juega después (debe pedir con 16 o menos)\n\n" +

                "OPCIONES DE JUEGO:\n" +
                "• PEDIR: Recibir otra carta\n" +
                "• PLANTARSE: Mantener tu mano actual\n" +
                "• DOBLAR: Doblar apuesta y recibir solo una carta más\n" +
                "• DIVIDIR: Si tienes dos cartas iguales, puedes dividir\n\n" +

                "BLACKJACK:\n" +
                "Si obtienes 21 con las dos primeras cartas, es BLACKJACK.\n" +
                "Paga 3:2 (recibes 1.5 veces tu apuesta)\n\n" +

                "RESULTADOS:\n" +
                "• Ganas si tu mano es mayor que la del dealer sin pasarte de 21\n" +
                "• Pierdes si te pasas de 21 o el dealer tiene mejor mano\n" +
                "• Empatas si ambos tienen el mismo valor\n\n" +

                "¡BUENA SUERTE!";
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }

    private void salir() {
        System.exit(0);
    }
}
