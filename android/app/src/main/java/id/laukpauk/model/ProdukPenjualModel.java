package id.laukpauk.model;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.Async;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Ui;

/** Port of lib/models/ProdukPenjualModel.dart. */
public final class ProdukPenjualModel {
    private ProdukPenjualModel() {}

    /**
     * Dart: toggleProdukPalingLaris(idProduk, jenisProduk) -> Future&lt;bool&gt;. BLOCKING.
     * jenisProduk 'satuan' toggles a product, anything else a package.
     */
    public static boolean toggleProdukPalingLaris(String idProduk, String jenisProduk) {
        String params = HTTPRequest.getQueryString(null, true);

        boolean satuan = jenisProduk.equalsIgnoreCase("satuan");
        String endPoint = satuan ? "toggleProdukPalingLaris" : "togglePaketPalingLaris";

        Http.Response httpResponse = Http.getApi("/Produk/" + endPoint + "/" + idProduk + "?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();

            if (satuan) {
                return Json.bool(dataObject, "statusToggleProdukPalingLaris");
            } else {
                return Json.bool(dataObject, "statusTogglePaketPalingLaris");
            }
        }

        return false;
    }

    /** Dart: ubahKategoriProduk(idProduk, idKategoriProdukBaru) -> Future&lt;bool&gt;. BLOCKING. */
    public static boolean ubahKategoriProduk(String idProduk, String idKategoriProdukBaru) {
        String params = HTTPRequest.getQueryString(null, true);
        Map<String, String> data = new LinkedHashMap<>();
        data.put("kategoriProduk", idKategoriProdukBaru);
        Map<String, String> body = HTTPRequest.postBody(data);

        Http.Response httpResponse = Http.postApi("/Produk/ubahKategoriProduk/" + idProduk + "?" + params, body);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.bool(dataObject, "statusUbahKategoriProduk");
        }

        return false;
    }

    /** Dart: ubahDeskripsiProduk(idProduk, jenisProduk, deskripsiProduk) -> Future&lt;bool&gt;. BLOCKING. */
    public static boolean ubahDeskripsiProduk(String idProduk, String jenisProduk,
                                              String deskripsiProduk) {
        String params = HTTPRequest.getQueryString(null, true);
        boolean satuan = jenisProduk.equalsIgnoreCase("satuan");
        String endPoint = satuan ? "ubahDeskripsiProduk" : "ubahDeskripsiPaket";

        Map<String, String> data = new LinkedHashMap<>();
        data.put(satuan ? "deskripsiProduk" : "deskripsiPaket", deskripsiProduk);

        Map<String, String> body = HTTPRequest.postBody(data);

        Http.Response httpResponse = Http.postApi("/Produk/" + endPoint + "/" + idProduk + "?" + params, body);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            if (satuan) {
                return Json.bool(dataObject, "statusUbahDeskripsiProduk");
            } else {
                return Json.bool(dataObject, "statusUbahDeskripsiPaket");
            }
        }

        return false;
    }

    /**
     * Dart: showKonfirmasi(context, {title, content, actions: List&lt;TextButton&gt;}) -> Future&lt;bool?&gt;
     * (a Material AlertDialog, dismissible by tapping outside). `title` / `content` may be null
     * (shown as ''). The Dart TextButtons called Navigator.pop(context, value): here `actions`
     * builds the buttons row from the dialog handle, e.g.
     * <pre>
     *   handle -> ShopModelUi.alertActions(activity,
     *       CustomAlert.textButton(activity, "Batal", () -> handle.close(Boolean.FALSE)),
     *       CustomAlert.textButton(activity, "Lanjutkan", () -> handle.close(Boolean.TRUE)))
     * </pre>
     * (may be null = no buttons). `done` gets the closed value, or null when dismissed.
     */
    public static void showKonfirmasi(final BaseActivity activity, final String title,
                                      final String content, final Modal.Content<Boolean> actions,
                                      Async.Done<Boolean> done) {
        final float hb = Ui.hb(activity);
        Modal.<Boolean>dialog(activity, true, handle -> ShopModelUi.alertDialogCard(activity,
                title == null ? "" : title, content == null ? "" : content, hb * 4f, hb * 3.5f,
                actions == null ? null : actions.build(handle)), done);
    }

    /** Dart: hapusProduk(idProduk) -> Future&lt;bool&gt;. BLOCKING. */
    public static boolean hapusProduk(String idProduk) {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/Produk/hapusProduk/" + idProduk + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.bool(dataObject, "statusHapusProduk");
        }

        return false;
    }

    /** Dart: hapusPaket(idProduk) -> Future&lt;bool&gt;. BLOCKING. */
    public static boolean hapusPaket(String idProduk) {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/Produk/hapusPaket/" + idProduk + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.bool(dataObject, "statusHapusPaket");
        }

        return false;
    }
}
