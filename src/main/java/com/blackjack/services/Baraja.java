package com.blackjack.services;

import com.blackjack.models.Carta;
import java.util.*;

/**
 * Servicio para gestionar la baraja de cartas
 * Principio DIP: Depende de abstracciones, no de concreciones
 */
public class Baraja {

    private List<Carta> cartas;
    private Random random;

    public Baraja() {
        this.random = new Random();
        inicializarBaraja();
    }

    private void inicializarBaraja() {
        cartas = new ArrayList<>();

        for (Carta.Palo palo : Carta.Palo.values()) {
            for (Carta.Valor valor : Carta.Valor.values()) {
                cartas.add(new Carta(palo, valor));
            }
        }

        barajar();
    }

    public void barajar() {
        Collections.shuffle(cartas, random);
    }

    public Carta sacarCarta() {
        if (cartas.isEmpty()) {
            inicializarBaraja();
        }
        return cartas.remove(cartas.size() - 1);
    }

    public int cartasRestantes() {
        return cartas.size();
    }

    public void reiniciar() {
        inicializarBaraja();
    }
}
