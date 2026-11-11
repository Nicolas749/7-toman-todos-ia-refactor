package com.juego.siete_toman_todos;

import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Debug builds only: shows every card design on one screen for visual review.
 * Start with: adb shell am start -n com.juego.siete_toman_todos/.CardGalleryActivity [--es mode detail]
 * {@code --es mode icon} previews the launcher icon and saves 512 px PNGs to the app's files folder.
 */
public class CardGalleryActivity extends AppCompatActivity {

    private static final int[][] DETAIL = {
            {1, Suit.SPADES.ordinal()}, {7, Suit.HEARTS.ordinal()}, {10, Suit.DIAMONDS.ordinal()},
            {11, Suit.CLUBS.ordinal()}, {12, Suit.HEARTS.ordinal()}, {13, Suit.SPADES.ordinal()},
            {5, Suit.CLUBS.ordinal()}, {8, Suit.DIAMONDS.ordinal()}, {0, 0},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ("icon".equals(getIntent().getStringExtra("mode"))) {
            setContentView(IconExport.preview(this));
            return;
        }
        boolean detail = "detail".equals(getIntent().getStringExtra("mode"));
        int columns = detail ? 3 : 7;
        int padding = Math.round(getResources().getDisplayMetrics().density * 4);
        int cell = getResources().getDisplayMetrics().widthPixels / columns;

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(columns);
        if (detail) {
            for (int[] item : DETAIL) {
                grid.addView(card(item[0], Suit.values()[item[1]], cell, padding));
            }
        } else {
            for (CardCatalog.Card card : CardCatalog.CARDS) {
                grid.addView(card(card.rank, card.suit, cell, padding));
            }
            grid.addView(card(0, Suit.SPADES, cell, padding));
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_app);
        scroll.addView(grid);
        setContentView(scroll);
    }

    /** Rank 0 shows the card back. */
    private PlayingCardView card(int rank, Suit suit, int cell, int padding) {
        PlayingCardView view = new PlayingCardView(this);
        if (rank == 0) {
            view.setFaceDown(true);
        } else {
            view.setCard(rank, suit);
        }
        view.setPadding(padding, padding, padding, padding);
        view.setLayoutParams(new GridLayout.LayoutParams(new android.view.ViewGroup.LayoutParams(cell, Math.round(cell * 1.4f))));
        return view;
    }
}
