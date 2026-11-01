package id.laukpauk.view.penjual.components.tokosaya;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;

import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.penjual.PenjualUi;

/** Port of lib/views/penjual/components/TokoSaya/TagihanToko.dart. */
public class TagihanToko extends LinearLayout {
    public TagihanToko(final Context ctx) {
        super(ctx);
        setOrientation(VERTICAL);
        LinearLayout card = PenjualUi.cardPadded(ctx, 15, 15);
        addView(card, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout row = Ui.row(ctx);
        card.addView(row, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        row.addView(PenjualUi.text(ctx, "Tagihan Toko", 4f, 0xDD000000, true));
        row.addView(Ui.spacer(ctx));

        final FrameLayout total = new FrameLayout(ctx);
        total.addView(PenjualUi.spinner(ctx, 5), new FrameLayout.LayoutParams(Ui.dp(ctx, 5), Ui.dp(ctx, 5),
                Gravity.CENTER));
        row.addView(total, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));

        PenjualUi.async(ctx, () -> getTotalTagihan(), totalTagihan -> {
            total.removeAllViews();
            total.addView(PenjualUi.text(ctx, Currency.formatRupiah(totalTagihan, 0), 4f, CustomColors.green, true),
                    new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.END));
        });
    }

    /** Dart getTotalTagihan(). BLOCKING. */
    private int getTotalTagihan() {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/TagihanPenjual/getTagihan?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.intVal(dataObject, "tagihan", 0);
        }
        return 0;
    }
}
