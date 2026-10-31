package id.laukpauk.view.pembeli.components.detailtransaksi;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

import id.laukpauk.core.CustomColors;
import id.laukpauk.element.Gift;
import id.laukpauk.element.GiftTransaksi;
import id.laukpauk.model.GiftModel;
import id.laukpauk.model.GiftTransaksiModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;

/** Port of lib/views/pembeli/components/DetailTransaksi/GiftTransaksiPembeli.dart. */
public class GiftTransaksiPembeli extends LinearLayout {
    private final BaseActivity activity;
    private final String cartCode;
    private final boolean useCard;
    private final LinearLayout bodyHolder;

    /** Dart: GiftTransaksiPembeli({idTransaksi, cartCode, scaffold = false, useCard = false}). */
    public GiftTransaksiPembeli(Context ctx, String idTransaksi, String cartCode, boolean scaffold,
                                boolean useCard) {
        super(ctx);
        setOrientation(VERTICAL);
        this.activity = (BaseActivity) ctx;
        this.cartCode = cartCode;
        this.useCard = useCard;

        bodyHolder = Ui.column(ctx);
        if (scaffold) {
            addView(DetailUi.appBar(activity, "Hadiah Belanja"), new LayoutParams(Ui.MATCH, Ui.WRAP));
            LinearLayout padded = Ui.column(ctx);
            Ui.padXY(padded, 5, 5);
            padded.addView(bodyHolder, new LayoutParams(Ui.MATCH, Ui.WRAP));
            ScrollView sv = Ui.scroll(ctx, padded);
            addView(sv, Ui.weightH(1f));
        } else {
            addView(bodyHolder, new LayoutParams(Ui.MATCH, Ui.WRAP));
        }
        load();
    }

    public GiftTransaksiPembeli(Context ctx, String cartCode) {
        this(ctx, null, cartCode, false, false);
    }

    private TextView contentText(String s) {
        return Ui.text(getContext(), s, Ui.hb(getContext()) * 3.25f, CustomColors.nearlyBlack);
    }

    private TextView deskripsiText(String s) {
        return Ui.text(getContext(), s, Ui.hb(getContext()) * 2.5f, CustomColors.textMuted);
    }

    private void load() {
        Context ctx = getContext();
        bodyHolder.removeAllViews();
        if (cartCode == null) {
            bodyHolder.addView(Ui.text(ctx, "Cart code tidak terdefinisi!"));
            return;
        }
        bodyHolder.addView(Loading.view(ctx));
        activity.async(() -> GiftTransaksiModel.getGiftTransaksi(cartCode), list -> {
            bodyHolder.removeAllViews();
            if (list == null) {
                bodyHolder.addView(Loading.view(ctx));
                return;
            }
            if (list.size() >= 1) {
                bodyHolder.addView(buildList(list));
            }
        });
    }

    private View buildList(List<GiftTransaksi> list) {
        Context ctx = getContext();
        LinearLayout col = Ui.column(ctx);
        for (GiftTransaksi giftTransaksi : list) {
            final String idGift = giftTransaksi.idGift;
            final LinearLayout slot = Ui.column(ctx);
            slot.addView(Loading.view(ctx, null, null, 15f, 15f));
            col.addView(slot, new LayoutParams(Ui.MATCH, Ui.WRAP));
            activity.async(() -> GiftModel.getGift(idGift), gift -> {
                // FutureBuilder<Gift?>: a null result never has data, so the spinner stays (Dart quirk).
                if (gift != null) {
                    slot.removeAllViews();
                    slot.addView(giftRow(gift), new LayoutParams(Ui.MATCH, Ui.WRAP));
                }
            });
        }
        if (useCard) {
            LinearLayout card = TransaksiUi.card(ctx, 15, 10);
            card.addView(col, new LayoutParams(Ui.MATCH, Ui.WRAP));
            return card;
        }
        return col;
    }

    private View giftRow(Gift gift) {
        Context ctx = getContext();
        String namaGift = gift.nama == null ? "" : gift.nama;
        String deskripsiGift = gift.deskripsi == null ? "" : gift.deskripsi;

        LinearLayout row = Ui.row(ctx);
        LinearLayout left = Ui.column(ctx);
        left.addView(contentText(namaGift + " 1 pcs"));
        left.addView(deskripsiText(deskripsiGift));
        row.addView(left, new LayoutParams(0, Ui.WRAP, 1f));
        TextView harga = contentText("Rp. 0");
        harga.setGravity(Gravity.END);
        row.addView(harga, new LayoutParams(Ui.WRAP, Ui.WRAP));
        return row;
    }
}
