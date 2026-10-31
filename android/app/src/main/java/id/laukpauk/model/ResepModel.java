package id.laukpauk.model;

import android.view.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

import id.laukpauk.core.Async;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Modal;

/** Port of lib/models/ResepModel.dart. */
public final class ResepModel {
    private ResepModel() {}

    /** Result of the background part of getResep: the list, or the text of the exception Dart would have caught. */
    private static final class ResepResult {
        JsonArray data = new JsonArray();
        String exception;
    }

    /**
     * Dart: getResep(context) -> Future&lt;List&gt;. Any failure (no connection, a status other
     * than 200, an unexpected body) shows the "Exception Occured" dialog (not awaited, as in
     * Dart) and yields an empty list.
     */
    public static void getResep(final BaseActivity activity, final Async.Done<JsonArray> done) {
        activity.async(() -> {
            ResepResult result = new ResepResult();
            Http.Response httpResponse = Http.getApi("/Resep/getResep");
            if (httpResponse.statusCode == 0) {
                result.exception = "SocketException: Connection failed";
            } else if (httpResponse.statusCode == 200) {
                JsonElement dataResep = Json.get(httpResponse.jsonObject(), "dataResep");
                if (dataResep != null && dataResep.isJsonArray()) {
                    result.data = dataResep.getAsJsonArray();
                } else {
                    result.exception = "type 'Null' is not a subtype of type 'List<dynamic>'";
                }
            } else {
                // _returnResponse() returned null -> assigning it to `List` threw a TypeError
                result.exception = "type 'Null' is not a subtype of type 'List<dynamic>'";
            }
            return result;
        }, result -> {
            if (result == null) {
                if (done != null) {
                    done.run(new JsonArray());
                }
                return;
            }
            if (result.exception != null) {
                final String stringException = result.exception;
                Modal.<Object>dialog(activity, true, handle -> ShopModelUi.alertDialogCard(activity,
                        "Exception Occured", stringException, 20f, 16f, null), null);
            }
            if (done != null) {
                done.run(result.data);
            }
        });
    }

    /** Dart: getResepItem(idResep) -> Future&lt;List&gt;. BLOCKING. */
    public static JsonArray getResepItem(String idResep) {
        String entitasResepItem = "id, namaProduk, takaran, idProduk";
        String params = HTTPRequest.getQueryString("entitasResepItem=" + entitasResepItem);

        Http.Response httpResponse = Http.getApi(
                "/Resep/getResepItemFromThisResep/" + idResep + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "dataResepItem");
        }

        return new JsonArray();
    }

    /**
     * Dart: belanjaResep(context, Map httpBody) -> Future&lt;bool&gt;. `done` gets the
     * `statusBelanjaResep` flag (false on failure).
     */
    public static void belanjaResep(BaseActivity activity, final JsonObject httpBody,
                                    final Async.Done<Boolean> done) {
        activity.async(() -> {
            Map<String, String> body = HTTPRequest.postBody(ShopModelUi.form(httpBody));

            Http.Response httpResponse = Http.postApi("/Keranjang/belanjaResep", body);

            if (httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                return Json.bool(dataObject, "statusBelanjaResep");
            }

            return false;
        }, statusBelanjaResep -> {
            if (done != null) {
                done.run(statusBelanjaResep != null && statusBelanjaResep);
            }
        });
    }
}
