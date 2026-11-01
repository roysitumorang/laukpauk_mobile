package id.laukpauk.model;

import android.view.View;

import com.google.gson.JsonObject;

import java.util.Map;

import id.laukpauk.core.Async;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Modal;
import id.laukpauk.view.penjual.components.tokosaya.UpdateLokasiBerjualan;

/** Port of lib/models/TokoSayaModel.dart. */
public final class TokoSayaModel {
    private TokoSayaModel() {}

    /**
     * Dart: showModalUbahLokasiBerjualan(context, Map dataToko) -> Future&lt;bool?&gt;. Shows the
     * bottom sheet with UpdateLokasiBerjualan; `done` gets the status passed by the sheet
     * (statusUbahLokasiBerjualan) or null when the sheet was dismissed.
     */
    public static void showModalUbahLokasiBerjualan(final BaseActivity activity,
                                                    final JsonObject dataToko,
                                                    Async.Done<Boolean> done) {
        Modal.<Boolean>bottomSheet(activity, true, handle -> {
            View sheet = new UpdateLokasiBerjualan(activity, dataToko,
                    (Boolean statusUbahLokasiBerjualan) -> handle.close(statusUbahLokasiBerjualan));

            return ShopModelUi.roundedSheet(activity, sheet);
        }, done);
    }

    /** Dart: ubahLokasiBerjualan(idPenjual, Map lokasiBaruBerjualan) -> Future&lt;bool&gt;. BLOCKING. */
    public static boolean ubahLokasiBerjualan(String idPenjual, JsonObject lokasiBaruBerjualan) {
        String params = HTTPRequest.getQueryString(null, true);

        Map<String, String> body = HTTPRequest.postBody(ShopModelUi.form(lokasiBaruBerjualan));

        Http.Response httpResponse = Http.postApi(
                "/Penjual/ubahLokasiBerjualan/" + idPenjual + "?" + params, body);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.bool(dataObject, "statusUbahLokasiBerjualan");
        }
        return false;
    }
}
