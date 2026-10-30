package id.laukpauk.view.pembeli.components.produkdidalampasar;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;

/** Port of lib/views/pembeli/components/ProdukDiDalamPasar/PasarLainDalamCA.dart. */
public class PasarLainDalamCA extends PasarLainSection {
    private final String currentPasar;

    public PasarLainDalamCA(Context ctx, String currentPasar) {
        super(ctx, "Pasar Lain di Sekitar Anda", "Yah, belum ada pasar disekitarmu mak ..", 30.25f, true);
        this.currentPasar = currentPasar;
        start();
    }

    @Override
    JsonArray fetchPasar() {
        boolean isADJDSet = LocalStorage.isAlamatDeliveryDanJadwalDeliverySet();
        if (isADJDSet) {
            JsonObject savedADJD = LocalStorage.getSavedLatitudeAndLongitude();
            String latitude = Json.strOrNull(savedADJD, "latitude");
            String longitude = Json.strOrNull(savedADJD, "longitude");
            String jadwalDeliveryCode = Json.strOrNull(savedADJD, "jadwalDeliveryCode");
            if (latitude == null || longitude == null || jadwalDeliveryCode == null) {
                return new JsonArray();
            }

            String excludePasar = currentPasar != null ? currentPasar : "";

            String params = HTTPRequest.getQueryString("latitude=" + latitude + "&longitude=" + longitude
                    + "&exclude=" + excludePasar + "&jadwalDeliveryCode=" + jadwalDeliveryCode);

            Http.Response httpResponse = Http.getApi("/Pasar/getPasarAroundOfThisLatLong?" + params);
            if (httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                return Json.array(dataObject, "listPasar");
            }
        }
        return new JsonArray();
    }
}
