package id.laukpauk.view.pembeli.components.homepage;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.AppState;
import id.laukpauk.core.Json;
import id.laukpauk.core.CustomColors;
import id.laukpauk.model.ADJDModel;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.model.ProdukModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Ui;

/**
 * Small helpers shared by the components of the home page (HomePage, ListProduk, ...): the default
 * Flutter text, the "empty" illustration block, URL launching and the "pilih lokasi / jadwal then
 * show the unit sheet" callback that Dart repeats in KontenItem, ProdukPasarSekitar and ListProduk.
 */
public final class HomeUi {
    private HomeUi() {}

    /** The BaseActivity hosting a view context (components are always built with an Activity context). */
    public static BaseActivity act(Context ctx) {
        Context c = ctx;
        while (c instanceof ContextWrapper) {
            if (c instanceof BaseActivity) {
                return (BaseActivity) c;
            }
            c = ((ContextWrapper) c).getBaseContext();
        }
        throw new IllegalStateException("Context is not hosted by a BaseActivity");
    }

    /** Flutter's default Text colour (black87) at the given size. */
    public static TextView t(Context ctx, CharSequence s, float sp) {
        return Ui.text(ctx, s, sp, 0xDD000000);
    }

    public static TextView t(Context ctx, CharSequence s, float sp, int color, boolean bold) {
        return Ui.text(ctx, s, sp, color, bold);
    }

    /** Section title: fontWeight w500, size horizontalBlock * 4. */
    public static TextView sectionTitle(Context ctx, String s) {
        return Ui.text(ctx, s, Ui.hb(ctx) * 4.0f, 0xDD000000);
    }

    /** "Lihat semua" style link. */
    public static TextView link(Context ctx, String s, int color) {
        return Ui.text(ctx, s, Ui.hb(ctx) * 3.0f, color);
    }

    /** A centred column: empty.png (width), 10dp gap, muted text. Used for every "no data" block. */
    public static LinearLayout emptyBlock(Context ctx, float imageWidthDp, String text, float textSp) {
        LinearLayout col = Ui.column(ctx);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        ImageView img = Ui.asset(ctx, "empty.png", imageWidthDp);
        col.addView(img);
        col.addView(Ui.spaceH(ctx, 10));
        TextView tv = Ui.text(ctx, text, textSp, CustomColors.textMuted);
        tv.setGravity(Gravity.CENTER);
        col.addView(tv, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        return col;
    }

    /** A network image filling its parent (BoxFit.cover). */
    public static ImageView netImage(Context ctx, String url, ImageView.ScaleType scale) {
        ImageView iv = new ImageView(ctx);
        iv.setScaleType(scale);
        iv.setLayoutParams(new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        Ui.loadNetwork(iv, url);
        return iv;
    }

    /** Dart double.toStringAsFixed(1). */
    public static String fixed1(double v) {
        return String.format(java.util.Locale.US, "%.1f", v);
    }

    /** url_launcher canLaunchUrl + launchUrl. Returns false when nothing can open it. */
    public static boolean launchUrl(Activity a, String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            a.startActivity(i);
            return true;
        } catch (ActivityNotFoundException | SecurityException | NullPointerException e) {
            return false;
        }
    }

    /** The plain Material AlertDialog Artikel shows when a URL cannot be opened. */
    public static void alertUrlTidakBisa(Activity a) {
        androidx.appcompat.app.AlertDialog.Builder b = new androidx.appcompat.app.AlertDialog.Builder(a);
        b.setTitle("Lauk Pauk");
        b.setMessage("URL tidak bisa diakses !");
        b.setPositiveButton("OK", null);
        b.show();
    }

    /**
     * The callback Dart passes as `deteksiADJD` to product items:
     * ADJDModel.deleteAlamatDanJadwalDelivery(); adjdSet = await HeaderModel.adjd(context); when
     * latitude, longitude and kodeJadwalDelivery are all set: HeaderViewModel.isADJDSet = true and,
     * when `produk` is not null, ProdukModel.showSatuanProduk(context, produk).
     */
    public static void pilihAdjd(final BaseActivity a, final JsonObject produk) {
        ADJDModel.deleteAlamatDanJadwalDelivery();
        HeaderModel.adjd(a, null, null, null, (JsonObject adjdSet) -> {
            if (adjdSet == null) {
                return;
            }
            String latitude = Json.strOrNull(adjdSet, "latitude");
            String longitude = Json.strOrNull(adjdSet, "longitude");
            String kodeJadwalDelivery = Json.strOrNull(adjdSet, "kodeJadwalDelivery");
            if (latitude != null && longitude != null && kodeJadwalDelivery != null) {
                AppState.get().setADJDSet(true);
                if (produk != null) {
                    ProdukModel.showSatuanProduk(a, produk, null);
                }
            }
        });
    }

    public static View hidden(Context ctx) {
        View v = new View(ctx);
        v.setVisibility(View.GONE);
        return v;
    }
}
