package com.blackjack.models;

import com.blackjack.exceptions.SaldoInsuficienteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa un jugador
 * Implementa el patrón Builder para construcción flexible
 */
public class Jugador {

    private Long id;
    private String nombre;
    private double saldo;
    private List<Mano> manos;
    private int manoActual;

    public Jugador(String nombre, double saldoInicial) {
        this.nombre = nombre;
        this.saldo = saldoInicial;
        this.manos = new ArrayList<>();
        this.manos.add(new Mano());
        this.manoActual = 0;
    }

    public void apostar(double cantidad) throws SaldoInsuficienteException {
        if (cantidad > saldo) {
            throw new SaldoInsuficienteException("Saldo insuficiente. Saldo actual: $" + saldo);
        }

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La apuesta debe ser mayor a 0");
        }

        saldo -= cantidad;
        manos.get(manoActual).setApuesta(cantidad);
    }

    public void doblarApuesta() throws SaldoInsuficienteException {
        Mano mano = manos.get(manoActual);
        double apuestaActual = mano.getApuesta();

        if (apuestaActual > saldo) {
            throw new SaldoInsuficienteException("Saldo insuficiente para doblar");
        }

        saldo -= apuestaActual;
        mano.setApuesta(apuestaActual * 2);
    }

    public void dividirMano() throws SaldoInsuficienteException {
        Mano manoOriginal = manos.get(manoActual);

        if (!manoOriginal.puedeDividir()) {
            throw new IllegalStateException("No se puede dividir esta mano");
        }

        double apuestaOriginal = manoOriginal.getApuesta();
        if (apuestaOriginal > saldo) {
            throw new SaldoInsuficienteException("Saldo insuficiente para dividir");
        }

        // Crear nueva mano con la segunda carta
        Mano nuevaMano = new Mano();
        List<Carta> cartasOriginales = manoOriginal.getCartas();
        nuevaMano.agregarCarta(cartasOriginales.get(1));
        nuevaMano.setApuesta(apuestaOriginal);

        // Mantener solo la primera carta en la mano original
        cartasOriginales.remove(1);

        // Restar apuesta del saldo
        saldo -= apuestaOriginal;

        // Agregar la nueva mano
        manos.add(manoActual + 1, nuevaMano);
    }

    public void recibirGanancia(double cantidad) {
        saldo += cantidad;
    }

    public void reiniciarManos() {
        manos.clear();
        manos.add(new Mano());
        manoActual = 0;
    }

    public boolean tieneSiguienteMano() {
        return manoActual < manos.size() - 1;
    }

    public void siguienteMano() {
        if (tieneSiguienteMano()) {
            manoActual++;
        }
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public Mano getManoActual() {
        return manos.get(manoActual);
    }

    public List<Mano> getManos() {
        return manos;
    }

    public int getIndiceManoActual() {
        return manoActual;
    }
}