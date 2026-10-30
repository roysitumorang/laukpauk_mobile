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
import id.laukpauk.model.BannerModel;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/components/HomePage/HeroSlider.dart (CarouselSlider -> ViewPager2 + auto-advance). */
public class HeroSlider extends FrameLayout {
    public HeroSlider(Context ctx) {
        super(ctx);
        setLayoutParams(new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, Ui.hb(ctx) * 60)));
        addView(Loading.view(ctx));
        HomeUi.act(ctx).async(() -> BannerModel.getBanner(), this::show);
    }

    private void show(final JsonArray banners) {
        final Context ctx = getContext();
        final float hb = Ui.hb(ctx);
        removeAllViews();
        if (banners == null) {
            addView(Loading.view(ctx));
            return;
        }
        AutoPager pager = new AutoPager(ctx, banners.size(), index -> {
            JsonObject banner = Json.objAt(banners, index);
            String bannerImg = Json.str(banner, "gambar");
            boolean isImageNull = bannerImg.length() <= 1;
            String bannerURL = ServerPath.baseURL + "/" + ServerPath.uploadGambarBanner + "/" + bannerImg;

            String bannerName = Json.str(banner, "nama");
            String bannerDesc = Json.str(banner, "keterangan");

            FrameLayout page = new FrameLayout(ctx);
            FrameLayout square = new FrameLayout(ctx);
            if (isImageNull) {
                ImageView iv = new ImageView(ctx);
                iv.setAdjustViewBounds(true);
                iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                Ui.setAsset(iv, "no-image-img.png");
                square.addView(iv, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP, Gravity.CENTER));
            } else {
                square.addView(HomeUi.netImage(ctx, bannerURL, ImageView.ScaleType.CENTER_CROP));
            }
            page.addView(square, new FrameLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, hb * 100)));

            LinearLayout texts = Ui.column(ctx);
            texts.addView(Ui.text(ctx, bannerName, hb * 3.25f, CustomColors.nearlyWhite));
            if (bannerDesc.length() >= 1) {
                texts.addView(Ui.spaceH(ctx, 2.5f));
                texts.addView(Ui.text(ctx, bannerDesc, hb * 2.75f, CustomColors.nearlyWhite));
            }
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP,
                    Gravity.BOTTOM | Gravity.START);
            lp.setMargins(Ui.dp(ctx, 10), 0, Ui.dp(ctx, 10), Ui.dp(ctx, 10));
            page.addView(texts, lp);
            return page;
        });
        addView(pager, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }
}
