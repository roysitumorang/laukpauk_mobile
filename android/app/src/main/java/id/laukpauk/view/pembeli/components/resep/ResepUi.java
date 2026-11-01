package id.laukpauk.view.pembeli.components.resep;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import id.laukpauk.core.CustomColors;

/** Small helpers shared by the Resep widgets (url_launcher + SnackBar equivalents). */
public final class ResepUi {
    private ResepUi() {}

    /** Dart: ScaffoldMessenger.showSnackBar(SnackBar(content: Text(text), duration, action)). */
    public static void snackbar(Activity a, String text, int durationMs, String actionLabel,
                                final Runnable action) {
        View anchor = a.findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(anchor, text, durationMs);
        if (actionLabel != null) {
            snackbar.setActionTextColor(CustomColors.green);
            snackbar.setAction(actionLabel, v -> {
                if (action != null) {
                    action.run();
                }
            });
        }
        snackbar.show();
    }

    /**
     * Dart: canLaunchUrl(Uri.parse(link)) ? launchUrl : SnackBar('URL Tidak Valid ...').
     * @return true when an app was started
     */
    public static boolean launch(Activity a, String link) {
        try {
            Uri uri = Uri.parse(link);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            a.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException | SecurityException | NullPointerException e) {
            return false;
        }
    }
}
