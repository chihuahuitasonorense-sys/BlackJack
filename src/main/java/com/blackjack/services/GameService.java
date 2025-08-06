package com.blackjack.services;

import com.blackjack.models.*;
import com.blackjack.exceptions.*;
import com.blackjack.database.DatabaseManager;
import com.blackjack.utils.Logger;

/**
 * Servicio principal que gestiona la lógica del juego
 * Implementa el patrón Facade para simplificar la interacción
 */
public class GameService {

    private static GameService instance;

    private Jugador jugador;
    private Dealer dealer;
    private Baraja baraja;
    private EstadoJuego estadoActual;
    private DatabaseManager dbManager;
    private double apuestaMinima = 10;
    private double apuestaMaxima = 1000;

    public enum EstadoJuego {
        ESPERANDO_APUESTA,
        JUGANDO,
        TURNO_DEALER,
        FINALIZADO
    }

    private GameService() {
        this.baraja = new Baraja();
        this.dealer = new Dealer();
        this.dbManager = DatabaseManager.getInstance();
        this.estadoActual = EstadoJuego.ESPERANDO_APUESTA;
    }

    public static GameService getInstance() {
        if (instance == null) {
            instance = new GameService();
        }
        return instance;
    }

    public void iniciarJugador(String nombre) throws GameException {
        try {
            // Buscar jugador en la base de datos o crear uno nuevo
            jugador = dbManager.obtenerJugador(nombre);

            if (jugador == null) {
                jugador = new Jugador(nombre, 1000); // Saldo inicial
                dbManager.guardarJugador(jugador);
            }

            Logger.log("Jugador iniciado: " + nombre + " - Saldo: $" + jugador.getSaldo());

        } catch (Exception e) {
            Logger.logError("Error al iniciar jugador", e);
            throw new GameException("Error al cargar datos del jugador", e);
        }
    }

    public void realizarApuesta(double cantidad) throws SaldoInsuficienteException, GameException {
        if (estadoActual != EstadoJuego.ESPERANDO_APUESTA) {
            throw new GameException("No es momento de apostar");
        }

        if (cantidad < apuestaMinima || cantidad > apuestaMaxima) {
            throw new GameException(String.format("La apuesta debe estar entre $%.2f y $%.2f",
                    apuestaMinima, apuestaMaxima));
        }

        jugador.apostar(cantidad);
        estadoActual = EstadoJuego.JUGANDO;

        // Repartir cartas iniciales
        repartirCartasIniciales();

        Logger.log("Apuesta realizada: $" + cantidad);
    }

    private void repartirCartasIniciales() {
        // Limpiar manos anteriores
        jugador.reiniciarManos();
        dealer.reiniciarMano();

        // Repartir 2 cartas al jugador
        jugador.getManoActual().agregarCarta(baraja.sacarCarta());
        jugador.getManoActual().agregarCarta(baraja.sacarCarta());

        // Repartir 2 cartas al dealer (una boca abajo)
        Carta primeraCarta = baraja.sacarCarta();
        dealer.getMano().agregarCarta(primeraCarta);

        Carta segundaCarta = baraja.sacarCarta();
        segundaCarta.setBocaAbajo(true);
        dealer.getMano().agregarCarta(segundaCarta);

        // Verificar blackjack inmediato
        verificarBlackjackInicial();
    }

    private void verificarBlackjackInicial() {
        boolean jugadorBlackjack = jugador.getManoActual().esBlackjack();
        boolean dealerBlackjack = dealer.getMano().esBlackjack();

        if (jugadorBlackjack && dealerBlackjack) {
            // Empate
            finalizarRonda(ResultadoRonda.EMPATE);
        } else if (jugadorBlackjack) {
            // Jugador gana con blackjack (pago 3:2)
            finalizarRonda(ResultadoRonda.BLACKJACK);
        } else if (dealerBlackjack) {
            // Dealer gana con blackjack
            revelarCartaDealer();
            finalizarRonda(ResultadoRonda.PIERDE);
        }
    }

    public void pedir() throws GameException {
        if (estadoActual != EstadoJuego.JUGANDO) {
            throw new GameException("No puedes pedir carta en este momento");
        }

        Mano manoActual = jugador.getManoActual();

        if (manoActual.isPlantado()) {
            throw new GameException("Esta mano ya está plantada");
        }

        manoActual.agregarCarta(baraja.sacarCarta());

        if (manoActual.esBusted()) {
            manoActual.setPlantado(true);

            if (jugador.tieneSiguienteMano()) {
                jugador.siguienteMano();
            } else {
                // Todas las manos del jugador terminadas
                if (todasLasManoBusted()) {
                    finalizarRonda(ResultadoRonda.PIERDE);
                } else {
                    turnoDealer();
                }
            }
        }

        Logger.log("Carta pedida. Valor actual: " + manoActual.calcularValor());
    }

    public void plantarse() throws GameException {
        if (estadoActual != EstadoJuego.JUGANDO) {
            throw new GameException("No puedes plantarte en este momento");
        }

        Mano manoActual = jugador.getManoActual();
        manoActual.setPlantado(true);

        if (jugador.tieneSiguienteMano()) {
            jugador.siguienteMano();
        } else {
            turnoDealer();
        }

        Logger.log("Jugador se planta con: " + manoActual.calcularValor());
    }

