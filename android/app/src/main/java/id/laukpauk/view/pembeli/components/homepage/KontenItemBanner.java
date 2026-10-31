package id.laukpauk.view.pembeli.components.homepage;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Routing;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/components/HomePage/KontenItemBanner.dart (16:9 auto-playing banner slider). */
public class KontenItemBanner extends FrameLayout {
    public KontenItemBanner(final Context ctx, JsonObject detailKontenMaster, final JsonArray kontenItem) {
        super(ctx);
        final float hb = Ui.hb(ctx);
        final String routeTo = Json.str(detailKontenMaster, "route");

        AutoPager pager = new AutoPager(ctx, kontenItem.size(), index -> {
            JsonObject banner = Json.objAt(kontenItem, index);

            final String idPilihan = Json.str(banner, "pilihan");
            String bannerImage = Json.str(banner, "item");
            String keteranganBanner = Json.str(banner, "keterangan");
            if (keteranganBanner.length() >= 31) {
                keteranganBanner = keteranganBanner.substring(0, 30) + ".. read more";
            }

            String bannerURL = ServerPath.baseURL + "/" + ServerPath.uploadGambarKontenItem + "/" + bannerImage;

            FrameLayout page = new FrameLayout(ctx);
            page.addView(HomeUi.netImage(ctx, bannerURL, ImageView.ScaleType.CENTER_CROP));
            Ui.clipRounded(page, 10);

            TextView ket = Ui.text(ctx, keteranganBanner, hb * 2.75f, 0xDD000000);
            Ui.padXY(ket, 15, 5);
            Ui.background(ket, CustomColors.white, 50);
            ket.setElevation(Ui.dp(ctx, 8));
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP,
                    Gravity.BOTTOM | Gravity.START);
            lp.setMargins(Ui.dp(ctx, 10), 0, 0, Ui.dp(ctx, 10));
            page.addView(ket, lp);

            Ui.onTap(page, () -> {
                // Navigator.pushNamed(context, '/$routeTo', arguments: {routeTo: idPilihan})
                Intent intent = Routing.intentFor(ctx, "/" + routeTo, Json.of(routeTo, idPilihan));
                ctx.startActivity(intent);
            });
            return page;
        });
        addView(Layouts.aspect(ctx, pager, 16f / 9f), new LayoutParams(Ui.MATCH, Ui.WRAP));
    }
}
