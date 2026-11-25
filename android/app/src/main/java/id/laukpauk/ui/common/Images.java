package id.laukpauk.ui.common;

import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import id.laukpauk.R;
import id.laukpauk.util.ServerPath;

/**
 * Image loading with Glide. Bundled images live in {@code assets/img/} (the Flutter app's
 * {@code assets/img/}); server images are {@code BASE_URL/<upload folder>/<file>} (see {@link ServerPath}).
 */
public final class Images {
    private Images() {}

    /** A bundled image, e.g. {@code Images.asset(iv, "notifikasi/orderan.png")}. */
    public static void asset(ImageView view, String pathInAssetsImg) {
        Glide.with(view).load("file:///android_asset/img/" + pathInAssetsImg).into(view);
    }

    /** A server image with the "no image" placeholder while loading and on error. */
    public static void url(ImageView view, String url) {
        Glide.with(view).load(url)
                .apply(new RequestOptions().placeholder(R.drawable.no_image).error(R.drawable.no_image))
                .into(view);
    }

    /** Same as {@link #url}, cropped to a circle. */
    public static void circle(ImageView view, String url) {
        Glide.with(view).load(url)
                .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.no_image).error(R.drawable.no_image))
                .into(view);
    }

    /** {@code BASE_URL/<folder>/<file>}, e.g. {@code Images.server(ServerPath.uploadImageProduct, foto)}. */
    public static String server(String folder, String file) {
        return ServerPath.baseURL + "/" + folder + "/" + file;
    }

    /**
     * A user photo ({@code photo} of {@code GET buyer/users/me}): a full URL is used as is ({@code http://} loaded
     * over {@code https://}); a bare file name is a legacy photo in the user upload folder.
     */
    public static String user(@Nullable String photo) {
        String p = photo == null ? "" : photo.trim();
        if (p.regionMatches(true, 0, "http://", 0, 7)) {
            return "https://" + p.substring(7);
        }
        if (p.regionMatches(true, 0, "https://", 0, 8)) {
            return p;
        }
        return ServerPath.baseURL + "/" + ServerPath.uploadImageUser + "/" + p;
    }

    /**
     * A voucher picture: a full URL (as {@code GET buyer/vouchers} sends it) is used as is, {@code http://}
     * upgraded to {@code https://}; a bare file name is a legacy picture in the voucher folder.
     */
    public static String voucher(@Nullable String photo) {
        String p = photo == null ? "" : photo.trim();
        if (p.regionMatches(true, 0, "http://", 0, 7)) {
            return "https://" + p.substring(7);
        }
        if (p.regionMatches(true, 0, "https://", 0, 8)) {
            return p;
        }
        return server(ServerPath.uploadImageVoucher + "/compress", p);
    }

    /**
     * A recipe photo: the full URL {@code buyer/recipes} sends, used as is ({@code http://} upgraded to
     * {@code https://} because the app allows no cleartext traffic).
     */
    public static String recipe(@Nullable String photo) {
        String p = photo == null ? "" : photo.trim();
        if (p.regionMatches(true, 0, "http://", 0, 7)) {
            return "https://" + p.substring(7);
        }
        return p;
    }

    /**
     * A product photo: a full URL (as {@code GET buyer/products} sends it) is used as is, {@code http://} upgraded
     * to {@code https://} because the app allows no cleartext traffic; a bare file name is a legacy photo in the
     * compressed product folder.
     */
    public static String product(@Nullable String photo) {
        String p = photo == null ? "" : photo.trim();
        if (p.regionMatches(true, 0, "http://", 0, 7)) {
            return "https://" + p.substring(7);
        }
        if (p.regionMatches(true, 0, "https://", 0, 8)) {
            return p;
        }
        return server(ServerPath.uploadImageProduct, "compress/" + p);
    }
}