    public void doblar() throws SaldoInsuficienteException, GameException {
        if (estadoActual != EstadoJuego.JUGANDO) {
            throw new GameException("No puedes doblar en este momento");
        }

        Mano manoActual = jugador.getManoActual();

        if (!manoActual.puedeDoublar()) {
            throw new GameException("Solo puedes doblar con las dos primeras cartas");
        }

        jugador.doblarApuesta();
        manoActual.agregarCarta(baraja.sacarCarta());
        manoActual.setPlantado(true);

        if (manoActual.esBusted()) {
            if (jugador.tieneSiguienteMano()) {
                jugador.siguienteMano();
            } else if (todasLasManoBusted()) {
                finalizarRonda(ResultadoRonda.PIERDE);
            } else {
                turnoDealer();
            }
        } else {
            if (jugador.tieneSiguienteMano()) {
                jugador.siguienteMano();
            } else {
                turnoDealer();
            }
        }

        Logger.log("Apuesta doblada. Nueva apuesta: $" + manoActual.getApuesta());
    }

    public void dividir() throws SaldoInsuficienteException, GameException {
        if (estadoActual != EstadoJuego.JUGANDO) {
            throw new GameException("No puedes dividir en este momento");
        }

        Mano manoActual = jugador.getManoActual();

        if (!manoActual.puedeDividir()) {
            throw new GameException("No puedes dividir esta mano");
        }

        jugador.dividirMano();

        // Dar una carta adicional a cada mano dividida
        jugador.getManoActual().agregarCarta(baraja.sacarCarta());
        jugador.getManos().get(jugador.getIndiceManoActual() + 1).agregarCarta(baraja.sacarCarta());

        Logger.log("Mano dividida");
    }

    private void turnoDealer() {
        estadoActual = EstadoJuego.TURNO_DEALER;
        revelarCartaDealer();

        // El dealer debe pedir hasta tener 17 o más
        while (dealer.getMano().calcularValor() < 17) {
            dealer.getMano().agregarCarta(baraja.sacarCarta());
        }

        evaluarResultados();
    }

    private void revelarCartaDealer() {
        for (Carta carta : dealer.getMano().getCartas()) {
            carta.setBocaAbajo(false);
        }
    }

    private void evaluarResultados() {
        int valorDealer = dealer.getMano().calcularValor();
        boolean dealerBusted = dealer.getMano().esBusted();

        double gananciasTotal = 0;

        for (Mano mano : jugador.getManos()) {
            if (mano.esBusted()) {
                // Jugador pierde esta mano
                continue;
            }

            int valorJugador = mano.calcularValor();
            double apuesta = mano.getApuesta();

            if (dealerBusted || valorJugador > valorDealer) {
                // Jugador gana
                if (mano.esBlackjack()) {
                    gananciasTotal += apuesta * 2.5; // Pago 3:2 para blackjack
                } else {
                    gananciasTotal += apuesta * 2; // Pago 1:1 normal
                }
            } else if (valorJugador == valorDealer) {
                // Empate
                gananciasTotal += apuesta; // Devolver apuesta
            }
            // Si pierde, no recibe nada
        }

        if (gananciasTotal > 0) {
            jugador.recibirGanancia(gananciasTotal);

            double gananciaLimpia = gananciasTotal - getTotalApostado();
            if (gananciaLimpia > 0) {
                finalizarRonda(ResultadoRonda.GANA);
            } else if (gananciaLimpia == 0) {
                finalizarRonda(ResultadoRonda.EMPATE);
            } else {
                finalizarRonda(ResultadoRonda.PIERDE);
            }
        } else {
            finalizarRonda(ResultadoRonda.PIERDE);
        }
    }

    private double getTotalApostado() {
        double total = 0;
        for (Mano mano : jugador.getManos()) {
            total += mano.getApuesta();
        }
        return total;
    }

    private boolean todasLasManoBusted() {
        for (Mano mano : jugador.getManos()) {
            if (!mano.esBusted()) {
                return false;
            }
        }
        return true;
    }

    private void finalizarRonda(ResultadoRonda resultado) {
        estadoActual = EstadoJuego.FINALIZADO;

        // Actualizar saldo en base de datos
        try {
            dbManager.actualizarSaldo(jugador);
            Logger.log("Ronda finalizada: " + resultado + " - Nuevo saldo: $" + jugador.getSaldo());
        } catch (Exception e) {
            Logger.logError("Error al actualizar saldo en BD", e);
        }
    }

    public void nuevaRonda() {
        baraja.barajar();
        jugador.reiniciarManos();
        dealer.reiniciarMano();
        estadoActual = EstadoJuego.ESPERANDO_APUESTA;
    }

    public enum ResultadoRonda {
        GANA, PIERDE, EMPATE, BLACKJACK
    }

    // Getters
    public Jugador getJugador() {
        return jugador;
    }

    public Dealer getDealer() {
        return dealer;
    }

    public EstadoJuego getEstadoActual() {
        return estadoActual;
    }

    public double getApuestaMinima() {
        return apuestaMinima;
    }

    public double getApuestaMaxima() {
        return apuestaMaxima;
    }
}
