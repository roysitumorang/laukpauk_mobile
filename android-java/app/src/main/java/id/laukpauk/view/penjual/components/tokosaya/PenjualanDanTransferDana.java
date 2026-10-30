package id.laukpauk.view.penjual.components.tokosaya;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.penjual.HistoriPenjualanActivity;
import id.laukpauk.view.penjual.PenjualUi;
import id.laukpauk.view.penjual.RequestPencairanDanaActivity;

/** Port of lib/views/penjual/components/TokoSaya/PenjualanDanTransferDana.dart. */
public class PenjualanDanTransferDana extends LinearLayout {
    public PenjualanDanTransferDana(final Context ctx) {
        super(ctx);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackground(Ui.shape(ctx, 0x00000000, 10f, CustomColors.green, 1f));
        Ui.padXY(this, 25, 10);

        float hb = Ui.hb(ctx);

        // Penjualan
        LinearLayout penjualan = Ui.row(ctx);
        penjualan.addView(Ui.asset(ctx, "icons/penjualan.png", hb * 10, hb * 10));
        penjualan.addView(Ui.spaceW(ctx, 10));
        LinearLayout penjualanCol = Ui.column(ctx);
        penjualanCol.setGravity(Gravity.CENTER_HORIZONTAL);
        penjualanCol.addView(PenjualUi.text(ctx, "Penjualan", 3.5f, 0xDD000000, true));
        final FrameLayout total = new FrameLayout(ctx);
        total.addView(PenjualUi.spinner(ctx, 10), new FrameLayout.LayoutParams(Ui.dp(ctx, 10), Ui.dp(ctx, 10),
                Gravity.CENTER));
        penjualanCol.addView(total, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        penjualan.addView(penjualanCol);
        Ui.onTap(penjualan, () -> Nav.push(ctx, HistoriPenjualanActivity.class));
        addView(penjualan, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));

        addView(Ui.spacer(ctx));
        View divider = new View(ctx);
        divider.setBackgroundColor(CustomColors.green);
        addView(divider, new LinearLayout.LayoutParams(Ui.dp(ctx, 2), Ui.dp(ctx, 35)));
        addView(Ui.spacer(ctx));

        // Transfer Dana
        LinearLayout transfer = Ui.row(ctx);
        transfer.addView(Ui.asset(ctx, "icons/transfer-dana.png", hb * 10, hb * 10));
        transfer.addView(Ui.spaceW(ctx, 10));
        LinearLayout transferCol = Ui.column(ctx);
        transferCol.addView(PenjualUi.text(ctx, "Transfer Dana", 3.5f, 0xDD000000, true));
        transfer.addView(transferCol);
        Ui.onTap(transfer, () -> Nav.push(ctx, RequestPencairanDanaActivity.class));
        addView(transfer, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));

        PenjualUi.async(ctx, () -> getTotalPenjualan(), totalPenjualan -> {
            total.removeAllViews();
            TextView t = PenjualUi.text(ctx, Currency.formatRupiah(totalPenjualan, 0), 2.75f, 0xDD000000, false);
            Ui.pad(t, 0, 1.5f, 0, 0);
            total.addView(t, new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.CENTER_HORIZONTAL));
        });
    }

    /** Dart getTotalPenjualan(). BLOCKING. */
    private int getTotalPenjualan() {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/Penjual/getTotalPenjualan?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.intVal(dataObject, "totalPenjualan", 0);
        }
        return 0;
    }
}
