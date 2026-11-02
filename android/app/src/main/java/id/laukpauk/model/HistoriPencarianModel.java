package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonArray;

import java.util.Calendar;

import id.laukpauk.core.Json;
import id.laukpauk.core.Prefs;

/** Port of lib/models/HistoriPencarianModel.dart (search history kept in SharedPreferences). */
public final class HistoriPencarianModel {
    private final Prefs prefs;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public HistoriPencarianModel(Prefs prefs) {
        this.prefs = prefs;
    }

    /** Items are maps {title, onTime}. */
    public JsonArray getHistoriPencarian() {
        String encodedHistoriPencarian = prefs.getString("historiPencarian");
        if (encodedHistoriPencarian != null) {
            return Json.parseArray(encodedHistoriPencarian);
        }

        return new JsonArray();
    }

    /** Always returns true, like the Dart version. */
    public boolean addHistoriPencarian(String search) {
        String encodedHistoriPencarian = prefs.getString("historiPencarian");

        Calendar now = Calendar.getInstance();

        String jam = String.valueOf(now.get(Calendar.HOUR_OF_DAY));
        String menit = String.valueOf(now.get(Calendar.MINUTE));
        String detik = String.valueOf(now.get(Calendar.SECOND));

        String tanggal = String.valueOf(now.get(Calendar.DAY_OF_MONTH));
        String bulan = String.valueOf(now.get(Calendar.MONTH) + 1);
        String tahun = String.valueOf(now.get(Calendar.YEAR));

        String onTime = jam + ":" + menit + ":" + detik + " " + tanggal + "/" + bulan + "/" + tahun;

        JsonArray decoded;
        if (encodedHistoriPencarian == null) {
            decoded = new JsonArray();
        } else {
            decoded = Json.parseArray(encodedHistoriPencarian);
        }
        decoded.add(Json.of("title", search, "onTime", onTime));
        encodedHistoriPencarian = decoded.toString();

        prefs.setString("historiPencarian", encodedHistoriPencarian);

        return true;
    }
}
