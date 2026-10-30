package id.laukpauk.view.pembeli.components.homepage;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.HomepageModel;
import id.laukpauk.model.SettingModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/components/HomePage/Artikel.dart. */
public class Artikel extends LinearLayout {
    private final FrameLayout box;

    public Artikel(Context ctx) {
        super(ctx);
        setOrientation(VERTICAL);
        final BaseActivity act = HomeUi.act(ctx);
        float hb = Ui.hb(ctx);

        LinearLayout titleRow = Ui.row(ctx);
        titleRow.addView(HomeUi.sectionTitle(ctx, "Artikel"));
        titleRow.addView(Ui.spacer(ctx));
        TextView lihatSemua = Ui.text(ctx, "Lihat Semua", hb * 3.0f, CustomColors.darkGreen);
        Ui.onTap(lihatSemua, () -> act.async(() -> SettingModel.getSettingValue("seeAllArticleURL"), value -> {
            String urlLihatSemua = Json.asStr(value);
            HomeUi.launchUrl(act, urlLihatSemua);
        }));
        titleRow.addView(lihatSemua);
        addView(titleRow, new LayoutParams(Ui.MATCH, Ui.WRAP));
        Ui.margin(titleRow, 0, 0, 0, 15);

        box = new FrameLayout(ctx);
        addView(box, new LayoutParams(Ui.MATCH, Ui.dp(ctx, Ui.vb(ctx) * 22.5f)));
        box.addView(Loading.view(ctx));

        act.async(() -> HomepageModel.getArtikel(), this::showArtikel);
    }

    private void showArtikel(final JsonArray banners) {
        final Context ctx = getContext();
        final BaseActivity act = HomeUi.act(ctx);
        final float hb = Ui.hb(ctx);
        box.removeAllViews();
        if (banners != null && banners.size() >= 1) {
            AutoPager pager = new AutoPager(ctx, banners.size(), index -> {
                final JsonObject banner = Json.objAt(banners, index);
                String bannerURL = ServerPath.baseURL + "/" + ServerPath.uploadGambarArtikel + "/"
                        + Json.str(banner, "foto");

                String judulArtikel = Json.str(banner, "judul");
                if (judulArtikel.length() >= 31) {
                    judulArtikel = judulArtikel.substring(0, 30) + " .. read more";
                }

                FrameLayout page = new FrameLayout(ctx);
                ImageView img = HomeUi.netImage(ctx, bannerURL, ImageView.ScaleType.CENTER_CROP);
                page.addView(img);
                Ui.clipRounded(page, 10);

                TextView judul = Ui.text(ctx, judulArtikel, hb * 2.75f, 0xDD000000);
                Ui.padXY(judul, 15, 5);
                Ui.background(judul, CustomColors.white, 50);
                judul.setElevation(Ui.dp(ctx, 8));
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP,
                        Gravity.BOTTOM | Gravity.START);
                lp.setMargins(Ui.dp(ctx, 10), 0, 0, Ui.dp(ctx, 10));
                page.addView(judul, lp);

                Ui.onTap(page, () -> {
                    String link = Json.str(banner, "link");
                    if (!HomeUi.launchUrl(act, link)) {
                        HomeUi.alertUrlTidakBisa(act);
                    }
                });
                return page;
            });
            box.addView(pager, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            return;
        }

        LinearLayout empty = HomeUi.emptyBlock(ctx, hb * 25, "Tidak ada artikel yang bisa dibaca", hb * 3.5f);
        box.addView(empty, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }
}
