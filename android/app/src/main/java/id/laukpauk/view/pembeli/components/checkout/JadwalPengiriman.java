package id.laukpauk.view.pembeli.components.checkout;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.AppState;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.model.ADJDModel;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.SplashScreenActivity;

/**
 * Port of lib/views/pembeli/components/Checkout/JadwalPengiriman.dart: shows the saved delivery schedule and
 * address and lets the user change them ("Ubah Alamat dan Jadwal"). The saved schedule is reported to the
 * parent once at start (Dart initState) through `jadwalPengirimanTerpilih` (null = none saved).
 */
public class JadwalPengiriman extends LinearLayout {
    private final Async.Done<JsonObject> jadwalPengirimanTerpilih;
    private boolean readyToShow = true;
    private final LinearLayout body;

    public JadwalPengiriman(Context ctx, Async.Done<JsonObject> jadwalPengirimanTerpilih) {
        super(ctx);
        this.jadwalPengirimanTerpilih = jadwalPengirimanTerpilih;
        setOrientation(VERTICAL);
        float hb = Ui.hb(ctx);

        TextView title = CheckoutUi.text(ctx, "Jadwal Pengiriman", hb * 4, CheckoutUi.TEXT, true);
        TextView ubah = CheckoutUi.text(ctx, "Ubah Alamat dan Jadwal", hb * 3.0f, CustomColors.green, true);
        ubah.setOnClickListener(v -> onUbah());
        addView(CheckoutUi.spaceBetween(ctx, title, ubah), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        addView(Ui.spaceH(ctx, 10));

        body = Ui.column(ctx);
        addView(body, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        renderBody();

        // initJadwalPengirimanToParent()
        final JsonObject saved = LocalStorage.savedJadwalDeliveryMap();
        Async.main(() -> {
            if (this.jadwalPengirimanTerpilih != null) {
                this.jadwalPengirimanTerpilih.run(saved);
            }
        });
    }

    private void renderBody() {
        Context ctx = getContext();
        float hb = Ui.hb(ctx);
        body.removeAllViews();
        if (!readyToShow) {
            body.addView(CheckoutUi.spinner(ctx, hb * 4.0f, hb * 4.5f),
                    new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            return;
        }

        JsonObject savedJadwal = LocalStorage.savedJadwalDeliveryMap();
        if (savedJadwal != null) {
            LinearLayout box = Ui.column(ctx);
            Ui.background(box, 0x35FFC107, 10);
            Ui.pad(box, 15, 10, 15, 10);
            box.addView(CheckoutUi.text(ctx, "Pengantaran " + Json.str(savedJadwal, "day") + " "
                    + Json.str(savedJadwal, "sessi"), hb * 3.5f, CustomColors.grey, true));
            TextView est = CheckoutUi.text(ctx, " Estimasi Jam " + Json.str(savedJadwal, "rentangWaktu")
                    + " WIB", hb * 2.75f, CustomColors.grey, false);
            est.setTypeface(est.getTypeface(), android.graphics.Typeface.ITALIC);
            box.addView(est, CheckoutUi.lp(Ui.WRAP, Ui.WRAP, 0, Ui.dp(ctx, 3), 0, 0));
            body.addView(box, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
        body.addView(Ui.spaceH(ctx, 5));

        JsonObject savedLatLong = LocalStorage.getSavedLatitudeAndLongitude();
        if (savedLatLong != null) {
            String alamat = Json.strOrNull(savedLatLong, "alamat");
            LinearLayout box = Ui.column(ctx);
            Ui.background(box, 0x3517A2B8, 10);
            Ui.pad(box, 15, 10, 15, 10);
            box.addView(CheckoutUi.text(ctx, "Alamat Pengantaran", hb * 3.5f, CustomColors.grey, true));
            if (alamat != null) {
                box.addView(CheckoutUi.text(ctx, alamat, hb * 2.75f, CustomColors.grey, false),
                        CheckoutUi.lp(Ui.WRAP, Ui.WRAP, 0, Ui.dp(ctx, 3), 0, 0));
            }
            body.addView(box, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
    }

    private void onUbah() {
        final BaseActivity a = CheckoutUi.activity(getContext());
        if (a == null) {
            return;
        }
        Modal.Content<Boolean> actions = handle -> CustomAlert.actionsRow(a,
                CustomButton.button(a, "Batal", () -> handle.close(Boolean.FALSE),
                        new CustomButton.Opts().color(CustomColors.textMuted).outline()),
                Ui.spaceW(a, 5),
                CustomButton.button(a, "Lanjutkan", () -> handle.close(Boolean.TRUE),
                        new CustomButton.Opts().color(CustomColors.green)));
        CustomAlert.<Boolean>textComponent(a, null, "Ubah Alamat dan Jadwal",
                "Jika anda mengganti alamat / jam delivery, seluruh pesanan di keranjang akan terhapus otomatis.",
                actions, null, false, (Boolean konfirmasi) -> {
                    if (konfirmasi == null || !konfirmasi) {
                        return;
                    }
                    readyToShow = false;
                    renderBody();
                    a.async(() -> {
                        ADJDModel.deleteAlamatDanJadwalDelivery();
                        return Boolean.TRUE;
                    }, ok -> HeaderModel.adjd(a, null, null, null, (JsonObject newADJD) -> {
                        String latitude = Json.strOrNull(newADJD, "latitude");
                        String longitude = Json.strOrNull(newADJD, "longitude");
                        String kodeJadwalDelivery = Json.strOrNull(newADJD, "kodeJadwalDelivery");
                        readyToShow = true;
                        renderBody();
                        if (latitude != null && longitude != null && kodeJadwalDelivery != null) {
                            AppState.get().setADJDSet(true);
                            Nav.pushAndRemoveUntil(a, SplashScreenActivity.class, null);
                        }
                    }));
                });
    }
}
