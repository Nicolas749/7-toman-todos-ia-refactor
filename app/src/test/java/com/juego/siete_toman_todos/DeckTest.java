package com.juego.siete_toman_todos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class DeckTest {

    private static final int SIZE = 52;

    @Test
    public void nonRepeatingDeckShowsEveryCardExactlyOnce() {
        for (long seed = 0; seed < 200; seed++) {
            Deck deck = new Deck(SIZE, false, new Random(seed));
            Set<Integer> seen = new HashSet<>();
            while (deck.hasNext()) {
                assertTrue("repeated card with seed " + seed, seen.add(deck.draw()));
            }
            assertEquals(SIZE, seen.size());
        }
    }

    @Test
    public void remainingCountsCardsStillToCome() {
        Deck deck = new Deck(SIZE, false, new Random(1));
        assertEquals(SIZE, deck.remaining());
        deck.draw();
        assertEquals(51, deck.remaining());
        while (deck.hasNext()) {
            deck.draw();
        }
        assertEquals(0, deck.remaining());
    }

    @Test
    public void drawingFromAnEmptyDeckFails() {
        Deck deck = new Deck(1, false, new Random(1));
        deck.draw();
        assertFalse(deck.hasNext());
        assertThrows(IllegalStateException.class, deck::draw);
    }

    @Test
    public void resetRestoresTheFullDeck() {
        Deck deck = new Deck(SIZE, false, new Random(1));
        while (deck.hasNext()) {
            deck.draw();
        }
        deck.reset();
        assertEquals(SIZE, deck.remaining());
        assertEquals(Deck.NONE, deck.current());
        Set<Integer> seen = new HashSet<>();
        while (deck.hasNext()) {
            seen.add(deck.draw());
        }
        assertEquals(SIZE, seen.size());
    }

    @Test
    public void restoredDeckContinuesWithoutRepeatingCards() {
        Deck deck = new Deck(SIZE, false, new Random(1));
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            seen.add(deck.draw());
        }
        Deck restored = Deck.restore(deck.order(), deck.drawn(), deck.current(), false, new Random(99));
        assertEquals(deck.current(), restored.current());
        assertEquals(32, restored.remaining());
        while (restored.hasNext()) {
            assertTrue(seen.add(restored.draw()));
        }
        assertEquals(SIZE, seen.size());
    }

    @Test
    public void repeatingDeckNeverRunsOutAndCanRepeat() {
        Deck deck = new Deck(SIZE, true, new Random(1));
        Set<Integer> seen = new HashSet<>();
        int draws = SIZE * 20;
        for (int i = 0; i < draws; i++) {
            assertTrue(deck.hasNext());
            int card = deck.draw();
            assertTrue(card >= 0 && card < SIZE);
            seen.add(card);
        }
        assertTrue(seen.size() < draws);
    }
}
