package com.juego.siete_toman_todos;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.PathParser;

/**
 * Draws a playing card (face or back) entirely in vector graphics, so it is sharp at any size and
 * needs no image files. The card keeps a 5:7 shape and is centred in whatever space the view gets.
 */
public class PlayingCardView extends View {

    /** Card height divided by width. */
    private static final float ASPECT = 1.4f;

    private static final int PAPER = 0xFFFFFDF8;
    private static final int EDGE = 0xFFD9D3C7;
    private static final int RED = 0xFFC62828;
    private static final int BLACK = 0xFF1E1A2B;
    private static final int GOLD = 0xFFE8A317;
    private static final int RED_TINT = 0xFFFBE9E6;
    private static final int BLACK_TINT = 0xFFEAE8F2;
    private static final int BACK_TOP = 0xFF5A3596;
    private static final int BACK_BOTTOM = 0xFF24163F;
    private static final int BACK_MEDALLION = 0xFF2A1B47;
    private static final int ORANGE = 0xFFF7A21B;

    // Shapes are drawn in a 100 x 100 box.
    private static final Path HEART = path("M50,92 C20,68 4,50 4,30 C4,14 16,4 29,4 C39,4 46,10 50,18"
            + " C54,10 61,4 71,4 C84,4 96,14 96,30 C96,50 80,68 50,92 Z");
    private static final Path DIAMOND = path("M50,2 C60,22 74,38 92,50 C74,62 60,78 50,98"
            + " C40,78 26,62 8,50 C26,38 40,22 50,2 Z");
    private static final Path SPADE = path("M50,4 C64,24 94,40 94,62 C94,76 83,84 72,84"
            + " C63,84 56,80 53,74 C54,84 58,92 66,97 L34,97 C42,92 46,84 47,74"
            + " C44,80 37,84 28,84 C17,84 6,76 6,62 C6,40 36,24 50,4 Z");
    private static final Path CLUB = path("M31,28 a19,19 0 1,1 38,0 a19,19 0 1,1 -38,0 Z"
            + " M9,58 a19,19 0 1,1 38,0 a19,19 0 1,1 -38,0 Z"
            + " M53,58 a19,19 0 1,1 38,0 a19,19 0 1,1 -38,0 Z"
            + " M40,40 L60,40 L64,62 L36,62 Z"
            + " M53,60 C53,78 58,90 68,97 L32,97 C42,90 47,78 47,60 Z");
    private static final Path CROWN = path("M8,88 L4,30 L28,52 L50,10 L72,52 L96,30 L92,88 Z");
    private static final Path TIARA = path("M10,88 L16,44 L34,60 L50,22 L66,60 L84,44 L90,88 Z");
    private static final Path STAR = path("M50,6 L62,38 L96,38 L68,58 L79,92 L50,72 L21,92 L32,58"
            + " L4,38 L38,38 Z");

    private static final float L = 0.34f;
    private static final float C = 0.5f;
    private static final float R = 0.66f;
    private static final float THIRD = 1f / 3f;
    private static final float TWO_THIRDS = 2f / 3f;

    /** Classic pip positions for 2 to 10, as (x fraction, row fraction) pairs. */
    private static final float[][] PIPS = {
            null,
            null,
            {C, 0, C, 1},
            {C, 0, C, .5f, C, 1},
            {L, 0, R, 0, L, 1, R, 1},
            {L, 0, R, 0, C, .5f, L, 1, R, 1},
            {L, 0, R, 0, L, .5f, R, .5f, L, 1, R, 1},
            {L, 0, R, 0, C, .25f, L, .5f, R, .5f, L, 1, R, 1},
            {L, 0, R, 0, C, .25f, L, .5f, R, .5f, C, .75f, L, 1, R, 1},
            {L, 0, R, 0, L, THIRD, R, THIRD, C, .5f, L, TWO_THIRDS, R, TWO_THIRDS, L, 1, R, 1},
            {L, 0, R, 0, C, 1f / 6f, L, THIRD, R, THIRD, L, TWO_THIRDS, R, TWO_THIRDS, C, 5f / 6f, L, 1, R, 1},
    };

