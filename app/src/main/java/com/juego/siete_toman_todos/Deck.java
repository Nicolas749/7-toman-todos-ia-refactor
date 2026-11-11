package com.juego.siete_toman_todos;

import java.util.Random;

/**
 * Draws card positions either from a shuffled deck (each card exactly once) or, in repeating mode,
 * independently at random with no end. Has no Android dependencies so it can be unit tested.
 */
final class Deck {

    /** Value of {@link #current()} before the first draw. */
    static final int NONE = -1;

    private final int[] order;
    private final boolean repeating;
    private final Random random;
    private int drawn;
    private int current = NONE;

    Deck(int size, boolean repeating, Random random) {
        if (size <= 0) {
            throw new IllegalArgumentException("A deck needs at least one card");
        }
        this.order = new int[size];
        this.repeating = repeating;
        this.random = random;
        reset();
    }

    /** Rebuilds a deck from values saved with {@link #order()}, {@link #drawn()} and {@link #current()}. */
    static Deck restore(int[] order, int drawn, int current, boolean repeating, Random random) {
        Deck deck = new Deck(order.length, repeating, random);
        System.arraycopy(order, 0, deck.order, 0, order.length);
        deck.drawn = drawn;
        deck.current = current;
        return deck;
    }

    /** Puts every card back and shuffles. */
    void reset() {
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        for (int i = order.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int swapped = order[i];
            order[i] = order[j];
            order[j] = swapped;
        }
        drawn = 0;
        current = NONE;
    }

    boolean hasNext() {
        return repeating || drawn < order.length;
    }

    /** Returns the next card position. In non-repeating mode, check {@link #hasNext()} first. */
    int draw() {
        if (!hasNext()) {
            throw new IllegalStateException("The deck is empty");
        }
        current = repeating ? random.nextInt(order.length) : order[drawn];
        drawn++;
        return current;
    }

    /** Cards still to come, not counting the current one. Not meaningful in repeating mode. */
    int remaining() {
        return order.length - drawn;
    }

    int current() {
        return current;
    }

    int drawn() {
        return drawn;
    }

    int[] order() {
        return order.clone();
    }
}
