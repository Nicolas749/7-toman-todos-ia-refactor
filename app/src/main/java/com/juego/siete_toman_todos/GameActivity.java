package com.juego.siete_toman_todos;

import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Random;

public class GameActivity extends AppCompatActivity {

    static final String EXTRA_REPEAT_CARDS = "repeat_cards";

    private static final String STATE_ORDER = "deck_order";
    private static final String STATE_DRAWN = "deck_drawn";
    private static final String STATE_CURRENT = "deck_current";
    private static final String STATE_FINISHED = "deck_finished";

    /** The card sound was designed to play at double speed. */
    private static final float SOUND_RATE = 2f;
    private static final long FLIP_HALF_MS = 130;
    private static final long INTRO_DELAY_MS = 350;
    /** From this system text size the card gives up some height to the rule. */
    private static final float LARGE_FONT_SCALE = 1.5f;

    private final Random random = new Random();

    private PlayingCardView cardView;
    private TextView cardTitle;
    private TextView infoTxt;
    private TextView progressTxt;
    private TextView tapHint;
    private LinearProgressIndicator progressBar;
    private View rulePanel;
    private View restartButton;
    private OnBackPressedCallback exitGuard;

    private SoundPool soundPool;
    private int cardSoundId;

    private boolean repeatCards;
    private Deck deck;
    private boolean finished;
    private boolean animating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FullScreen.enableEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        FullScreen.lockPortraitOnPhones(this);
        setContentView(R.layout.activity_game);
        FullScreen.keepContentInSafeArea(findViewById(R.id.gameRoot));

        cardView = findViewById(R.id.cardView);
        cardTitle = findViewById(R.id.cardTitle);
        infoTxt = findViewById(R.id.infoTxt);
        progressTxt = findViewById(R.id.progressTxt);
        tapHint = findViewById(R.id.tapHintTxt);
        progressBar = findViewById(R.id.progressBar);
        rulePanel = findViewById(R.id.rulePanel);
        restartButton = findViewById(R.id.restartButton);

        cardView.setCameraDistance(getResources().getDisplayMetrics().density * 8000f);
        // Long rules scroll instead of being cut off when the text is very large.
        infoTxt.setMovementMethod(new ScrollingMovementMethod());
        giveRuleMoreRoomForLargeText();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        cardSoundId = soundPool.load(this, R.raw.card_change, 1);

        repeatCards = getIntent().getBooleanExtra(EXTRA_REPEAT_CARDS, false);
        progressBar.setMax(CardCatalog.CARDS.size());
        progressBar.setVisibility(repeatCards ? View.GONE : View.VISIBLE);

