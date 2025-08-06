package com.blackjack.models;

/**
 * Clase que representa al dealer
 * Extiende el comportamiento básico del jugador
 */
public class Dealer {

    private Mano mano;

    public Dealer() {
        this.mano = new Mano();
    }

    public Mano getMano() {
        return mano;
    }

    public void reiniciarMano() {
        mano.limpiar();
    }

    public boolean debeTomarCarta() {
        int valor = mano.calcularValor();
        // El dealer debe tomar carta si tiene 16 o menos
        // y debe plantarse con 17 o más
        return valor < 17;
    }

    public Carta getCartaVisible() {
        if (mano.getCartas().size() > 0) {
            return mano.getCartas().get(0);
        }
        return null;
    }
}
