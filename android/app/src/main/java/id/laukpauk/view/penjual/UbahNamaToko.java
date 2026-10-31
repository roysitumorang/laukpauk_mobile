package id.laukpauk.view.penjual;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;

import id.laukpauk.R;
import id.laukpauk.core.Async;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;

/**
 * Port of class UbahNamaToko in lib/views/penjual/UbahNamaToko.dart: the "Ubah Nama Toko" card embedded in TokoSaya.
 * `ubahNamaTokoCallback` gets the result of the UbahNamaTokoForm screen (true / false) or null when it was left
 * without saving.
 */
public class UbahNamaToko extends LinearLayout {
    public UbahNamaToko(final Context ctx, final Async.Done<Boolean> ubahNamaTokoCallback) {
        super(ctx);
        setOrientation(VERTICAL);
        LinearLayout card = PenjualUi.cardPadded(ctx, 15, 10);
        addView(card, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout row = Ui.row(ctx);
        row.addView(PenjualUi.text(ctx, "Ubah Nama Toko", 4f, 0xDD000000, true));
        row.addView(Ui.spacer(ctx));
        ImageView arrow = Ui.icon(ctx, R.drawable.ic_arrow_circle_right, Ui.hb(ctx) * 5, 0xDD000000);
        Ui.onTap(arrow, () -> Nav.pushForResult((BaseActivity) ctx, UbahNamaTokoFormActivity.class, null, result -> {
            Boolean statusUbahNamaToko = (result != null && result.isJsonPrimitive())
                    ? Boolean.valueOf(id.laukpauk.core.Json.asBool(result, false)) : null;
            ubahNamaTokoCallback.run(statusUbahNamaToko);
        }));
        row.addView(arrow);
        card.addView(row, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }
}
