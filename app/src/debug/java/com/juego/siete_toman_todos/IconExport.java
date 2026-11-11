package com.juego.siete_toman_todos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/** Debug builds only: renders the launcher icon for review and for the Play Store listing. */
final class IconExport {

    private static final int SIZE = 512;

    static View preview(Context context) {
        AdaptiveIconDrawable icon = (AdaptiveIconDrawable) context.getDrawable(R.mipmap.ic_launcher);
        Bitmap playStore = fullBleed(icon);
        Bitmap masked = render(icon);
        Bitmap themed = themed(context.getDrawable(R.drawable.ic_launcher_monochrome));
        Context app = context.getApplicationContext();
        new Thread(() -> {
            save(app, playStore, "icon_playstore_512.png");
            save(app, masked, "icon_masked_512.png");
            save(app, themed, "icon_themed_512.png");
        }).start();

        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER);
        column.setBackgroundColor(Color.rgb(0xE9, 0xE6, 0xEF));
        int[] sizes = {SIZE, 288, 144};
        for (Bitmap bitmap : new Bitmap[]{playStore, masked, themed}) {
            LinearLayout row = new LinearLayout(context);
            row.setGravity(Gravity.CENTER);
            for (int size : sizes) {
                ImageView image = new ImageView(context);
                image.setImageBitmap(bitmap);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size / 2, size / 2);
                params.setMargins(16, 16, 16, 16);
                row.addView(image, params);
            }
            column.addView(row);
        }
        return column;
    }

    /** Square, unmasked: the Play Console applies its own rounded mask. */
    private static Bitmap fullBleed(AdaptiveIconDrawable icon) {
        Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        int extra = SIZE / 4; // layers are 108dp, of which the middle 72dp is visible
        for (Drawable layer : new Drawable[]{icon.getBackground(), icon.getForeground()}) {
            layer.setBounds(-extra, -extra, SIZE + extra, SIZE + extra);
            layer.draw(canvas);
        }
        return bitmap;
    }

    private static Bitmap render(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        drawable.setBounds(0, 0, SIZE, SIZE);
        drawable.draw(new Canvas(bitmap));
        return bitmap;
    }

    /** Roughly how a Material You launcher shows the themed icon. */
    private static Bitmap themed(Drawable mono) {
        Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(0xE8, 0xDE, 0xF8));
        canvas.drawCircle(SIZE / 2f, SIZE / 2f, SIZE / 2f, paint);
        {
            int extra = SIZE / 4;
            mono.setBounds(-extra, -extra, SIZE + extra, SIZE + extra);
            mono.setColorFilter(new PorterDuffColorFilter(Color.rgb(0x4A, 0x2C, 0x80), PorterDuff.Mode.SRC_IN));
            mono.draw(canvas);
        }
        return bitmap;
    }

    @androidx.annotation.WorkerThread
    private static void save(Context context, Bitmap bitmap, String name) {
        try (FileOutputStream out = new FileOutputStream(new File(context.getFilesDir(), name))) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private IconExport() {
    }
}
