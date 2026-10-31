package id.laukpauk.model;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import id.laukpauk.core.Async;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Ui;

/** Port of lib/models/PaketModel.dart. */
public final class PaketModel {
    private PaketModel() {}

    /**
     * Dart: addToCartSnackBar({context, text}) -> SnackBar (the caller then did
     * ScaffoldMessenger.of(context).showSnackBar(snackBar)).
     *
     * Java: builds the 3 second "text / Lihat Keranjang" snack bar AND shows it (main thread);
     * the returned Snackbar is only for callers that want to keep or dismiss it, calling
     * {@code show()} on it again is harmless.
     */
    public static Snackbar addToCartSnackBar(BaseActivity activity, String text) {
        return ShopModelUi.keranjangSnackbar(activity, text, 3);
    }

    /** Dart: detailPaket(idPaket) -> Future&lt;dynamic&gt;. BLOCKING. `detailPaket`, or false on failure. */
    public static JsonElement detailPaket(String idPaket) {
        idPaket = idPaket == null ? "" : idPaket;

        String params = HTTPRequest.getQueryString("idPaket=" + idPaket);

        Http.Response httpResponse = Http.getApi("/Produk/detailPaket?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return ShopModelUi.dyn(dataObject.get("detailPaket"));
        }

        return ShopModelUi.FALSE;
    }

    /** Dart: addToCart(idPaket) -> Future&lt;bool&gt;. BLOCKING. */
    public static boolean addToCart(String idPaket) {
        String cartCode = LocalStorage.getCartCode();

        JsonObject jadwalDeliveryTerpilih = LocalStorage.savedJadwalDeliveryMap();
        JsonObject savedADJD = LocalStorage.getSavedLatitudeAndLongitude();

        String dayDelivery = (jadwalDeliveryTerpilih != null)
                ? Json.str(jadwalDeliveryTerpilih, "day")
                : "Hari Pengantaran";
        String jadwalDeliveryCode = Json.strOrNull(savedADJD, "jadwalDeliveryCode");
        if (jadwalDeliveryCode == null) {
            return false; // Dart: `String jadwalDeliveryCode = savedADJD[...]` throws on null
        }

        Map<String, String> body = new LinkedHashMap<>();
        body.put("cartCode", cartCode);
        body.put("idProduk", idPaket);
        body.put("jenisProduk", "paket");
        body.put("quantity", "1");
        body.put("jadwalDeliveryCode", jadwalDeliveryCode);
        body.put("dayDelivery", dayDelivery);
        Map<String, String> httpBody = HTTPRequest.postBody(body);

        Http.Response httpResponse = Http.postApi("/Keranjang/saveCart", httpBody);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.bool(dataObject, "statusAddCart");
        }
        return false;
    }

    /**
     * Dart: adjdNotSetAlert(context, {namaProduk = ''}) -> Future&lt;bool?&gt; (a Material
     * AlertDialog). `done` receives false ("Tutup"), true ("Tentukan Alamat &amp; Jadwal") or null
     * (dismissed by tapping outside).
     */
    public static void adjdNotSetAlert(final BaseActivity activity, String namaProduk,
                                       Async.Done<Boolean> done) {
        final String nama = namaProduk == null ? "" : namaProduk;
        final float hb = Ui.hb(activity);

        Modal.<Boolean>dialog(activity, true, handle -> {
            View actions = ShopModelUi.alertActions(activity,
                    CustomAlert.textButton(activity, "Tutup", () -> handle.close(Boolean.FALSE)),
                    CustomAlert.textButton(activity, "Tentukan Alamat & Jadwal",
                            () -> handle.close(Boolean.TRUE)));
            return ShopModelUi.alertDialogCard(activity, "Lauk Pauk",
                    "Mohon maaf, untuk dapat mengorder produk " + nama.toUpperCase(Locale.ROOT)
                            + " anda harus menentukan alamat dan jadwal delivery terlebih dahulu !",
                    hb * 4f, hb * 3.5f, actions);
        }, done);
    }

    public static void adjdNotSetAlert(BaseActivity activity, Async.Done<Boolean> done) {
        adjdNotSetAlert(activity, "", done);
    }
}