        exitGuard = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                confirmExit();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, exitGuard);

        findViewById(R.id.backButton).setOnClickListener(
                view -> getOnBackPressedDispatcher().onBackPressed());
        restartButton.setOnClickListener(view -> restart());
        cardView.setOnClickListener(view -> onCardTapped());

        if (savedInstanceState != null) {
            deck = Deck.restore(
                    savedInstanceState.getIntArray(STATE_ORDER),
                    savedInstanceState.getInt(STATE_DRAWN),
                    savedInstanceState.getInt(STATE_CURRENT),
                    repeatCards,
                    random);
            finished = savedInstanceState.getBoolean(STATE_FINISHED);
            render();
        } else {
            deck = new Deck(CardCatalog.CARDS.size(), repeatCards, random);
            deck.draw();
            // Start with the card face down, then turn it over.
            render();
            cardView.setFaceDown(true);
            rulePanel.setAlpha(0f);
            animating = true;
            cardView.postDelayed(this::flipToCurrent, INTRO_DELAY_MS);
        }
    }

    private void giveRuleMoreRoomForLargeText() {
        Guideline cardBottom = findViewById(R.id.cardBottom);
        if (cardBottom != null && getResources().getConfiguration().fontScale >= LARGE_FONT_SCALE) {
            cardBottom.setGuidelinePercent(0.48f);
        }
    }

    private void onCardTapped() {
        if (animating || finished) {
            return;
        }
        if (deck.hasNext()) {
            deck.draw();
            soundPool.play(cardSoundId, 1f, 1f, 0, 0, SOUND_RATE);
        } else {
            finished = true;
        }
        cardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        flipToCurrent();
    }

    private void restart() {
        if (animating) {
            return;
        }
        finished = false;
        deck.reset();
        deck.draw();
        soundPool.play(cardSoundId, 1f, 1f, 0, 0, SOUND_RATE);
        flipToCurrent();
    }

    /** Asks before throwing away a deck in progress. Nothing is lost until the player confirms. */
    private void confirmExit() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.game_exit_title)
                .setMessage(R.string.game_exit_message)
                .setNegativeButton(R.string.game_exit_cancel, null)
                .setPositiveButton(R.string.game_exit_confirm, (dialog, which) -> finish())
                .show();
    }

    /** Turns the card over, swapping in the current state halfway through. */
    private void flipToCurrent() {
        animating = true;
        cardView.animate()
                .rotationY(90f)
                .setDuration(FLIP_HALF_MS)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    render();
                    rulePanel.setAlpha(0f);
                    rulePanel.animate().alpha(1f).setDuration(220).start();
                    cardView.setRotationY(-90f);
                    cardView.animate()
                            .rotationY(0f)
                            .setDuration(FLIP_HALF_MS)
                            .setInterpolator(new DecelerateInterpolator())
                            .withEndAction(() -> animating = false)
                            .start();
                })
                .start();
    }

    private void render() {
        cardView.setFaceDown(finished);
        restartButton.setVisibility(finished ? View.VISIBLE : View.GONE);
        tapHint.setVisibility(finished ? View.INVISIBLE : View.VISIBLE);

        if (finished) {
            cardTitle.setText(R.string.game_deck_finished_title);
            infoTxt.setText(R.string.game_deck_finished_text);
            cardView.setContentDescription(getString(R.string.game_deck_finished_title));
            cardView.setClickable(false);
            ViewCompat.replaceAccessibilityAction(cardView, AccessibilityActionCompat.ACTION_CLICK, null, null);
        } else {
            CardCatalog.Card card = CardCatalog.CARDS.get(deck.current());
            cardView.setCard(card.rank, card.suit);
            cardTitle.setText(card.title);
            infoTxt.setText(card.rule);
            cardView.setContentDescription(getString(R.string.card_description,
                    rankName(card.rank), getString(CardCatalog.suitName(card.suit))));
            cardView.setClickable(true);
            // TalkBack says "double tap to draw the next card" instead of a generic "activate".
            ViewCompat.replaceAccessibilityAction(cardView, AccessibilityActionCompat.ACTION_CLICK,
                    getString(R.string.game_draw_next), (view, arguments) -> view.performClick());
        }
        infoTxt.scrollTo(0, 0);

        if (repeatCards) {
            progressTxt.setText(R.string.game_random_mode);
        } else {
            int total = CardCatalog.CARDS.size();
            progressTxt.setText(getString(R.string.game_progress, deck.drawn(), total));
            progressBar.setProgressCompat(deck.drawn(), true);
        }

        // Only a full deck that is part-way through is worth protecting.
        exitGuard.setEnabled(!repeatCards && !finished && deck.drawn() > 1);
    }

    private String rankName(int rank) {
        int name = CardCatalog.rankName(rank);
        return name != 0 ? getString(name) : String.valueOf(rank);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FullScreen.hideSystemBars(getWindow());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray(STATE_ORDER, deck.order());
        outState.putInt(STATE_DRAWN, deck.drawn());
        outState.putInt(STATE_CURRENT, deck.current());
        outState.putBoolean(STATE_FINISHED, finished);
    }

    @Override
    protected void onDestroy() {
        cardView.animate().cancel();
        soundPool.release();
        soundPool = null;
        super.onDestroy();
    }
}
