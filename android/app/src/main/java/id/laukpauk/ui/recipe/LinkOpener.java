package id.laukpauk.ui.recipe;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.Fragment;

import id.laukpauk.ui.common.Messages;

/** Opens a recipe's Facebook / Instagram link (legacy ResepUi.launch + "URL Tidak Valid" snackbar). */
final class LinkOpener {
    private LinkOpener() {}

    static void open(Fragment fragment, String link, int invalidMessage) {
        boolean ok = false;
        try {
            if (link != null && !link.isEmpty()) {
                fragment.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                ok = true;
            }
        } catch (ActivityNotFoundException | SecurityException e) {
            ok = false;
        }
        if (!ok) {
            Messages.snack(fragment, fragment.getString(invalidMessage));
        }
    }
}
