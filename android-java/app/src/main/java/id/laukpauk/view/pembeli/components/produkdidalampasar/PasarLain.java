package id.laukpauk.view.pembeli.components.produkdidalampasar;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.AppState;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/components/ProdukDiDalamPasar/PasarLain.dart (Consumer of HeaderViewModel). */
public class PasarLain extends LinearLayout implements AppState.Listener {
    private final String idPasar;
    private Boolean renderedFor; // the isADJDSet value the current children were built for

    public PasarLain(Context ctx, String idPasar) {
        super(ctx);
        setOrientation(VERTICAL);
        this.idPasar = idPasar;
        render();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        AppState.get().addListener(this);
        render();
    }

    @Override
    protected void onDetachedFromWindow() {
        AppState.get().removeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onChanged() {
        render();
    }

    private void render() {
        final Context ctx = getContext();
        boolean isADJDSet = AppState.get().isADJDSet();
        if (renderedFor != null && renderedFor == isADJDSet) {
            return;
        }
        renderedFor = isADJDSet;
        removeAllViews();
        float hb = Ui.hb(ctx);

        if (isADJDSet) {
            LinearLayout a = Ui.column(ctx);
            Ui.pad(a, 10, 15, 10, 15);
            a.addView(new PasarLainDalamCA(ctx, idPasar), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            addView(a, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

            LinearLayout b = Ui.column(ctx);
            Ui.pad(b, 10, 10, 10, 10);
            b.addView(new PasarLainLuarCA(ctx), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            addView(b, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        } else {
            LinearLayout box = Ui.column(ctx);
            box.setGravity(Gravity.CENTER_HORIZONTAL);
            Ui.pad(box, 15, 10, 15, 20);
            box.addView(Ui.asset(ctx, "adjd-belum-ditentukan.png", hb * 25));
            box.addView(Ui.spaceH(ctx, 5));
            TextView info = Ui.text(ctx,
                    "Anda belum menentukan lokasi dan jadwal pengantaran. Untuk melihat pasar lain yang berada didalam dan diluar area anda silahkan tentukan terlebih dahulu lokasi dan jadwal pengantaran.",
                    hb * 2.75f, CustomColors.textMuted);
            info.setGravity(Gravity.CENTER);
            box.addView(info, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            box.addView(Ui.spaceH(ctx, 15));
            box.addView(CustomButton.button(ctx, "Tentukan Lokasi dan Alamat Pengantaran", this::tentukanADJD,
                    new CustomButton.Opts().pill().color(CustomButton.themeColor).size(CustomButton.smSize)));
            addView(box, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
    }

    private void tentukanADJD() {
        BaseActivity activity = (BaseActivity) getContext();
        HeaderModel.adjd(activity, null, null, null, (JsonObject adjdSet) -> {
            String latitude = Json.strOrNull(adjdSet, "latitude");
            String longitude = Json.strOrNull(adjdSet, "longitude");
            String kodeJadwalDelivery = Json.strOrNull(adjdSet, "kodeJadwalDelivery");

            if (latitude != null && longitude != null && kodeJadwalDelivery != null) {
                AppState.get().setADJDSet(true);
            }
        });
    }
}
