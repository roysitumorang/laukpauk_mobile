package id.laukpauk.view.pembeli.components.produkdidalampasar;

import id.laukpauk.core.AppContainer;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;

/** Port of lib/views/pembeli/components/ProdukDiDalamPasar/PasarLainLuarCA.dart. */
public class PasarLainLuarCA extends PasarLainSection {
    /** Dependencies of this component, resolved once from its Context (see AppContainer). */
    private final AppContainer deps = AppContainer.from(getContext());


    public PasarLainLuarCA(Context ctx) {
        super(ctx, "Pasar Lain di Luar Area Anda", "Yaahh.. belum ada pasar diluar area mu mak...", 28f, false);
        start();
    }

    @Override
    JsonArray fetchPasar() {
        boolean isADJDSet = deps.localStorage.isAlamatDeliveryDanJadwalDeliverySet();
        if (isADJDSet) {
            JsonObject savedADJD = deps.localStorage.getSavedLatitudeAndLongitude();
            String latitude = Json.strOrNull(savedADJD, "latitude");
            String longitude = Json.strOrNull(savedADJD, "longitude");
            String jadwalDeliveryCode = Json.strOrNull(savedADJD, "jadwalDeliveryCode");
            if (latitude == null || longitude == null || jadwalDeliveryCode == null) {
                return new JsonArray();
            }

            String params = deps.httpRequest.getQueryString("latitude=" + latitude + "&longitude=" + longitude
                    + "&jadwalDeliveryCode=" + jadwalDeliveryCode);

            Http.Response httpResponse = deps.http.getApi("/Pasar/getPasarOverOfThisLatLong?" + params);
            if (httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                return Json.array(dataObject, "listPasar");
            }
        }
        return new JsonArray();
    }
}
