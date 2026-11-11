package com.juego.siete_toman_todos;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.View;
import android.view.Window;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/** Window setup shared by the screens: edge-to-edge drawing, safe areas, bars and orientation. */
final class FullScreen {

    /** Phones stay portrait; tablets, foldables and large windows may rotate. */
    private static final int LARGE_SCREEN_MIN_DP = 600;

    /**
     * Call before {@code setContentView}. The app background is always dark, so system bar icons
     * are always light, whatever the system theme.
     */
    static void enableEdgeToEdge(ComponentActivity activity) {
        EdgeToEdge.enable(activity,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));
    }

    /** Locks portrait on phones only. Android 16+ ignores the lock on large screens anyway. */
    static void lockPortraitOnPhones(Activity activity) {
        if (activity.getResources().getConfiguration().smallestScreenWidthDp < LARGE_SCREEN_MIN_DP) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /** Pads {@code root} so its children are never covered by system bars or a display cutout. */
    static void keepContentInSafeArea(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets safe = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            return windowInsets;
        });
    }

    /**
     * Hides the system bars during gameplay. A swipe from the screen edge shows them briefly.
     * Call from {@code onResume}.
     */
    static void hideSystemBars(Window window) {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        controller.hide(WindowInsetsCompat.Type.systemBars());
    }

    private FullScreen() {
    }
}