    private static final Typeface SERIF_BOLD = Typeface.create(Typeface.SERIF, Typeface.BOLD);

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final RectF card = new RectF();
    private final RectF box = new RectF();
    private final Path clip = new Path();
    private final Path scratch = new Path();
    private final Matrix matrix = new Matrix();
    private final Rect bounds = new Rect();

    private Shader backShader;
    private Typeface brandFont;

    private int rank = 1;
    private Suit suit = Suit.SPADES;
    private boolean faceDown;

    public PlayingCardView(Context context) {
        this(context, null);
    }

    public PlayingCardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        stroke.setStyle(Paint.Style.STROKE);
        text.setTextAlign(Paint.Align.CENTER);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(Math.round(card.left), Math.round(card.top),
                        Math.round(card.right), Math.round(card.bottom), radius());
            }
        });
        if (!isInEditMode()) {
            // Fully qualified: the pip column constant R hides the resource class here.
            ResourcesCompat.getFont(context, com.juego.siete_toman_todos.R.font.luckiest_guy, new ResourcesCompat.FontCallback() {
                @Override
                public void onFontRetrieved(@NonNull Typeface typeface) {
                    brandFont = typeface;
                    invalidate();
                }

                @Override
                public void onFontRetrievalFailed(int reason) {
                    // Keep the serif fallback.
                }
            }, null);
        }
    }

    /** @param rank 1 (ace) to 13 (king) */
    public void setCard(int rank, Suit suit) {
        if (rank < 1 || rank > 13) {
            throw new IllegalArgumentException("rank must be 1..13");
        }
        this.rank = rank;
        this.suit = suit;
        invalidate();
    }

    public void setFaceDown(boolean faceDown) {
        this.faceDown = faceDown;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            height = resolveSize(Math.round(width * ASPECT), heightMeasureSpec);
        } else if (heightMode == MeasureSpec.EXACTLY && widthMode != MeasureSpec.EXACTLY) {
            width = resolveSize(Math.round(height / ASPECT), widthMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float availableW = w - getPaddingLeft() - getPaddingRight();
        float availableH = h - getPaddingTop() - getPaddingBottom();
        float cardW = Math.min(availableW, availableH / ASPECT);
        float cardH = cardW * ASPECT;
        float left = getPaddingLeft() + (availableW - cardW) / 2f;
        float top = getPaddingTop() + (availableH - cardH) / 2f;
        card.set(left, top, left + cardW, top + cardH);
        backShader = new LinearGradient(0, 0, cardW, cardH, BACK_TOP, BACK_BOTTOM, Shader.TileMode.CLAMP);
        invalidateOutline();
    }

    private float radius() {
        return card.width() * 0.07f;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (card.isEmpty()) {
            return;
        }
        float w = card.width();
        float h = card.height();
        canvas.save();
        canvas.translate(card.left, card.top);
        if (faceDown) {
            drawBack(canvas, w, h);
        } else {
            drawFace(canvas, w, h);
        }
        canvas.restore();
    }

    // ---------------------------------------------------------------- face

    private void drawFace(Canvas canvas, float w, float h) {
        float r = radius();
        int ink = suit.red ? RED : BLACK;

        fill.setShader(null);
        fill.setColor(PAPER);
        canvas.drawRoundRect(0, 0, w, h, r, r, fill);
        stroke.setColor(EDGE);
        stroke.setStrokeWidth(w * 0.008f);
        float half = w * 0.004f;
        canvas.drawRoundRect(half, half, w - half, h - half, r, r, stroke);

        drawIndex(canvas, w, h, ink);
        canvas.save();
        canvas.rotate(180, w / 2f, h / 2f);
        drawIndex(canvas, w, h, ink);
        canvas.restore();

        if (rank == 1) {
            drawAce(canvas, w, h, ink);
        } else if (rank <= 10) {
            drawPips(canvas, w, h, ink);
        } else {
            drawCourt(canvas, w, h, ink);
        }
    }

    private void drawIndex(Canvas canvas, float w, float h, int ink) {
        String label = label(rank);
        text.setTypeface(SERIF_BOLD);
        text.setColor(ink);
        text.setTextSize(w * 0.16f);
        text.setTextScaleX(rank == 10 ? 0.78f : 1f);
        text.getTextBounds(label, 0, label.length(), bounds);
        float cx = w * 0.1f;
        float top = h * 0.045f;
        canvas.drawText(label, cx, top - bounds.top, text);
        text.setTextScaleX(1f);
        float pip = w * 0.1f;
        drawShape(canvas, suitPath(suit), cx, top + bounds.height() + w * 0.03f + pip / 2f, pip, false, ink);
    }

    private void drawAce(Canvas canvas, float w, float h, int ink) {
        float cx = w / 2f;
        float cy = h / 2f;
        stroke.setColor(withAlpha(ink, 45));
        stroke.setStrokeWidth(w * 0.012f);
        canvas.drawCircle(cx, cy, w * 0.33f, stroke);
        stroke.setStrokeWidth(w * 0.005f);
        canvas.drawCircle(cx, cy, w * 0.365f, stroke);
        drawShape(canvas, suitPath(suit), cx, cy, w * 0.46f, false, ink);
    }

    private void drawPips(Canvas canvas, float w, float h, int ink) {
        float[] pips = PIPS[rank];
        float top = h * 0.21f;
        float bottom = h * 0.79f;
        float size = w * 0.16f;
        for (int i = 0; i < pips.length; i += 2) {
            float x = pips[i] * w;
            float row = pips[i + 1];
            float y = top + row * (bottom - top);
            drawShape(canvas, suitPath(suit), x, y, size, row > 0.5f, ink);
        }
    }

    private void drawCourt(Canvas canvas, float w, float h, int ink) {
        box.set(w * 0.22f, h * 0.12f, w * 0.78f, h * 0.88f);
        float pr = w * 0.04f;

        fill.setColor(suit.red ? RED_TINT : BLACK_TINT);
        canvas.drawRoundRect(box, pr, pr, fill);

        // Fine diagonal texture inside the panel.
        clip.reset();
        clip.addRoundRect(box, pr, pr, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clip);
        stroke.setColor(withAlpha(ink, 22));
        stroke.setStrokeWidth(w * 0.004f);
        float step = w * 0.035f;
        for (float d = box.left - box.height(); d < box.right; d += step) {
            canvas.drawLine(d, box.bottom, d + box.height(), box.top, stroke);
        }
        canvas.restore();

        drawCourtHalf(canvas, w, ink);
        canvas.save();
        canvas.rotate(180, w / 2f, h / 2f);
        drawCourtHalf(canvas, w, ink);
        canvas.restore();

        // Divider with a small diamond in the middle.
        stroke.setColor(withAlpha(ink, 110));
        stroke.setStrokeWidth(w * 0.006f);
        canvas.drawLine(box.left + w * 0.05f, h / 2f, box.right - w * 0.05f, h / 2f, stroke);
        drawShape(canvas, DIAMOND, w / 2f, h / 2f, w * 0.07f, false, ink);

        stroke.setColor(ink);
        stroke.setStrokeWidth(w * 0.012f);
        canvas.drawRoundRect(box, pr, pr, stroke);
        float inset = w * 0.025f;
        stroke.setColor(withAlpha(ink, 90));
        stroke.setStrokeWidth(w * 0.005f);
        canvas.drawRoundRect(box.left + inset, box.top + inset, box.right - inset, box.bottom - inset,
                pr * 0.6f, pr * 0.6f, stroke);
    }

    /** Draws the emblem and letter in the top half of the court panel. */
    private void drawCourtHalf(Canvas canvas, float w, int ink) {
        float top = box.top + w * 0.045f;
        float size = w * 0.22f;
        Path emblem = rank == 11 ? STAR : rank == 12 ? TIARA : CROWN;
        float cx = w / 2f;
        float cy = top + size / 2f;
        drawShape(canvas, emblem, cx, cy, size, false, GOLD);
        stroke.setColor(ink);
        stroke.setStrokeWidth(w * 0.008f);
        canvas.drawPath(scratch, stroke);
        float jewelY = top + size * (rank == 11 ? 0.56f : 0.66f);
        drawShape(canvas, suitPath(suit), cx, jewelY, size * 0.28f, false, ink);

        String label = label(rank);
        text.setTypeface(SERIF_BOLD);
        text.setColor(ink);
        text.setTextSize(w * 0.22f);
        text.getTextBounds(label, 0, label.length(), bounds);
        float baseline = top + size + w * 0.03f - bounds.top;
        canvas.drawText(label, cx, baseline, text);

        float flankY = baseline + bounds.top / 2f;
        drawShape(canvas, suitPath(suit), cx - w * 0.17f, flankY, w * 0.08f, false, ink);
        drawShape(canvas, suitPath(suit), cx + w * 0.17f, flankY, w * 0.08f, false, ink);
    }

    // ---------------------------------------------------------------- back

    private void drawBack(Canvas canvas, float w, float h) {
        float r = radius();
        fill.setShader(null);
        fill.setColor(PAPER);
        canvas.drawRoundRect(0, 0, w, h, r, r, fill);

        float border = w * 0.035f;
        box.set(border, border, w - border, h - border);
        fill.setShader(backShader);
        canvas.drawRoundRect(box, r * 0.75f, r * 0.75f, fill);
        fill.setShader(null);

        float inset = w * 0.075f;
        box.set(inset, inset, w - inset, h - inset);
        clip.reset();
        clip.addRoundRect(box, r * 0.5f, r * 0.5f, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clip);
        stroke.setColor(0x26FFFFFF);
        stroke.setStrokeWidth(w * 0.006f);
        float step = w * 0.09f;
        for (float d = -h; d < w + h; d += step) {
            canvas.drawLine(d, 0, d + h, h, stroke);
            canvas.drawLine(d, 0, d - h, h, stroke);
        }
        canvas.restore();

        stroke.setColor(ORANGE);
        stroke.setStrokeWidth(w * 0.014f);
        canvas.drawRoundRect(box, r * 0.5f, r * 0.5f, stroke);

        float cx = w / 2f;
        float cy = h / 2f;
        fill.setColor(BACK_MEDALLION);
        canvas.drawCircle(cx, cy, w * 0.2f, fill);
        stroke.setStrokeWidth(w * 0.012f);
        canvas.drawCircle(cx, cy, w * 0.2f, stroke);
        stroke.setColor(withAlpha(ORANGE, 110));
        stroke.setStrokeWidth(w * 0.005f);
        canvas.drawCircle(cx, cy, w * 0.24f, stroke);

        text.setTypeface(brandFont != null ? brandFont : SERIF_BOLD);
        text.setColor(ORANGE);
        text.setTextSize(w * 0.3f);
        text.getTextBounds("7", 0, 1, bounds);
        canvas.drawText("7", cx, cy - bounds.exactCenterY(), text);
    }

    // ---------------------------------------------------------------- helpers

    /** Fills {@code shape} centred on (cx, cy); the transformed path is left in {@link #scratch}. */
    private void drawShape(Canvas canvas, Path shape, float cx, float cy, float size, boolean upsideDown, int color) {
        float scale = size / 100f;
        matrix.setScale(scale, scale);
        matrix.postTranslate(cx - size / 2f, cy - size / 2f);
        if (upsideDown) {
            matrix.postRotate(180, cx, cy);
        }
        shape.transform(matrix, scratch);
        fill.setShader(null);
        fill.setColor(color);
        canvas.drawPath(scratch, fill);
    }

    private static Path suitPath(Suit suit) {
        switch (suit) {
            case HEARTS:
                return HEART;
            case DIAMONDS:
                return DIAMOND;
            case CLUBS:
                return CLUB;
            default:
                return SPADE;
        }
    }

    private static String label(int rank) {
        switch (rank) {
            case 1:
                return "A";
            case 11:
                return "J";
            case 12:
                return "Q";
            case 13:
                return "K";
            default:
                return String.valueOf(rank);
        }
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private static Path path(String data) {
        return PathParser.createPathFromPathData(data);
    }
}
