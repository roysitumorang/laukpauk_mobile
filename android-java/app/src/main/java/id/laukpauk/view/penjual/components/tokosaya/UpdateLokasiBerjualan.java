package id.laukpauk.view.penjual.components.tokosaya;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.ADJDModel;
import id.laukpauk.model.TokoSayaModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.PinGMAPSActivity;
import id.laukpauk.view.pembeli.components.otpverifikasiuser.ListPasar;
import id.laukpauk.view.penjual.PenjualUi;

/**
 * Port of lib/views/penjual/components/TokoSaya/UpdateLokasiBerjualan.dart (bottom sheet content).
 * `statusUbahLokasiBerjualanCallback` receives the result of TokoSayaModel.ubahLokasiBerjualan.
 */
public class UpdateLokasiBerjualan extends LinearLayout {
    private final JsonObject dataPenjual;
    private final Async.Done<Boolean> statusUbahLokasiBerjualanCallback;

    /** Map (lokasi tersendiri: latitude, longitude, readAbleAddress) or String (id pasar) or null. */
    private Object lokasiBaruBerjualanSelected;
    private String lokasiBerjualanTerpilih = "pasarTersedia";

    public UpdateLokasiBerjualan(Context ctx, JsonObject dataPenjual,
                                 Async.Done<Boolean> statusUbahLokasiBerjualanCallback) {
        super(ctx);
        this.dataPenjual = dataPenjual;
        this.statusUbahLokasiBerjualanCallback = statusUbahLokasiBerjualanCallback;
        setOrientation(VERTICAL);
        render();
    }

    private TextView judul(String text, Integer color) {
        Context ctx = getContext();
        return PenjualUi.text(ctx, text, 3.5f, color == null ? CustomColors.nearlyBlack : color, true);
    }

    private void render() {
        removeAllViews();
        final Context ctx = getContext();
        Ui.pad(this, 15, 20, 15, 20);

        LinearLayout row = Ui.row(ctx);
        TextView pasarTersedia = judul("Pasar Tersedia", CustomColors.textInfo);
        Ui.onTap(pasarTersedia, () -> {
            lokasiBerjualanTerpilih = "pasarTersedia";
            lokasiBaruBerjualanSelected = null;
            render();
        });
        row.addView(pasarTersedia);
        row.addView(Ui.spacer(ctx));
        TextView tersendiri = judul("Lokasi Tersendiri", CustomColors.textPrimary);
        Ui.onTap(tersendiri, () -> pilihLokasiTersendiri());
        row.addView(tersendiri);
        addView(row, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        addView(Ui.spaceH(ctx, 10));

        if (lokasiBerjualanTerpilih.equals("pasarTersedia")) {
            String selected = (lokasiBaruBerjualanSelected instanceof String)
                    ? (String) lokasiBaruBerjualanSelected : null;
            addView(new ListPasar(ctx, selected, (String idPasar) -> {
                lokasiBaruBerjualanSelected = idPasar;
                lokasiBerjualanTerpilih = "pasarTersedia";
                render();
            }), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        } else {
            JsonObject lokasi = (JsonObject) lokasiBaruBerjualanSelected;
            addView(PenjualUi.text(ctx, "Lokasi baru anda berjualan di sekitar jalan "
                    + Json.str(lokasi, "readAbleAddress"), 3.25f, 0xDD000000, false));
        }

        if (lokasiBaruBerjualanSelected != null) {
            FrameLayout center = new FrameLayout(ctx);
            Ui.pad(center, 0, 5, 0, 0);
            TextView tetapkan = PenjualUi.textButton(ctx, "Tetapkan Lokasi Baru Berjualan", Ui.hb(ctx) * 3.5f,
                    CustomColors.green, true, () -> tetapkan());
            center.addView(tetapkan, new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, android.view.Gravity.CENTER));
            addView(center, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
    }

    private void pilihLokasiTersendiri() {
        final BaseActivity activity = (BaseActivity) getContext();
        Nav.pushForResult(activity, PinGMAPSActivity.class, null, result -> {
            if (result != null && result.isJsonObject()) {
                JsonObject pinnedLocationMap = result.getAsJsonObject();
                final String latitude = Json.str(pinnedLocationMap, "lat");
                final String longitude = Json.str(pinnedLocationMap, "lng");

                activity.async(() -> ADJDModel.reverseGeocoding(latitude, longitude), pinnedLocationString -> {
                    JsonObject lokasiBaru = new JsonObject();
                    lokasiBaru.addProperty("latitude", latitude);
                    lokasiBaru.addProperty("longitude", longitude);
                    lokasiBaru.addProperty("readAbleAddress", pinnedLocationString);

                    lokasiBaruBerjualanSelected = lokasiBaru;
                    lokasiBerjualanTerpilih = "lokasiTersendiri";
                    render();
                });
            }
        });
    }

    private void tetapkan() {
        final BaseActivity activity = (BaseActivity) getContext();
        final String idPenjual = Json.str(dataPenjual, "id");
        final JsonObject lokasiBaru = new JsonObject();
        lokasiBaru.addProperty("jenisPasarBaru", lokasiBerjualanTerpilih);
        if (lokasiBerjualanTerpilih.equals("lokasiTersendiri")) {
            JsonObject selected = (JsonObject) lokasiBaruBerjualanSelected;
            lokasiBaru.addProperty("latPasarBaru", Json.str(selected, "latitude"));
            lokasiBaru.addProperty("longPasarBaru", Json.str(selected, "longitude"));
        } else {
            lokasiBaru.addProperty("idPasarBaru", (String) lokasiBaruBerjualanSelected);
        }

        activity.async(() -> TokoSayaModel.ubahLokasiBerjualan(idPenjual, lokasiBaru), result ->
                statusUbahLokasiBerjualanCallback.run(result));
    }
}
