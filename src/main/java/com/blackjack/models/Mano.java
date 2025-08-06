package com.blackjack.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa una mano de cartas
 * Principio OCP: Abierto para extensión, cerrado para modificación
 */
public class Mano {

    private List<Carta> cartas;
    private double apuesta;
    private boolean plantado;
    private boolean blackjack;

    public Mano() {
        this.cartas = new ArrayList<>();
        this.apuesta = 0;
        this.plantado = false;
        this.blackjack = false;
    }

    public void agregarCarta(Carta carta) {
        cartas.add(carta);
        verificarBlackjack();
    }

    public int calcularValor() {
        int valor = 0;
        int ases = 0;

        for (Carta carta : cartas) {
            if (carta.getValor() == Carta.Valor.AS) {
                ases++;
                valor += 11;
            } else {
                valor += carta.getValor().getValorMinimo();
            }
        }

        // Ajustar valor de los ases si es necesario
        while (valor > 21 && ases > 0) {
            valor -= 10;
            ases--;
        }

        return valor;
    }

    public boolean esBusted() {
        return calcularValor() > 21;
    }

    public boolean esBlackjack() {
        return cartas.size() == 2 && calcularValor() == 21;
    }

    private void verificarBlackjack() {
        if (cartas.size() == 2 && calcularValor() == 21) {
            blackjack = true;
        }
    }

    public boolean puedeDoublar() {
        return cartas.size() == 2 && !plantado;
    }

    public boolean puedeDividir() {
        if (cartas.size() != 2) return false;

        Carta.Valor valor1 = cartas.get(0).getValor();
        Carta.Valor valor2 = cartas.get(1).getValor();

        return valor1.getValorMinimo() == valor2.getValorMinimo();
    }

    public void limpiar() {
        cartas.clear();
        apuesta = 0;
        plantado = false;
        blackjack = false;
    }

    // Getters y Setters
    public List<Carta> getCartas() {
        return new ArrayList<>(cartas);
    }

    public double getApuesta() {
        return apuesta;
    }

    public void setApuesta(double apuesta) {
        this.apuesta = apuesta;
    }

    public boolean isPlantado() {
        return plantado;
    }

    public void setPlantado(boolean plantado) {
        this.plantado = plantado;
    }

    public int getCantidadCartas() {
        return cartas.size();
    }
}