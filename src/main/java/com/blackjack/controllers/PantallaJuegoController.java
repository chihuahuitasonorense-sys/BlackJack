package com.blackjack.controllers;

import com.blackjack.App;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.Optional;
import com.blackjack.models.*;
import com.blackjack.services.GameService;
import com.blackjack.exceptions.*;
import com.blackjack.utils.Logger;

/**
 * Controlador principal del juego
 * Gestiona toda la interfaz durante la partida
 */
public class PantallaJuegoController {

    @FXML private Label lblNombreJugador;
    @FXML private Label lblSaldo;
    @FXML private Label lblApuesta;
    @FXML private Label lblValorJugador;
    @FXML private Label lblValorDealer;
    @FXML private Label lblMensaje;

    @FXML private HBox hboxCartasJugador;
    @FXML private HBox hboxCartasDealer;

    @FXML private Button btnPedir;
    @FXML private Button btnPlantarse;
    @FXML private Button btnDoblar;
    @FXML private Button btnDividir;
    @FXML private Button btnNuevaRonda;
    @FXML private Button btnApostar;
    @FXML private Button btnVolver;

    @FXML private TextField txtApuesta;
    @FXML private VBox vboxApuesta;

    private GameService gameService;

    @FXML
    public void initialize() {
        gameService = GameService.getInstance();

        configurarEventos();
        actualizarInterfaz();

        // Mostrar panel de apuesta
        mostrarPanelApuesta();
    }

    private void configurarEventos() {
        btnApostar.setOnAction(e -> realizarApuesta());
        btnPedir.setOnAction(e -> pedir());
        btnPlantarse.setOnAction(e -> plantarse());
        btnDoblar.setOnAction(e -> doblar());
        btnDividir.setOnAction(e -> dividir());
        btnNuevaRonda.setOnAction(e -> nuevaRonda());
        btnVolver.setOnAction(e -> volverAlMenu());
    }

