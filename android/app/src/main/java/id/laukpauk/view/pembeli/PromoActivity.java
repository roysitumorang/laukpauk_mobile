package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;

import id.laukpauk.core.AppState;
import id.laukpauk.core.Json;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Header;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.promo.ProdukPromo;

/** Port of lib/views/pembeli/Promo.dart (a Footer tab). No args. */
public class PromoActivity extends BaseActivity implements AppState.Listener {
    private FrameLayout bodyHolder;
    private ProdukPromo produkPromo;
    private int shownState = -1; // 1 = ProdukPromo, 0 = ask for ADJD

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = Ui.column(this);
        root.addView(new Header(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        bodyHolder = new FrameLayout(this);
        root.addView(bodyHolder, Ui.weightH(1f));
        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);
        renderBody(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppState.get().addListener(this);
    }

    @Override
    protected void onStop() {
        AppState.get().removeListener(this);
        super.onStop();
    }

    /** Dart: Consumer&lt;HeaderViewModel&gt; rebuild. */
    @Override
    public void onChanged() {
        renderBody(true);
    }

    private void renderBody(boolean fromChange) {
        boolean isADJDSet = AppState.get().isADJDSet();
        int state = isADJDSet ? 1 : 0;
        if (state == shownState) {
            if (fromChange && produkPromo != null) {
                produkPromo.refresh(); // didUpdateWidget -> initialization()
            }
            return;
        }
        shownState = state;
        bodyHolder.removeAllViews();
        produkPromo = null;
        if (isADJDSet) {
            produkPromo = new ProdukPromo(this);
            bodyHolder.addView(produkPromo, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            return;
        }

        float hb = Ui.hb(this);
        LinearLayout column = Ui.column(this);
        column.setGravity(Gravity.CENTER);

        LinearLayout inner = Ui.column(this);
        inner.setGravity(Gravity.CENTER_HORIZONTAL);
        Ui.padXY(inner, hb * 15f, 0);
        inner.addView(Ui.asset(this, "adjd-belum-ditentukan.png", hb * 35f));
        inner.addView(Ui.spaceH(this, 5));
        android.widget.TextView text = Ui.text(this,
                "Oops, anda harus menentukan lokasi anda untuk melihat promo kami");
        text.setGravity(Gravity.CENTER);
        inner.addView(text, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        column.addView(inner, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        column.addView(Ui.spaceH(this, 15));
        View button = CustomButton.button(this, "Tentukan Lokasi dan Jadwal Delivery", () -> {
            HeaderModel.adjd(this, null, null, null, (JsonObject adjdSet) -> {
                if (adjdSet == null) {
                    return;
                }
                String latitude = Json.strOrNull(adjdSet, "latitude");
                String longitude = Json.strOrNull(adjdSet, "longitude");
                String jadwalDeliveryCode = Json.strOrNull(adjdSet, "kodeJadwalDelivery");

                boolean isADJDCompleted = false;
                if (latitude != null && longitude != null && jadwalDeliveryCode != null) {
                    isADJDCompleted = true;
                }
                AppState.get().setADJDSet(isADJDCompleted);
            });
        }, new CustomButton.Opts().color(CustomButton.themeColor).pill());
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
        btnLp.gravity = Gravity.CENTER_HORIZONTAL;
        column.addView(button, btnLp);

        bodyHolder.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }
}
