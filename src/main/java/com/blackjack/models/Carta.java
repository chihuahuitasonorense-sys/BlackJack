package com.blackjack.models;

/**
 * Clase que representa una carta del juego
 * Principio SRP: Responsabilidad única de representar una carta
 */
public class Carta {

    public enum Palo {
        CORAZONES("♥"), DIAMANTES("♦"), TREBOLES("♣"), PICAS("♠");

        private final String simbolo;

        Palo(String simbolo) {
            this.simbolo = simbolo;
        }

        public String getSimbolo() {
            return simbolo;
        }
    }

    public enum Valor {
        AS(1, 11), DOS(2), TRES(3), CUATRO(4), CINCO(5),
        SEIS(6), SIETE(7), OCHO(8), NUEVE(9), DIEZ(10),
        JOTA(10), REINA(10), REY(10);

        private final int valorMinimo;
        private final int valorMaximo;

        Valor(int valor) {
            this.valorMinimo = valor;
            this.valorMaximo = valor;
        }

        Valor(int valorMinimo, int valorMaximo) {
            this.valorMinimo = valorMinimo;
            this.valorMaximo = valorMaximo;
        }

        public int getValorMinimo() {
            return valorMinimo;
        }

        public int getValorMaximo() {
            return valorMaximo;
        }
    }

    private final Palo palo;
    private final Valor valor;
    private boolean bocaAbajo;

    public Carta(Palo palo, Valor valor) {
        this.palo = palo;
        this.valor = valor;
        this.bocaAbajo = false;
    }

    public Palo getPalo() {
        return palo;
    }

    public Valor getValor() {
        return valor;
    }

    public boolean isBocaAbajo() {
        return bocaAbajo;
    }

    public void setBocaAbajo(boolean bocaAbajo) {
        this.bocaAbajo = bocaAbajo;
    }

    public String getRepresentacion() {
        if (bocaAbajo) {
            return "??";
        }

        String valorStr;
        switch (valor) {
            case AS: valorStr = "A"; break;
            case JOTA: valorStr = "J"; break;
            case REINA: valorStr = "Q"; break;
            case REY: valorStr = "K"; break;
            default: valorStr = String.valueOf(valor.getValorMinimo());
        }

        return valorStr + palo.getSimbolo();
    }

    @Override
    public String toString() {
        return getRepresentacion();
    }
}

