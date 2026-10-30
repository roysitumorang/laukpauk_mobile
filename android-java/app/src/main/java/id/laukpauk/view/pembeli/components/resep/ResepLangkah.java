package id.laukpauk.view.pembeli.components.resep;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/components/Resep/ResepLangkah.dart. */
public class ResepLangkah extends LinearLayout {
    private final String idResep;
    private final LinearLayout content;

    public ResepLangkah(Context ctx, String idResep) {
        super(ctx);
        this.idResep = idResep;
        setOrientation(VERTICAL);
        float hb = Ui.hb(ctx);

        // Card(elevation 10, radius 10) > Padding(20)
        setBackground(Ui.shape(ctx, CustomColors.nearlyWhite, 10));
        setElevation(Ui.dp(ctx, 10));
        Ui.padAll(this, 20);

        addView(Ui.text(ctx, "Langkah - langkah : ", hb * 4, 0xDD000000, true));
        addView(Ui.spaceH(ctx, 10));

        content = Ui.column(ctx);
        addView(content, new LayoutParams(Ui.MATCH, Ui.WRAP));
        FrameLayout spinner = new FrameLayout(ctx);
        spinner.addView(new ProgressBar(ctx), new FrameLayout.LayoutParams(Ui.dp(ctx, 36), Ui.dp(ctx, 36),
                Gravity.CENTER));
        content.addView(spinner, new LayoutParams(Ui.MATCH, Ui.WRAP));

        Async.run(() -> getResepLangkah(), this::render);
    }

    /** Dart: getResepLangkah(). BLOCKING. */
    private JsonArray getResepLangkah() {
        String entitasResepLangkah = "id, langkah";
        String params = HTTPRequest.getQueryString("entitasResepLangkah=" + entitasResepLangkah);

        Http.Response httpResponse = Http.getApi(
                "/Resep/getResepLangkahFromThisResep/" + idResep + "?" + params);

        if (httpResponse.statusCode == 200) {
            return Json.array(httpResponse.jsonObject(), "dataResepLangkah");
        }
        return new JsonArray();
    }

    private void render(JsonArray resepLangkah) {
        if (resepLangkah == null) {
            return; // keeps the spinner (Dart: FutureBuilder without data)
        }
        Context ctx = getContext();
        float hb = Ui.hb(ctx);
        content.removeAllViews();

        if (resepLangkah.size() >= 1) {
            int indexData = 0;
            for (JsonElement el : resepLangkah) {
                indexData++;
                LinearLayout row = new LinearLayout(ctx);
                row.setOrientation(HORIZONTAL);
                row.setGravity(Gravity.TOP);
                row.addView(Ui.text(ctx, indexData + ".", 14f, 0xDD000000, true));
                row.addView(Ui.spaceW(ctx, 15));
                TextView langkah = Ui.text(ctx, Json.str(Json.obj(el), "langkah"), hb * 3.25f, 0xDD000000);
                row.addView(langkah, new LayoutParams(0, Ui.WRAP, 1f));
                LayoutParams lp = new LayoutParams(Ui.MATCH, Ui.WRAP);
                lp.bottomMargin = Ui.dp(ctx, 10);
                content.addView(row, lp);
            }
        } else {
            LinearLayout empty = Ui.column(ctx);
            empty.setGravity(Gravity.CENTER_HORIZONTAL);
            empty.addView(Ui.asset(ctx, "empty.png", hb * 30));
            empty.addView(Ui.spaceH(ctx, 5));
            empty.addView(Ui.text(ctx, "Langkah - langkah tidak ada", hb * 3.5f, CustomColors.textDanger));
            content.addView(empty, new LayoutParams(Ui.MATCH, Ui.WRAP));
        }
    }
}
