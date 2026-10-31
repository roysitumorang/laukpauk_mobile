package id.laukpauk.core;

import android.app.Activity;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.homepage.JadwalPengantaran;

/**
 * Port of lib/libraries/TanyaJadwal.dart: the content of the "Jadwal Delivery" dialog (schedule picker,
 * delivery address, "Nanti Saja" / "OK" buttons). Navigator.pop(context, value) becomes the
 * {@link #setOnClose} callback: null for "Nanti Saja", {jadwalDeliverySelected, jadwalDeliveryTerpilih} for "OK".
 */
public class TanyaJadwal extends LinearLayout {
    private final Activity activity;
    private final String currentLocation;

    private String kodeJadwalDeliverySelected;
    private JsonObject jadwalDeliveryTerpilih;

    private LinearLayout buttonsRow;
    private Async.Done<JsonObject> onClose;

    public TanyaJadwal(Activity activity, String currentLocation) {
        super(activity);
        this.activity = activity;
        this.currentLocation = currentLocation;
        setOrientation(VERTICAL);
        build();
    }

    /** Receives what Dart passed to Navigator.pop(context, ...). */
    public void setOnClose(Async.Done<JsonObject> onClose) {
        this.onClose = onClose;
    }

    private void close(JsonObject value) {
        if (onClose != null) {
            onClose.run(value);
        }
    }

    private void build() {
        float hb = Ui.hb(activity);

        JadwalPengantaran jadwalPengantaran = new JadwalPengantaran(activity, (JsonObject selectedJadwalDelivery) -> {
            // setState
            kodeJadwalDeliverySelected = Json.strOrNull(selectedJadwalDelivery, "kode");
            jadwalDeliveryTerpilih = selectedJadwalDelivery;
            renderButtons();
        });
        addView(jadwalPengantaran, new LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout info = Ui.column(activity);
        info.setGravity(Gravity.CENTER_HORIZONTAL);
        Ui.pad(info, 0, 15, 0, 15);
        TextView t1 = Ui.text(activity, "Belanjaan anda akan diantar ke sekitar", new Txt.Style(hb * 3.0f));
        t1.setGravity(Gravity.CENTER);
        info.addView(t1, new LayoutParams(Ui.WRAP, Ui.WRAP));
        info.addView(Ui.spaceH(activity, 5));
        TextView t2 = Ui.text(activity, currentLocation,
                new Txt.Style(hb * 2.75f).color(CustomColors.darkGreen).bold());
        t2.setGravity(Gravity.CENTER);
        info.addView(t2, new LayoutParams(Ui.MATCH, Ui.WRAP));
        addView(info, new LayoutParams(Ui.MATCH, Ui.WRAP));

        buttonsRow = Ui.row(activity);
        buttonsRow.setGravity(Gravity.CENTER);
        addView(buttonsRow, new LayoutParams(Ui.MATCH, Ui.WRAP));
        renderButtons();
    }

    private void renderButtons() {
        buttonsRow.removeAllViews();
        buttonsRow.addView(CustomAlert.textButton(activity, "Nanti Saja", () -> close(null)));
        if (kodeJadwalDeliverySelected != null && jadwalDeliveryTerpilih != null) {
            buttonsRow.addView(CustomAlert.textButton(activity, "OK", () -> {
                JsonObject returnJadwalDelivery = new JsonObject();
                returnJadwalDelivery.addProperty("jadwalDeliverySelected", kodeJadwalDeliverySelected);
                returnJadwalDelivery.add("jadwalDeliveryTerpilih", jadwalDeliveryTerpilih);
                close(returnJadwalDelivery);
            }));
        }
    }
}
