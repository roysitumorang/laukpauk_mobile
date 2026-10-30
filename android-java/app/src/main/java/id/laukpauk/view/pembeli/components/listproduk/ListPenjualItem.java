package id.laukpauk.view.pembeli.components.listproduk;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.DefaultImage;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.PenjualActivity;
import id.laukpauk.view.pembeli.components.homepage.HomeUi;

/** Port of lib/views/pembeli/components/ListProduk/ListPenjualItem.dart (a seller card in the ListProduk grid). */
public class ListPenjualItem extends FrameLayout {
    private final Double widgetWidth;

    public ListPenjualItem(final Context ctx, JsonObject dataPenjual, Runnable deteksiADJD, Double widgetWidth) {
        super(ctx);
        this.widgetWidth = widgetWidth;
        float hb = Ui.hb(ctx);

        final String idPenjual = Json.str(dataPenjual, "id");
        String bannerToko = Json.str(dataPenjual, "bannerToko");
        String namaToko = Json.str(dataPenjual, "namaToko");
        String alamatToko = Json.str(dataPenjual, "alamatToko");

        boolean isDefaultImage = bannerToko.equals(DefaultImage.defautBannerPenjual);

        // Card(margin: EdgeInsets.only(bottom: 10, left: 2.5, right: 2.5), radius 10)
        Ui.pad(this, 2.5f, 0, 2.5f, 10);
        LinearLayout card = Ui.column(ctx);
        Ui.background(card, CustomColors.white, 10);
        card.setElevation(Ui.dp(ctx, 1));
        Ui.clipRounded(card, 10);
        addView(card, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        // Expanded: banner (bottom margin 15)
        FrameLayout imageBox = new FrameLayout(ctx);
        imageBox.addView(HomeUi.netImage(ctx, ServerPath.baseURL + "/" + ServerPath.uploadGambarBannerPenjual
                + "/compress/" + bannerToko,
                isDefaultImage ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP));
        LinearLayout.LayoutParams imageLp = Ui.weightH(1f);
        imageLp.bottomMargin = Ui.dp(ctx, 15);
        card.addView(imageBox, imageLp);

        // Expanded: texts
        LinearLayout texts = Ui.column(ctx);
        texts.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView nama = Ui.text(ctx, namaToko, hb * 3.5f, 0xDD000000, true);
        nama.setEllipsize(android.text.TextUtils.TruncateAt.END);
        nama.setGravity(Gravity.CENTER);
        Ui.padXY(nama, 15, 0);
        texts.addView(nama, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        texts.addView(Ui.spaceH(ctx, 2));

        TextView toko = Ui.text(ctx, namaToko.length() >= 1 ? namaToko : "Penjual Tidak Diketahui",
                hb * 2.75f, CustomColors.textMuted);
        toko.setGravity(Gravity.CENTER);
        Ui.padXY(toko, 15, 0);
        Ui.onTap(toko, () -> Nav.push(ctx, PenjualActivity.class, Json.of("id", idPenjual)));
        texts.addView(toko, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        texts.addView(Ui.spaceH(ctx, 10));

        TextView alamat = Ui.text(ctx, alamatToko, hb * 3.25f, CustomColors.textMuted);
        alamat.setGravity(Gravity.CENTER);
        Ui.padXY(alamat, 10, 0);
        texts.addView(alamat, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        ScrollView scroll = new ScrollView(ctx);
        scroll.addView(texts, new android.view.ViewGroup.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(scroll, Ui.weightH(1f));

        Ui.onTap(this, () -> Nav.push(ctx, PenjualActivity.class, Json.of("id", idPenjual)));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (widgetWidth != null && MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(Ui.dp(getContext(), widgetWidth.floatValue()),
                    MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
