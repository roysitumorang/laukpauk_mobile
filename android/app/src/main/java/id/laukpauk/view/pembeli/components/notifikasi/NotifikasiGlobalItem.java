package id.laukpauk.view.pembeli.components.notifikasi;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;

import java.util.Locale;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/components/Notifikasi/NotifikasiGlobalItem.dart. */
public class NotifikasiGlobalItem extends LinearLayout {

    public NotifikasiGlobalItem(final Context ctx, JsonObject dataNotifikasiItem) {
        super(ctx);
        setOrientation(HORIZONTAL);
        float hb = Ui.hb(ctx);

        JsonObject notifikasi = dataNotifikasiItem;

        String foto = Json.str(notifikasi, "foto");
        String pesan = Json.str(notifikasi, "pesan");
        if (pesan.length() >= 75) {
            pesan = pesan.substring(0, 75) + "..";
        }
        String judul = Json.str(notifikasi, "judul");
        boolean isRead = Json.bool(notifikasi, "isRead");
        final String idNotifikasi = Json.str(notifikasi, "idNotifikasi");
        String createdAt = Json.str(notifikasi, "createdAt");

        float left = hb * 5.0f;
        float right = hb * 5.0f;

        // Container(padding: only(bottom: 5), color) > Padding(horizontal: 10) > ListTile(contentPadding: ...)
        setBackgroundColor(!isRead ? 0x2566CC99 : 0xFFFFFFFF);
        Ui.pad(this, 10 + left, 10, 10 + right, 10);

        ImageView leading;
        if (foto.length() >= 1) {
            leading = Ui.network(ctx, ServerPath.baseURL + "/" + ServerPath.uploadGambarPushNotifikasi
                    + "/compress/" + foto, hb * 13.5f, hb * 13.5f);
        } else {
            leading = Ui.asset(ctx, "laukpauk-icon.png", 50f, 50f);
        }
        Ui.clipRounded(leading, 50f);
        addView(leading, new LayoutParams(leading.getLayoutParams().width, leading.getLayoutParams().height));
        addView(Ui.spaceW(ctx, 16));

        LinearLayout texts = Ui.column(ctx);
        texts.addView(Ui.text(ctx, judul.toUpperCase(Locale.ROOT),
                new Txt.Style(hb * 3.5f)
                        .color(!isRead ? CustomColors.textSuccess : CustomColors.nearlyBlack).bold()));
        texts.addView(Ui.text(ctx, pesan, hb * 3f, 0x8A000000));
        texts.addView(Ui.spaceH(ctx, 5));
        texts.addView(Ui.text(ctx, createdAt, hb * 2.5f, 0x8A000000));
        addView(texts, Ui.weightW(1f));

        Layouts.ripple(this);
        setOnClickListener(v -> {
            Nav.push(ctx, id.laukpauk.view.pembeli.components.notifikasi.DetailNotifikasiGlobalActivity.class,
                    Json.of("idNotifikasi", idNotifikasi));
        });
    }
}
