package id.laukpauk.view.pembeli.components.voucher;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Currency;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailVoucherActivity;

/** Port of lib/views/pembeli/components/Voucher/VoucherItem.dart (a ticket-like card). */
public class VoucherItem extends FrameLayout {

    public VoucherItem(Context ctx, JsonObject dataVoucher) {
        super(ctx);
        final JsonObject data = dataVoucher != null ? dataVoucher : new JsonObject();
        final float hb = Ui.hb(ctx);
        final float containerHeight = Ui.vb(ctx) * 20;
        setClipChildren(false);
        setClipToPadding(false);
        Ui.pad(this, 0, 0, 0, 5); // Container(margin: bottom 5)

        LinearLayout card = Layouts.card(ctx, CustomColors.nearlyWhite, 10f, 1f);
        FrameLayout.LayoutParams cardLp = new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
        cardLp.setMargins(Ui.dp(ctx, 4), Ui.dp(ctx, 4), Ui.dp(ctx, 4), Ui.dp(ctx, 4)); // Card default margin
        addView(card, cardLp);

        ImageView foto = new ImageView(ctx);
        foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Ui.loadNetwork(foto, ServerPath.baseURL + "/" + ServerPath.uploadGambarVoucher + "/" + Json.str(data, "foto"));
        FrameLayout fotoHolder = new FrameLayout(ctx);
        fotoHolder.addView(foto, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        Ui.clipRounded(fotoHolder, 10f);
        Layouts.ripple(fotoHolder, 0x33000000, 0f);
        fotoHolder.setOnClickListener(v -> Nav.push(ctx, DetailVoucherActivity.class,
                Json.of("idVoucher", Json.str(data, "id"))));
        card.addView(fotoHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, containerHeight)));

        LinearLayout info = Ui.row(ctx);
        info.setGravity(Gravity.CENTER_VERTICAL);
        Ui.padXY(info, 10, 15);
        info.addView(Ui.text(ctx, "Berlaku hingga " + Json.str(data, "berlakuSampai"), hb * 3.5f, 0xDD000000),
                new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        info.addView(Ui.spacer(ctx));
        info.addView(Ui.text(ctx, "Min. Belanja " + Currency.formatRupiah(
                Json.parseInt(Json.str(data, "minimumBelanja"), 0), 0), hb * 3.5f, 0xDD000000),
                new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        card.addView(info, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        // The two "ticket notches": 30dp circles centred on the card's left / right edge.
        for (int side = 0; side < 2; side++) {
            View notch = new View(ctx);
            notch.setBackground(Ui.circle(CustomColors.aliceBlue));
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.dp(ctx, 30), Ui.dp(ctx, 30),
                    side == 0 ? Gravity.TOP | Gravity.START : Gravity.TOP | Gravity.END);
            lp.topMargin = Ui.dp(ctx, containerHeight / 2);
            if (side == 0) {
                lp.leftMargin = -Ui.dp(ctx, 15);
            } else {
                lp.rightMargin = -Ui.dp(ctx, 15);
            }
            addView(notch, lp);
        }
    }
}
