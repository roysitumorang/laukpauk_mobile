package id.laukpauk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/** Android equivalents for share, URL launching, and image selection plugins. */
public final class NativeIntentActions {
    public static final int REQUEST_PICK_IMAGE = 4207;

    private NativeIntentActions() { }

    public static void shareText(Activity activity, String text, String subject) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, text == null ? "" : text);
        if (subject != null) send.putExtra(Intent.EXTRA_SUBJECT, subject);
        activity.startActivity(Intent.createChooser(send, "Bagikan"));
    }

    public static boolean openUrl(Activity activity, String url) {
        if (url == null || url.trim().isEmpty()) return false;
        Uri uri = Uri.parse(url.trim());
        String scheme = uri.getScheme();
        if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)
                && !"tel".equalsIgnoreCase(scheme) && !"mailto".equalsIgnoreCase(scheme)) {
            return false;
        }
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, uri));
            return true;
        } catch (android.content.ActivityNotFoundException error) {
            return false;
        }
    }

    public static void pickImage(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(Intent.createChooser(intent, "Pilih foto"),
                REQUEST_PICK_IMAGE);
    }

    public static Uri selectedImage(Intent resultIntent, int resultCode) {
        if (resultCode != Activity.RESULT_OK || resultIntent == null) return null;
        return resultIntent.getData();
    }
}
