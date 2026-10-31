package id.laukpauk.model;

import com.google.gson.JsonObject;

import java.util.Map;

import id.laukpauk.core.Async;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;

/** Port of lib/models/UpdateHargaModel.dart. */
public final class UpdateHargaModel {
    private UpdateHargaModel() {}

    /** Dart: hapusSatuan(idSatuanProduk) -> Future&lt;bool&gt;. BLOCKING. */
    public static boolean hapusSatuan(String idSatuanProduk) {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi(
                "/HargaProduk/hapusHargaProduk/" + idSatuanProduk + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.bool(dataObject, "statusHapusHargaProduk");
        }

        return false;
    }

    /**
     * Dart: konfirmasiHapusSatuan(context, Map dataSatuan) -> Future&lt;bool&gt;.
     * `done` gets true only when the user answered "Ya" (false otherwise).
     */
    public static void konfirmasiHapusSatuan(BaseActivity activity, JsonObject dataSatuan,
                                             final Async.Done<Boolean> done) {
        String title = "Satuan Produk";
        String subTitle = "Apakah anda yakin akan menghapus satuan " + Json.str(dataSatuan, "nama") + " ?";
        CustomAlert.konfirmasi(activity, title, subTitle, null, konfirmasi -> {
            if (done != null) {
                done.run(konfirmasi != null && konfirmasi);
            }
        });
    }

    /**
     * Dart: editSatuanProduk(String? idSatuanProduk, Map dataSatuanProduk) -> Future&lt;bool&gt;.
     * BLOCKING. A null idSatuanProduk creates a new unit.
     */
    public static boolean editSatuanProduk(String idSatuanProduk, JsonObject dataSatuanProduk) {
        String params = HTTPRequest.getQueryString(null, true);
        Map<String, String> body = HTTPRequest.postBody(ShopModelUi.form(dataSatuanProduk));

        String urlOptions = (idSatuanProduk != null) ? "/" + idSatuanProduk : "";

        Http.Response httpResponse = Http.postApi(
                "/HargaProduk/saveHargaProduk" + urlOptions + "?" + params, body);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.bool(dataObject, "statusSaveHargaProduk");
        }

        return false;
    }
}
