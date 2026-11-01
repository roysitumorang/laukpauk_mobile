package id.laukpauk.view.pembeli.components.resep;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailResepActivity;

/** Port of lib/views/pembeli/components/Resep/ResepCard.dart. */
public class ResepCard extends LinearLayout {

    public ResepCard(Context ctx, JsonElement dataResepEl) {
        super(ctx);
        setOrientation(VERTICAL);
        final Activity activity = (Activity) ctx;
        final JsonObject dataResep = Json.obj(dataResepEl);
        float hb = Ui.hb(ctx);

        String keterangan = Json.str(dataResep, "keterangan");
        if (keterangan.length() >= 31) {
            keterangan = keterangan.substring(0, 30) + " .. read more";
        }

        // Container(margin: bottom 10) > Material(elevation 8, radius 10) > InkWell
        Ui.pad(this, 0, 0, 0, 10);
        setClipChildren(false);

        FrameLayout stack = new FrameLayout(ctx);
        stack.setBackground(Ui.shape(ctx, CustomColors.nearlyWhite, 10));
        stack.setElevation(Ui.dp(ctx, 8));
        Ui.clipRounded(stack, 10f);
        final String id = Json.str(dataResep, "id");
        stack.setOnClickListener(v -> Nav.push(activity, DetailResepActivity.class, Json.of("id", id)));
        addView(stack, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        ImageView foto = new ImageView(ctx);
        foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Ui.loadNetwork(foto, ServerPath.baseURL + "/" + ServerPath.uploadGambarResep + "/"
                + Json.str(dataResep, "foto"));
        stack.addView(foto, new FrameLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, Ui.vb(ctx) * 20)));

        View overlay = new View(ctx);
        overlay.setBackgroundColor(0x4D000000); // rgba(0,0,0,0.3)
        stack.addView(overlay, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        LinearLayout texts = Ui.column(ctx);
        Ui.padXY(texts, 15, 0);
        texts.addView(Ui.text(ctx, Json.str(dataResep, "nama").toUpperCase(Locale.ROOT), hb * 4.0f,
                CustomColors.nearlyWhite, true));
        texts.addView(Ui.spaceH(ctx, 3.5f));
        texts.addView(Ui.text(ctx, keterangan, hb * 3.0f, CustomColors.nearlyWhite));
        FrameLayout.LayoutParams textsLp = new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP,
                Gravity.BOTTOM | Gravity.LEFT);
        textsLp.bottomMargin = Ui.dp(ctx, 10);
        stack.addView(texts, textsLp);

        final String fbLink = Json.str(dataResep, "fbLink");
        final String igLink = Json.str(dataResep, "igLink");

        stack.addView(linkIcon(ctx, R.drawable.ic_send, fbLink, activity, 0));
        stack.addView(linkIcon(ctx, R.drawable.ic_instagram, igLink, activity, 30));
    }

    private View linkIcon(Context ctx, int drawable, final String link, final Activity activity,
                          int rightDp) {
        FrameLayout box = new FrameLayout(ctx);
        box.addView(Ui.icon(ctx, drawable, 20, CustomColors.nearlyWhite),
                new FrameLayout.LayoutParams(Ui.dp(ctx, 20), Ui.dp(ctx, 20), Gravity.CENTER));
        Layouts.ripple(box, 0x33FFFFFF, 0f);
        box.setOnClickListener(v -> {
            if (!ResepUi.launch(activity, link)) {
                ResepUi.snackbar(activity, "URL Tidak Valid!", 1000, null, null);
            }
        });
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.dp(ctx, 50), Ui.dp(ctx, 20),
                Gravity.TOP | Gravity.RIGHT);
        lp.topMargin = Ui.dp(ctx, 10);
        lp.rightMargin = Ui.dp(ctx, rightDp);
        box.setLayoutParams(lp);
        return box;
    }
}
