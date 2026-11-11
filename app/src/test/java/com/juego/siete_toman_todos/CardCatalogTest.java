package com.juego.siete_toman_todos;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CardCatalogTest {

    @Test
    public void catalogHas52DifferentCards() {
        Set<String> cards = new HashSet<>();
        for (CardCatalog.Card card : CardCatalog.CARDS) {
            cards.add(card.rank + "-" + card.suit);
        }
        assertEquals(52, CardCatalog.CARDS.size());
        assertEquals(52, cards.size());
    }

    @Test
    public void everyRuleAppearsOnFourCards() {
        Map<Integer, Integer> countByRule = new HashMap<>();
        for (CardCatalog.Card card : CardCatalog.CARDS) {
            countByRule.merge(card.rule, 1, Integer::sum);
        }
        assertEquals(13, countByRule.size());
        for (int count : countByRule.values()) {
            assertEquals(4, count);
        }
    }

    @Test
    public void ranksShowTheirOwnRule() {
        for (CardCatalog.Card card : CardCatalog.CARDS) {
            if (card.rank == 7) {
                assertEquals(R.string.card_siete_rule, card.rule);
            } else if (card.rank == 11) {
                assertEquals(R.string.card_joto_rule, card.rule);
            } else if (card.rank == 12) {
                assertEquals(R.string.card_reina_rule, card.rule);
            } else if (card.rank == 13) {
                assertEquals(R.string.card_rey_rule, card.rule);
            }
        }
    }

    @Test
    public void everySuitHasThirteenRanks() {
        Map<Suit, Integer> countBySuit = new HashMap<>();
        for (CardCatalog.Card card : CardCatalog.CARDS) {
            countBySuit.merge(card.suit, 1, Integer::sum);
        }
        for (Suit suit : Suit.values()) {
            assertEquals(Integer.valueOf(13), countBySuit.get(suit));
        }
    }
}
