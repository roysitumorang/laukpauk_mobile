package id.laukpauk.view.pembeli.components.produkdidalampasar;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.util.Locale;

import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.ProdukDiDalamPasarActivity;

/** Port of lib/views/pembeli/components/ProdukDiDalamPasar/PasarLainItem.dart. */
public class PasarLainItem extends LinearLayout {

    public PasarLainItem(Context ctx, JsonObject dataPasar, boolean showDistance) {
        super(ctx);
        setOrientation(VERTICAL);
        final JsonObject data = dataPasar != null ? dataPasar : new JsonObject();
        final float hb = Ui.hb(ctx);
        setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(ctx, hb * 35), Ui.WRAP));

        String imageURL = ServerPath.baseURL + "/" + ServerPath.uploadGambarPasar + "/compress/"
                + Json.str(data, "gambar");
        boolean useAsset = false;
        if ("0".equals(Json.str(data, "pasarUmum"))) {
            useAsset = true; // ClientPath.assetsImg + '/laukpauk.png'
        }

        String distanceString = "";
        if (showDistance) {
            double distanceDouble = Json.dbl(data, "distance");
            distanceString = String.format(Locale.US, "%.1f", distanceDouble);
        }

        FrameLayout imageHolder = new FrameLayout(ctx);
        ImageView image = new ImageView(ctx);
        image.setScaleType(ImageView.ScaleType.FIT_XY); // BoxFit.fill
        if (useAsset) {
            Ui.setAsset(image, "laukpauk.png");
        } else {
            Ui.loadNetwork(image, imageURL);
        }
        imageHolder.addView(image, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        Ui.clipRounded(imageHolder, 10f);
        Layouts.ripple(imageHolder, 0x33000000, 10f);
        imageHolder.setOnClickListener(v -> {
            Nav.pushReplacement((Activity) getContext(), ProdukDiDalamPasarActivity.class,
                    Json.of("idPasar", Json.str(data, "id"), "namaPasar", Json.str(data, "nama")));
        });
        addView(imageHolder, new LinearLayout.LayoutParams(Ui.dp(ctx, hb * 35), Ui.dp(ctx, hb * 22.5f)));

        addView(Ui.spaceH(ctx, 6.5f));

        TextView nama = Ui.text(ctx, Json.str(data, "nama").toUpperCase(), hb * 2.75f, 0xDD000000);
        nama.setSingleLine(true);
        nama.setGravity(Gravity.CENTER);
        addView(Layouts.hscroll(ctx, nama), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        if (showDistance) {
            TextView distance = Ui.text(ctx, distanceString + " KM", hb * 2.25f, 0xDD000000);
            distance.setSingleLine(true);
            distance.setGravity(Gravity.CENTER);
            addView(Layouts.hscroll(ctx, distance), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
    }

    public PasarLainItem(Context ctx, JsonObject dataPasar) {
        this(ctx, dataPasar, false);
    }
}
