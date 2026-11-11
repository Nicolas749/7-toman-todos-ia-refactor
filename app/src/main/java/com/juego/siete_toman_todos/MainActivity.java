package com.juego.siete_toman_todos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;

public class MainActivity extends AppCompatActivity {

    /** Kept from the first version so players keep their saved choice. True means random mode. */
    private static final String PREF_REPEAT_CARDS = "switch_state";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FullScreen.enableEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        FullScreen.lockPortraitOnPhones(this);
        setContentView(R.layout.activity_main);
        // The menu keeps the system bars visible; only the game itself goes full screen.
        FullScreen.keepContentInSafeArea(findViewById(R.id.mainRoot));

        showTitle();
        setUpCardFan();

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        MaterialButtonToggleGroup modeToggle = findViewById(R.id.modeToggle);
        TextView modeHelp = findViewById(R.id.modeHelpTxt);
        // At large system text sizes the labels wrap to two lines instead of being cut off.
        for (int i = 0; i < modeToggle.getChildCount(); i++) {
            TextView button = (TextView) modeToggle.getChildAt(i);
            button.setSingleLine(false);
            button.setMaxLines(2);
            button.setEllipsize(null);
        }
        boolean repeatCards = preferences.getBoolean(PREF_REPEAT_CARDS, false);
        modeToggle.check(repeatCards ? R.id.modeRandom : R.id.modeFullDeck);
        modeHelp.setText(repeatCards ? R.string.home_mode_random_help : R.string.home_mode_full_help);
        modeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            boolean random = checkedId == R.id.modeRandom;
            preferences.edit().putBoolean(PREF_REPEAT_CARDS, random).apply();
            modeHelp.setText(random ? R.string.home_mode_random_help : R.string.home_mode_full_help);
        });

        findViewById(R.id.btnJugar).setOnClickListener(view -> {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra(GameActivity.EXTRA_REPEAT_CARDS, modeToggle.getCheckedButtonId() == R.id.modeRandom);
            startActivity(intent);
        });
    }

    /** Shows the title with the "7" in the brand orange. */
    private void showTitle() {
        TextView title = findViewById(R.id.titleTxt);
        SpannableString text = new SpannableString(getString(R.string.home_title));
        int seven = text.toString().indexOf('7');
        if (seven >= 0) {
            text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.brand_orange)),
                    seven, seven + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        title.setText(text);
    }

    private void setUpCardFan() {
        PlayingCardView left = findViewById(R.id.fanLeft);
        PlayingCardView center = findViewById(R.id.fanCenter);
        PlayingCardView right = findViewById(R.id.fanRight);
        left.setFaceDown(true);
        center.setCard(7, Suit.SPADES);
        right.setCard(7, Suit.HEARTS);

        // Shrink the fan when space is short (small screens, large text) so it never overlaps.
        View content = findViewById(R.id.cardFanContent);
        findViewById(R.id.cardFan).addOnLayoutChangeListener(
                (view, l, t, r, b, oldL, oldT, oldR, oldB) -> {
                    int neededH = content.getHeight();
                    int neededW = content.getWidth();
                    float scaleH = neededH > 0 ? (b - t) / (float) neededH : 1f;
                    float scaleW = neededW > 0 ? (r - l) / (float) neededW : 1f;
                    float scale = Math.min(1f, Math.min(scaleH, scaleW));
                    content.setScaleX(scale);
                    content.setScaleY(scale);
                });
    }
}
