package id.laukpauk.view.pembeli.components.keranjang;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.List;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.ServerPath;
import id.laukpauk.element.Gift;
import id.laukpauk.element.GiftTransaksi;
import id.laukpauk.model.GiftModel;
import id.laukpauk.model.GiftTransaksiModel;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.penjual.PesananScreenUi;

/**
 * Port of lib/views/pembeli/components/Keranjang/MyGift.dart (a whole Scaffold: green "Gift" app bar plus body).
 * Dart `MyGift({Key? key, this.cartCode})`.
 */
public class MyGift extends LinearLayout {
    private final String cartCode;
    private final FrameLayout body;

    public MyGift(Context ctx, String cartCode) {
        super(ctx);
        this.cartCode = cartCode;
        setOrientation(VERTICAL);
        setBackgroundColor(CustomColors.nearlyWhite);

        Activity a = CartUi.activity(ctx);
        addView(CartUi.appBar(a != null ? a : (Activity) ctx, "Gift", true), new LayoutParams(Ui.MATCH, Ui.WRAP));

        body = new FrameLayout(ctx);
        Ui.pad(body, 5, 5, 5, 5);
        addView(body, Ui.weightH(1f));
        body.addView(Loading.view(ctx, null, null, 25f, 25f));

        PesananScreenUi.run(ctx, () -> GiftTransaksiModel.getGiftTransaksi(this.cartCode),
                (List<GiftTransaksi> data) -> render(data));
    }

    private void render(List<GiftTransaksi> data) {
        final Context ctx = getContext();
        float hb = Ui.hb(ctx);
        body.removeAllViews();

        if (data != null && data.size() >= 1) {
            LinearLayout column = Ui.column(ctx);
            for (GiftTransaksi gT : data) {
                final FrameLayout slot = new FrameLayout(ctx);
                slot.addView(Loading.view(ctx, null, null, 35f, 35f));
                column.addView(slot, new LayoutParams(Ui.MATCH, Ui.WRAP));
                final String idGift = gT.idGift;
                PesananScreenUi.run(ctx, () -> GiftModel.getGift(idGift), (Gift g) -> {
                    if (g != null) { // a null gift keeps the Loading indicator, as in Dart
                        slot.removeAllViews();
                        slot.addView(giftCard(ctx, g));
                    }
                });
            }
            ScrollView scroll = new ScrollView(ctx);
            scroll.addView(column, new ScrollView.LayoutParams(Ui.MATCH, Ui.WRAP));
            body.addView(scroll, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            return;
        }

        LinearLayout empty = Ui.column(ctx);
        empty.setGravity(Gravity.CENTER_HORIZONTAL);
        Ui.pad(empty, hb * 10.0f, 0, hb * 10.0f, 0);
        empty.addView(Ui.asset(ctx, "empty.png", hb * 35.0f));
        empty.addView(Ui.spaceH(ctx, 15.0f));
        empty.addView(Ui.text(ctx, "Hadiah", Txt.textSM(ctx)));
        empty.addView(Ui.spaceH(ctx, 10.0f));
        android.widget.TextView desc = Ui.text(ctx,
                "Yaahhhhh, hadiahnya belum bisa didapat ya mak ... Yuk belanja lebih banyak lagi mak supaya dapat hadiah...",
                Txt.textXS(ctx));
        desc.setGravity(Gravity.CENTER);
        empty.addView(desc);
        empty.addView(Ui.spaceH(ctx, 25.0f));
        empty.addView(CustomButton.button(ctx, "Belanja Lagi", () -> {
            Activity act = CartUi.activity(getContext());
            if (act != null) {
                act.finish();
            }
        }, new CustomButton.Opts().color(CustomColors.green).pill()));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.CENTER);
        body.addView(empty, lp);
    }

    private View giftCard(Context ctx, Gift g) {
        String nama = g.nama != null ? g.nama : "Gift";
        String foto = g.foto != null ? g.foto : "gift-default.png";
        String keterangan = g.deskripsi != null ? g.deskripsi : "";

        // Card(shape: radius 100, elevation 5) > ListTile
        LinearLayout card = CartUi.card(ctx, CustomColors.white, 100f, 5f, false);
        LinearLayout tile = new LinearLayout(ctx);
        tile.setOrientation(HORIZONTAL);
        tile.setGravity(Gravity.CENTER_VERTICAL);
        Ui.pad(tile, 16, 8, 16, 8);

        ImageView image = Ui.network(ctx, ServerPath.baseURL + "/" + ServerPath.uploadGambarGift + "/compress/" + foto,
                50f, 50f);
        Ui.clipRounded(image, 25f);
        tile.addView(image);
        tile.addView(Ui.spaceW(ctx, 16));

        LinearLayout texts = Ui.column(ctx);
        texts.addView(Ui.text(ctx, nama, Txt.textSM(ctx)));
        texts.addView(Ui.text(ctx, keterangan, Txt.textXS(ctx).color(CustomColors.textMuted)));
        tile.addView(texts, Ui.weightW(1f));
        card.addView(tile, new LayoutParams(Ui.MATCH, Ui.WRAP));
        return card;
    }
}
