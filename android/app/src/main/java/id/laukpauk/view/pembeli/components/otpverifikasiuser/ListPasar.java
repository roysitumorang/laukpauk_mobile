package id.laukpauk.view.pembeli.components.otpverifikasiuser;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.core.Async;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.Pencarian;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;

/**
 * lib/views/pembeli/components/OTPVerifikasiUser/ListPasar.dart: searchable horizontal list of the
 * available markets. The parent calls {@link #setPasarSelected(String)} where Dart rebuilt the widget.
 */
public class ListPasar extends LinearLayout {
    private boolean isReadyToShowState = false;
    private List<JsonObject> dataPasarState = new ArrayList<>();
    private List<JsonObject> dataPasarImmutableState = new ArrayList<>();
    private String pencarianString = "";
    private String pasarSelected;
    private final Async.Done<String> onPasarClicked;
    private Pencarian pencarian;
    private final FrameLayout listHolder;
    private final float hb;

    public ListPasar(Context ctx, String pasarSelected, Async.Done<String> onPasarClicked) {
        super(ctx);
        this.pasarSelected = pasarSelected;
        this.onPasarClicked = onPasarClicked;
        hb = Ui.hb(ctx);
        setOrientation(VERTICAL);
        LinearLayout.LayoutParams self = new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, hb * 40f));
        self.topMargin = Ui.dp(ctx, 5);
        setLayoutParams(self);

        listHolder = new FrameLayout(ctx);
        ProgressBar bar = new ProgressBar(ctx);
        bar.setIndeterminate(true);
        listHolder.addView(bar, new FrameLayout.LayoutParams(Ui.dp(ctx, 15), Ui.dp(ctx, 15), Gravity.CENTER));
        addView(listHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        Async.run(() -> getPasar(), (List<JsonObject> dataPasar) -> {
            dataPasarState = dataPasar;
            dataPasarImmutableState = dataPasar;
            isReadyToShowState = true;
            render();
        });
    }

    public void setPasarSelected(String pasarSelected) {
        this.pasarSelected = pasarSelected;
        if (isReadyToShowState) {
            render();
        }
    }

    private List<JsonObject> getPasar() {
        String params = HTTPRequest.getQueryString("jenisPasar=pasarTersedia");
        Http.Response httpResponse = Http.getApi("/Pasar/getPasar?" + params);
        List<JsonObject> out = new ArrayList<>();
        if (httpResponse.statusCode == 200) {
            JsonArray dataPasar = Json.array(httpResponse.jsonObject(), "dataPasar");
            for (int i = 0; i < dataPasar.size(); i++) {
                out.add(Json.objAt(dataPasar, i));
            }
        }
        return out;
    }

    private void render() {
        Context ctx = getContext();
        removeAllViews();
        if (!isReadyToShowState) {
            return;
        }

        pencarian = new Pencarian(ctx, string -> {
            pencarianString = string;
            List<JsonObject> filtered = new ArrayList<>();
            for (JsonObject pasar : dataPasarImmutableState) {
                String namaPasarLC = Json.str(pasar, "nama").toLowerCase();
                if (namaPasarLC.indexOf(string) >= 0) {
                    filtered.add(pasar);
                }
            }
            dataPasarState = filtered;
            render();
        }, () -> {
            dataPasarState = dataPasarImmutableState;
            pencarianString = "";
            render();
        }, "Type Here", pencarianString);
        addView(pencarian, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        if (dataPasarState.size() >= 1) {
            LinearLayout row = Ui.row(ctx);
            for (JsonObject pasarItem : dataPasarState) {
                row.addView(item(ctx, pasarItem), new LinearLayout.LayoutParams(Ui.dp(ctx, hb * 25f), Ui.MATCH));
            }
            HorizontalScrollView scroll = new HorizontalScrollView(ctx);
            scroll.setHorizontalScrollBarEnabled(false);
            scroll.addView(row, new HorizontalScrollView.LayoutParams(Ui.WRAP, Ui.MATCH));
            addView(scroll, new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f));
        } else {
            LinearLayout empty = Ui.column(ctx);
            empty.setGravity(Gravity.CENTER);
            ImageView img = Ui.asset(ctx, "empty.png", hb * 25f);
            img.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(ctx, hb * 25f), Ui.WRAP));
            empty.addView(img);
            empty.addView(Ui.text(ctx, "Tidak ada pasar yang bisa dipilih", hb * 3f, 0xFF000000));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f);
            lp.topMargin = Ui.dp(ctx, 5);
            lp.bottomMargin = Ui.dp(ctx, 5);
            addView(empty, lp);
        }
    }

    private View item(Context ctx, final JsonObject pasarItem) {
        final String id = Json.str(pasarItem, "id");
        LinearLayout box = Ui.column(ctx);
        box.setGravity(Gravity.CENTER);
        Ui.pad(box, 5, 0, 5, 0);
        if (id.equals(pasarSelected)) {
            box.setBackground(Ui.shape(ctx, 0x7766CC99, 5f));
        }

        String pasarImg = ServerPath.baseURL + "/" + ServerPath.uploadGambarPasar + "/compress/"
                + Json.str(pasarItem, "gambar");
        ImageView iv = new ImageView(ctx);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Ui.loadNetworkCircle(iv, pasarImg);
        box.addView(iv, new LinearLayout.LayoutParams(Ui.dp(ctx, hb * 15f), Ui.dp(ctx, hb * 15f)));
        box.addView(Ui.spaceH(ctx, 7.5f));

        TextView nama = Ui.text(ctx, Json.str(pasarItem, "nama"), new Txt.Style(hb * 3f).color(0xFF000000));
        nama.setGravity(Gravity.CENTER);
        nama.setSingleLine(true);
        nama.setEllipsize(android.text.TextUtils.TruncateAt.END);
        box.addView(nama, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));

        box.setClickable(true);
        box.setOnClickListener(v -> {
            if (onPasarClicked != null) {
                onPasarClicked.run(id);
            }
        });
        return box;
    }
}