    private void realizarApuesta() {
        try {
            double apuesta = Double.parseDouble(txtApuesta.getText());
            gameService.realizarApuesta(apuesta);

            ocultarPanelApuesta();
            actualizarInterfaz();
            habilitarBotonesJuego();

            // Verificar blackjack inicial
            if (gameService.getEstadoActual() == GameService.EstadoJuego.FINALIZADO) {
                finalizarRonda();
            }

        } catch (NumberFormatException e) {
            mostrarMensaje("Por favor ingrese una apuesta válida", true);
        } catch (SaldoInsuficienteException e) {
            mostrarMensaje(e.getMessage(), true);
        } catch (GameException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    private void pedir() {
        try {
            gameService.pedir();
            actualizarInterfaz();

            if (gameService.getEstadoActual() == GameService.EstadoJuego.FINALIZADO ||
                    gameService.getEstadoActual() == GameService.EstadoJuego.TURNO_DEALER) {
                finalizarRonda();
            }

        } catch (GameException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    private void plantarse() {
        try {
            gameService.plantarse();
            actualizarInterfaz();

            if (gameService.getEstadoActual() == GameService.EstadoJuego.TURNO_DEALER) {
                // Mostrar animación del dealer
                Platform.runLater(() -> {
                    actualizarInterfaz();
                    finalizarRonda();
                });
            }

        } catch (GameException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    private void doblar() {
        try {
            gameService.doblar();
            actualizarInterfaz();

            if (gameService.getEstadoActual() == GameService.EstadoJuego.FINALIZADO ||
                    gameService.getEstadoActual() == GameService.EstadoJuego.TURNO_DEALER) {
                finalizarRonda();
            }

        } catch (SaldoInsuficienteException e) {
            mostrarMensaje(e.getMessage(), true);
        } catch (GameException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    private void dividir() {
        try {
            gameService.dividir();
            actualizarInterfaz();

        } catch (SaldoInsuficienteException e) {
            mostrarMensaje(e.getMessage(), true);
        } catch (GameException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    private void actualizarInterfaz() {
        Jugador jugador = gameService.getJugador();

        if (jugador != null) {
            lblNombreJugador.setText(jugador.getNombre());
            lblSaldo.setText(String.format("Saldo: $%.2f", jugador.getSaldo()));

            // Actualizar cartas y valores
            actualizarCartasJugador();
            actualizarCartasDealer();

            // Actualizar botones según el estado
            actualizarBotones();
        }
    }

    private void actualizarCartasJugador() {
        hboxCartasJugador.getChildren().clear();

        Jugador jugador = gameService.getJugador();

        for (int i = 0; i < jugador.getManos().size(); i++) {
            Mano mano = jugador.getManos().get(i);

            VBox vboxMano = new VBox(5);
            vboxMano.setAlignment(Pos.CENTER);

            // Indicar mano actual
            if (i == jugador.getIndiceManoActual()) {
                vboxMano.setStyle("-fx-border-color: yellow; -fx-border-width: 2;");
            }

            HBox hboxCartas = new HBox(5);

            for (Carta carta : mano.getCartas()) {
                Label lblCarta = crearLabelCarta(carta);
                hboxCartas.getChildren().add(lblCarta);
            }

            Label lblValor = new Label("Valor: " + mano.calcularValor());
            lblValor.setTextFill(Color.WHITE);

            if (mano.esBusted()) {
                lblValor.setText("¡PASADO! (" + mano.calcularValor() + ")");
                lblValor.setTextFill(Color.RED);
            } else if (mano.esBlackjack()) {
                lblValor.setText("¡BLACKJACK!");
                lblValor.setTextFill(Color.GOLD);
            }

            vboxMano.getChildren().addAll(hboxCartas, lblValor);
            hboxCartasJugador.getChildren().add(vboxMano);
        }

        // Actualizar valor total
        Mano manoActual = jugador.getManoActual();
        lblValorJugador.setText("Valor: " + manoActual.calcularValor());
        lblApuesta.setText(String.format("Apuesta: $%.2f", manoActual.getApuesta()));
    }

    private void actualizarCartasDealer() {
        hboxCartasDealer.getChildren().clear();

        Dealer dealer = gameService.getDealer();
        Mano manoDealer = dealer.getMano();

        for (Carta carta : manoDealer.getCartas()) {
            Label lblCarta = crearLabelCarta(carta);
            hboxCartasDealer.getChildren().add(lblCarta);
        }

        // Actualizar valor del dealer
        if (gameService.getEstadoActual() == GameService.EstadoJuego.TURNO_DEALER ||
                gameService.getEstadoActual() == GameService.EstadoJuego.FINALIZADO) {

            lblValorDealer.setText("Valor: " + manoDealer.calcularValor());

            if (manoDealer.esBusted()) {
                lblValorDealer.setText("¡PASADO! (" + manoDealer.calcularValor() + ")");
                lblValorDealer.setTextFill(Color.RED);
            }
        } else {
            // Solo mostrar valor de la carta visible
            Carta cartaVisible = dealer.getCartaVisible();
            if (cartaVisible != null) {
                lblValorDealer.setText("Mostrando: " + cartaVisible.getValor().getValorMinimo());
            }
        }
    }

    private Label crearLabelCarta(Carta carta) {
        Label label = new Label();

        if (carta.isBocaAbajo()) {
            label.setText("??");
            label.setStyle("-fx-background-color: #2C3E50; -fx-text-fill: white; " +
                    "-fx-padding: 10; -fx-border-color: white; -fx-border-width: 1;");
        } else {
            label.setText(carta.getRepresentacion());

            String color = (carta.getPalo() == Carta.Palo.CORAZONES ||
                    carta.getPalo() == Carta.Palo.DIAMANTES) ? "red" : "black";

            label.setStyle("-fx-background-color: white; -fx-text-fill: " + color + "; " +
                    "-fx-padding: 10; -fx-border-color: black; -fx-border-width: 1; " +
                    "-fx-font-size: 20; -fx-font-weight: bold;");
        }

        label.setMinSize(60, 80);
        label.setMaxSize(60, 80);
        label.setAlignment(Pos.CENTER);

        return label;
    }

    private void actualizarBotones() {
        GameService.EstadoJuego estado = gameService.getEstadoActual();

        if (estado == GameService.EstadoJuego.JUGANDO) {
            Mano manoActual = gameService.getJugador().getManoActual();

            btnPedir.setDisable(manoActual.isPlantado());
            btnPlantarse.setDisable(manoActual.isPlantado());
            btnDoblar.setDisable(!manoActual.puedeDoublar());
            btnDividir.setDisable(!manoActual.puedeDividir());
            btnNuevaRonda.setDisable(true);

        } else if (estado == GameService.EstadoJuego.FINALIZADO) {
            deshabilitarBotonesJuego();
            btnNuevaRonda.setDisable(false);

        } else {
            deshabilitarBotonesJuego();
        }
    }

    private void habilitarBotonesJuego() {
        btnPedir.setDisable(false);
        btnPlantarse.setDisable(false);
        actualizarBotones();
    }

    private void deshabilitarBotonesJuego() {
        btnPedir.setDisable(true);
        btnPlantarse.setDisable(true);
        btnDoblar.setDisable(true);
        btnDividir.setDisable(true);
    }

    private void mostrarPanelApuesta() {
        vboxApuesta.setVisible(true);
        deshabilitarBotonesJuego();
        btnNuevaRonda.setDisable(true);

        txtApuesta.setText("");
        txtApuesta.requestFocus();
    }

    private void ocultarPanelApuesta() {
        vboxApuesta.setVisible(false);
    }

    private void finalizarRonda() {
        actualizarInterfaz();

        // Determinar resultado
        String mensaje = determinarMensajeResultado();
        mostrarMensaje(mensaje, false);

        btnNuevaRonda.setDisable(false);
    }

    private String determinarMensajeResultado() {
        Jugador jugador = gameService.getJugador();
        Dealer dealer = gameService.getDealer();

        int valorJugador = jugador.getManoActual().calcularValor();
        int valorDealer = dealer.getMano().calcularValor();

        if (jugador.getManoActual().esBusted()) {
            return "¡Te pasaste! Pierdes la ronda.";
        } else if (dealer.getMano().esBusted()) {
            return "¡El dealer se pasó! ¡GANASTE!";
        } else if (jugador.getManoActual().esBlackjack() && !dealer.getMano().esBlackjack()) {
            return "¡BLACKJACK! ¡Ganaste con pago 3:2!";
        } else if (valorJugador > valorDealer) {
            return "¡GANASTE! Tu " + valorJugador + " vence al " + valorDealer + " del dealer";
        } else if (valorJugador < valorDealer) {
            return "Perdiste. El " + valorDealer + " del dealer vence a tu " + valorJugador;
        } else {
            return "¡EMPATE! Ambos tienen " + valorJugador;
        }
    }

    private void nuevaRonda() {
        gameService.nuevaRonda();
        lblMensaje.setText("");
        actualizarInterfaz();
        mostrarPanelApuesta();
    }

    private void volverAlMenu() {
        try {
            App.getInstance().cambiarEscena("/fxml/MenuPrincipal.fxml");
        } catch (Exception e) {
            Logger.logError("Error al volver al menú", e);
        }
    }

    private void mostrarMensaje(String mensaje, boolean esError) {
        lblMensaje.setText(mensaje);
        lblMensaje.setTextFill(esError ? Color.RED : Color.GREEN);
    }
}
