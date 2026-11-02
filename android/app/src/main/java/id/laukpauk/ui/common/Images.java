package id.laukpauk.ui.common;

import android.widget.ImageView;

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

    /** {@code BASE_URL/<folder>/<file>}, e.g. {@code Images.server(ServerPath.uploadGambarProduk, foto)}. */
    public static String server(String folder, String file) {
        return ServerPath.baseURL + "/" + folder + "/" + file;
    }
}
