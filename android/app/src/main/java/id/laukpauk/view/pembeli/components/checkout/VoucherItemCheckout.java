package id.laukpauk.view.pembeli.components.checkout;

import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.Currency;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/components/Checkout/VoucherItemCheckout.dart (a Flutter ListTile row). */
public class VoucherItemCheckout extends LinearLayout {

    /** @param voucherTerpilih the voucher currently used, or null */
    public VoucherItemCheckout(Context ctx, final JsonObject dataVoucher, JsonObject voucherTerpilih,
                               final Async.Done<JsonObject> onVoucherSelected) {
        super(ctx);
        setOrientation(VERTICAL);
        float hb = Ui.hb(ctx);

        long nilaiVoucher = Json.longVal(dataVoucher, "nilai");
        long minimumBelanja = Json.longVal(dataVoucher, "minimumBelanja");

        String voucherImage = ServerPath.baseURL + "/" + ServerPath.uploadGambarVoucher + "/compress/"
                + Json.str(dataVoucher, "foto");

        boolean isSelectedVoucher = voucherTerpilih != null
                && Json.str(voucherTerpilih, "id").equals(Json.str(dataVoucher, "id"));

        if (isSelectedVoucher) {
            Ui.background(this, 0x6666CC99, 10);
            Ui.pad(this, 5, 10, 5, 10);
        }

        LinearLayout tile = Ui.row(ctx);
        tile.setGravity(Gravity.CENTER_VERTICAL);
        Ui.pad(tile, 16, 8, 16, 8);
        Layouts.ripple(tile);
        tile.setOnClickListener(v -> {
            if (onVoucherSelected != null) {
                onVoucherSelected.run(dataVoucher);
            }
        });

        float imgDp = hb * 12.5f;
        ImageView img = Ui.network(ctx, voucherImage, imgDp, imgDp);
        Ui.clipRounded(img, imgDp / 2f);
        tile.addView(img, new LinearLayout.LayoutParams(Ui.dp(ctx, imgDp), Ui.dp(ctx, imgDp)));
        tile.addView(Ui.spaceW(ctx, 16));

        LinearLayout texts = Ui.column(ctx);
        texts.addView(CheckoutUi.text(ctx, Json.str(dataVoucher, "nama"), hb * 4, CheckoutUi.TEXT, false));
        texts.addView(CheckoutUi.text(ctx, "Voucher untuk mengurangi " + Json.str(dataVoucher, "jenis"),
                hb * 3, CheckoutUi.TEXT, false));
        TextView sub = CheckoutUi.text(ctx, "Potongan " + Currency.formatRupiah(nilaiVoucher, 0)
                + " dengan minimal belanja " + Currency.formatRupiah(minimumBelanja, 0), hb * 3, 0x99000000,
                false);
        texts.addView(sub, CheckoutUi.lp(Ui.WRAP, Ui.WRAP, 0, Ui.dp(ctx, 5), 0, 0));
        tile.addView(texts, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));

        addView(tile, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        addView(Ui.spaceH(ctx, 5)); // margin: EdgeInsets.only(bottom: 5)
    }
}
