package id.laukpauk.view.pembeli.components.homepage;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.AuthModel;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Currency;
import id.laukpauk.core.Json;
import id.laukpauk.model.HomepageModel;
import id.laukpauk.model.PointModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.HistoriDompetActivity;
import id.laukpauk.view.pembeli.HistoriPoinActivity;
import id.laukpauk.view.pembeli.KodeReferalActivity;
import id.laukpauk.view.pembeli.VoucherActivity;

/** Port of lib/views/pembeli/components/HomePage/WalletAndVoucher.dart. */
public class WalletAndVoucher extends LinearLayout {
    private boolean isUserLogin = false;
    private final BaseActivity act;

    public WalletAndVoucher(Context ctx) {
        super(ctx);
        act = HomeUi.act(ctx);
        setOrientation(HORIZONTAL);
        setGravity(android.view.Gravity.CENTER_VERTICAL);
        setBackground(Ui.shape(ctx, CustomColors.nearlyWhite, 10, CustomColors.green, 1.25f));
        Ui.pad(this, 8, 10, 8, 10);
        render();
        act.async(() -> AuthModel.isLogin(), isLogin -> {
            isUserLogin = Boolean.TRUE.equals(isLogin);
            render();
        });
    }

    private TextView title(String s) {
        return Ui.text(getContext(), s, Ui.hb(getContext()) * 3.0f, 0xDD000000, true);
    }

    private TextView content(String s) {
        return HomeUi.t(getContext(), s, Ui.hb(getContext()) * 2.25f);
    }

    private TextView small(String s) {
        return HomeUi.t(getContext(), s, Ui.hb(getContext()) * 2.0f);
    }

    /** Row(children: [Image, SizedBox(15), Column(...)]) */
    private LinearLayout entry(String asset, LinearLayout column, Runnable onTap) {
        Context ctx = getContext();
        float iconWH = Ui.hb(ctx) * 5.0f;
        LinearLayout row = Ui.row(ctx);
        row.addView(Ui.asset(ctx, asset, iconWH, iconWH));
        row.addView(Ui.spaceW(ctx, 15));
        row.addView(column, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        Ui.onTap(row, onTap);
        return row;
    }

    private LinearLayout column(TextView head, View... more) {
        LinearLayout c = Ui.column(getContext());
        c.addView(head, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        for (View v : more) {
            c.addView(v);
        }
        return c;
    }

    private void space() {
        addView(new View(getContext()), new LinearLayout.LayoutParams(0, 1, 1f));
    }

    private void render() {
        final Context ctx = getContext();
        removeAllViews();

        // Dompet
        final TextView dompetValue = small("Mengambil data ...");
        LinearLayout dompetCol = isUserLogin
                ? column(title("Dompet"), Ui.spaceH(ctx, 3), dompetValue)
                : column(title("Dompet"), Ui.spaceH(ctx, 3));
        if (isUserLogin) {
            act.async(() -> HomepageModel.getTotalDompet(), (Integer totalDompet) -> {
                dompetValue.setText(Currency.formatRupiah(totalDompet, 0));
                dompetValue.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, Ui.hb(ctx) * 2.25f);
            });
        }

        // Voucher
        final TextView voucherValue = small("Mengambil data ...");
        LinearLayout voucherCol = isUserLogin
                ? column(title("Voucher"), Ui.spaceH(ctx, 3), voucherValue)
                : column(title("Voucher"), Ui.spaceH(ctx, 3));
        if (isUserLogin) {
            act.async(() -> HomepageModel.getJumlahVoucher(), (Integer jumlahVoucher) -> {
                voucherValue.setText(jumlahVoucher >= 1
                        ? Currency.formatSatuan(jumlahVoucher, "Voucher")
                        : "Belum Ada Voucher");
                voucherValue.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, Ui.hb(ctx) * 2.25f);
            });
        }

        // Poin
        final TextView poinValue = small("Mengambil data ...");
        LinearLayout poinCol = isUserLogin
                ? column(title("Poin"), Ui.spaceH(ctx, 3), poinValue)
                : column(title("Poin"), Ui.spaceH(ctx, 3));
        if (isUserLogin) {
            act.async(() -> PointModel.getPoint("point=true"), (JsonElement responsePoint) -> {
                if (!Json.isFalse(responsePoint) && Json.isObject(responsePoint)) {
                    int totalPoint = Json.intVal(responsePoint.getAsJsonObject(), "point");
                    poinValue.setText(Currency.formatRupiah(totalPoint, 0));
                } else {
                    poinValue.setText("Rp. 0");
                }
                poinValue.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, Ui.hb(ctx) * 2.25f);
            });
        }

        // Kode Referal
        LinearLayout referalCol = column(title("Kode"), title("Referal"));

        space();
        addView(entry("icons/pembeli/dompet.png", dompetCol, () -> Nav.push(ctx, HistoriDompetActivity.class)));
        space();
        addView(entry("icons/pembeli/voucher.png", voucherCol, () -> Nav.push(ctx, VoucherActivity.class)));
        space();
        addView(entry("poin.png", poinCol, () -> Nav.push(ctx, HistoriPoinActivity.class)));
        space();
        addView(entry("referal.png", referalCol, () -> Nav.push(ctx, KodeReferalActivity.class)));
        space();
    }
}
