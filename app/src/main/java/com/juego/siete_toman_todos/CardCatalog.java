package com.juego.siete_toman_todos;

import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The 52 cards and the rule each one triggers. New decks and rules only need to change this data. */
final class CardCatalog {

    static final class Card {
        /** 1 (ace) to 13 (king). */
        final int rank;
        final Suit suit;
        @StringRes final int title;
        @StringRes final int rule;

        Card(int rank, Suit suit, @StringRes int title, @StringRes int rule) {
            this.rank = rank;
            this.suit = suit;
            this.title = title;
            this.rule = rule;
        }
    }

    static final List<Card> CARDS;

    static {
        List<Card> cards = new ArrayList<>(52);
        addRank(cards, 1, R.string.card_as_title, R.string.card_as_rule);
        addRank(cards, 2, R.string.card_dos_title, R.string.card_dos_rule);
        addRank(cards, 3, R.string.card_tres_title, R.string.card_tres_rule);
        addRank(cards, 4, R.string.card_cuatro_title, R.string.card_cuatro_rule);
        addRank(cards, 5, R.string.card_cinco_title, R.string.card_cinco_rule);
        addRank(cards, 6, R.string.card_seis_title, R.string.card_seis_rule);
        addRank(cards, 7, R.string.card_siete_title, R.string.card_siete_rule);
        addRank(cards, 8, R.string.card_ocho_title, R.string.card_ocho_rule);
        addRank(cards, 9, R.string.card_nueve_title, R.string.card_nueve_rule);
        addRank(cards, 10, R.string.card_diez_title, R.string.card_diez_rule);
        addRank(cards, 11, R.string.card_joto_title, R.string.card_joto_rule);
        addRank(cards, 12, R.string.card_reina_title, R.string.card_reina_rule);
        addRank(cards, 13, R.string.card_rey_title, R.string.card_rey_rule);
        CARDS = Collections.unmodifiableList(cards);
    }

    private static void addRank(List<Card> cards, int rank, @StringRes int title, @StringRes int rule) {
        for (Suit suit : Suit.values()) {
            cards.add(new Card(rank, suit, title, rule));
        }
    }

    @StringRes
    static int rankName(int rank) {
        switch (rank) {
            case 1:
                return R.string.rank_ace;
            case 11:
                return R.string.rank_jack;
            case 12:
                return R.string.rank_queen;
            case 13:
                return R.string.rank_king;
            default:
                return 0;
        }
    }

    @StringRes
    static int suitName(Suit suit) {
        switch (suit) {
            case HEARTS:
                return R.string.suit_hearts;
            case DIAMONDS:
                return R.string.suit_diamonds;
            case CLUBS:
                return R.string.suit_clubs;
            default:
                return R.string.suit_spades;
        }
    }

    private CardCatalog() {
    }
}
