package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Currency;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.VoucherModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Ui;

/**
 * Port of lib/views/pembeli/DetailVoucher.dart.
 * Args: idVoucher (String).
 */
public class DetailVoucherActivity extends BaseActivity {
    private String idVoucher;
    private String usedVoucherId; // usedVoucherState['id'], null when no voucher is in use
    private FrameLayout bodyHolder;

    private static final class Loaded {
        String usedVoucherId;
        JsonElement detailVoucher;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        idVoucher = Json.has(args(), "idVoucher") ? Json.str(args(), "idVoucher") : Json.str(args(), "id");

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);
        root.addView(DetailUi.appBar(this, "Detail Voucher"), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        bodyHolder = new FrameLayout(this);
        bodyHolder.addView(DetailUi.smallSpinner(this, 25), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        root.addView(bodyHolder, Ui.weightH(1f));
        setRoot(root);

        load();
    }

    /** initialization() (used voucher) together with the FutureBuilder's getVoucherDetail. */
    private void load() {
        async(() -> {
            Loaded l = new Loaded();
            String usedVoucherString = LocalStorage.getUsedVoucher();
            if (usedVoucherString != null) {
                JsonElement used = VoucherModel.getVoucherDetail(usedVoucherString);
                l.usedVoucherId = Json.isObject(used) ? Json.str(used.getAsJsonObject(), "id") : null;
            }
            l.detailVoucher = VoucherModel.getVoucherDetail(idVoucher);
            return l;
        }, l -> {
            usedVoucherId = l.usedVoucherId;
            render(l.detailVoucher);
        });
    }

    private boolean isThisVoucherUsed() {
        return usedVoucherId != null && idVoucher.equals(usedVoucherId);
    }

    private void toggleVoucher(JsonObject dataVoucher) {
        boolean isUsed = isThisVoucherUsed();
        String namaVoucher = Json.str(dataVoucher, "nama");
        String message;

        if (isUsed) {
            LocalStorage.clearUsedVoucher();
            message = "Penggunaan voucher " + namaVoucher + " dibatalkan!";
        } else {
            LocalStorage.setUsedVoucher(idVoucher);
            message = "Voucher " + namaVoucher + " digunakan!";
        }

        load();

        DetailSnack.show(this, message, 4, 0f);
    }

    private TextView label(String s) {
        return Ui.text(this, s, Ui.hb(this) * 4, 0xDD000000, true);
    }

    private TextView value(String s) {
        return Ui.text(this, s, Ui.hb(this) * 3.25f, 0xDD000000);
    }

    private void render(JsonElement detailElement) {
        final float hb = Ui.hb(this);
        bodyHolder.removeAllViews();

        if (!Json.isObject(detailElement)) {
            LinearLayout column = Ui.column(this);
            column.setGravity(Gravity.CENTER);
            column.addView(Ui.asset(this, "empty.png", hb * 35));
            column.addView(Ui.spaceH(this, 20));
            column.addView(Ui.text(this, "Detail Voucher Tidak Ditemukan", hb * 3.5f, CustomColors.textMuted, true));
            bodyHolder.addView(wrapRefresh(column, true), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            return;
        }

        final JsonObject detailVoucher = detailElement.getAsJsonObject();
        int usageCount = Json.intVal(detailVoucher, "usageCount");
        int maximumUsage = Json.parseInt(Json.str(detailVoucher, "maximumUsage"), 0);
        int sisaPenggunaan = maximumUsage - usageCount;

        String penjelasanVoucher = Json.str(detailVoucher, "jenis").toLowerCase().equals("ongkir")
                ? "Voucher untuk mengurangi ongkos kirim" : "Voucher untuk mengurangi total belanja";
        boolean used = isThisVoucherUsed();

        LinearLayout content = Ui.column(this);

        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        Ui.loadNetwork(image, ServerPath.baseURL + "/" + ServerPath.uploadGambarVoucher + "/"
                + Json.str(detailVoucher, "foto"));
        content.addView(image, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(this, hb * 60)));

        LinearLayout info = Ui.column(this);
        Ui.padXY(info, 15, 15);
        content.addView(info, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout head = Ui.row(this);
        LinearLayout headLeft = Ui.column(this);
        headLeft.addView(Ui.text(this, Json.str(detailVoucher, "nama"), hb * 5.0f, CustomColors.green, true));
        headLeft.addView(Ui.spaceH(this, 5));
        headLeft.addView(Ui.text(this, Json.str(detailVoucher, "jenis").toUpperCase()));
        headLeft.addView(value(penjelasanVoucher));
        head.addView(headLeft, Ui.weightW(1f));
        head.addView(Ui.spaceW(this, 10));
        head.addView(Ui.text(this, Currency.formatRupiah(Json.parseInt(Json.str(detailVoucher, "nilai"), 0), 0),
                hb * 4.5f, CustomColors.green, true));
        info.addView(head, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        View line = new View(this);
        line.setBackgroundColor(CustomColors.nearlyBlack);
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(Ui.MATCH, Math.max(1, Ui.dp(this, 0.5f)));
        lineLp.topMargin = Ui.dp(this, 15);
        lineLp.bottomMargin = Ui.dp(this, 15);
        info.addView(line, lineLp);

        info.addView(label("Kode Voucher"));
        info.addView(Ui.spaceH(this, 3));
        info.addView(value(Json.str(detailVoucher, "kode").toUpperCase()));
        info.addView(Ui.spaceH(this, 10));
        info.addView(label("Minimum Belanja"));
        info.addView(Ui.spaceH(this, 3));
        info.addView(value(Currency.formatRupiah(Json.parseInt(Json.str(detailVoucher, "minimumBelanja"), 0), 0)));
        info.addView(Ui.spaceH(this, 10));
        info.addView(label("Maksimum Penggunaan"));
        info.addView(Ui.spaceH(this, 3));
        info.addView(value(Currency.formatSatuan(maximumUsage, "Kali Penggunaan")));
        info.addView(Ui.spaceH(this, 10));
        info.addView(label("Jumlah Penggunaan"));
        info.addView(Ui.spaceH(this, 3));
        info.addView(value(usageCount >= 1 ? Currency.formatSatuan(usageCount, "Kali Penggunaan")
                : "Belum ada penggunaan"));
        info.addView(Ui.spaceH(this, 10));
        info.addView(label("Masa Berlaku"));
        info.addView(Ui.spaceH(this, 3));
        info.addView(value(Json.str(detailVoucher, "berlakuMulai") + " s/d " + Json.str(detailVoucher, "berlakuSampai")));
        info.addView(Ui.spaceH(this, 10));
        info.addView(label("Deskripsi Voucher"));
        info.addView(Ui.spaceH(this, 3));
        TextView deskripsi = value(Json.str(detailVoucher, "deskripsi"));
        deskripsi.setGravity(Gravity.START);
        info.addView(deskripsi, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        info.addView(Ui.spaceH(this, 15));

        if (sisaPenggunaan >= 1) {
            info.addView(CustomButton.button(this, used ? "Batal Gunakan Voucher" : "Gunakan Voucher",
                    () -> toggleVoucher(detailVoucher),
                    new CustomButton.Opts().color(used ? CustomButton.dangerColor : CustomButton.themeColor).block()),
                    new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        } else {
            TextView habis = Ui.text(this,
                    "Voucher tidak dapat digunakan karena sudah mencapai batas maksimum penggunaan", 14f,
                    CustomColors.textDanger);
            habis.setGravity(Gravity.CENTER);
            habis.setTypeface(habis.getTypeface(), android.graphics.Typeface.ITALIC);
            info.addView(habis, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
        info.addView(Ui.spaceH(this, 5));

        bodyHolder.addView(wrapRefresh(content, false), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }

    private SwipeRefreshLayout wrapRefresh(View content, boolean fillViewport) {
        final SwipeRefreshLayout[] ref = new SwipeRefreshLayout[1];
        ref[0] = DetailUi.refreshable(this, content, () -> {
            load();
            ref[0].setRefreshing(false);
        }, fillViewport);
        return ref[0];
    }
}
