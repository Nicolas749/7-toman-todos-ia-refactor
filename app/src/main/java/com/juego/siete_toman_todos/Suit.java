package com.juego.siete_toman_todos;

/** Card suits. Hearts and diamonds are printed in red, spades and clubs in black. */
enum Suit {
    HEARTS(true),
    DIAMONDS(true),
    SPADES(false),
    CLUBS(false);

    final boolean red;

    Suit(boolean red) {
        this.red = red;
    }
}
