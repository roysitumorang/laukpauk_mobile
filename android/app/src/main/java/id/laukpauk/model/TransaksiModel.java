package id.laukpauk.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.Async;
import id.laukpauk.core.AppState;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Nav;
import id.laukpauk.view.pembeli.KeranjangActivity;

/** Port of lib/models/TransaksiModel.dart. */
public final class TransaksiModel {
    private TransaksiModel() {}

    /** BLOCKING. Returns dataObject['transaksi'] or the JSON boolean false. */
    public static JsonElement getDetailTransaksi(String idTransaksi, String select) {
        String entitasTransaksiQS = (select == null) ? "" : "entitasTransaksi=" + select;

        String params = HTTPRequest.getQueryString(entitasTransaksiQS, true);

        Http.Response httpResponse = Http.getApi("/Transaksi/getTransaksi/" + idTransaksi + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.get(dataObject, "transaksi");
        }

        return new JsonPrimitive(Boolean.FALSE);
    }

    public static JsonElement getDetailTransaksi(String idTransaksi) {
        return getDetailTransaksi(idTransaksi, null);
    }

    /** Dart: removeTransactionSession(context). UI flow (see CONVENTIONS); `done` runs on the main thread once. */
    public static void removeTransactionSession(BaseActivity activity, final Async.Done<Void> done) {
        // Not tied to the screen's lifetime (Async.run, not activity.async): the session must be cleared even
        // when the screen is gone.
        Async.run(() -> {
            LocalStorage.clearCartCode();
            LocalStorage.clearUsedVoucher();
            ADJDModel.deleteAlamatDanJadwalDelivery();
            ADJDModel.deleteCoppiedADJD();

            CheckoutModel.bersihkanKeranjang();
            return Boolean.TRUE;
        }, ok -> {
            AppState.get().setJumlahCart(0);
            AppState.get().setADJDSet(false);
            if (done != null) {
                done.run(null);
            }
        });
    }

    public static void melewatiBatasRangeJadwalDeliveryBelumCheckout(BaseActivity activity,
                                                                    final Async.Done<Void> done) {
        Async.run(() -> {
            ADJDModel.deleteAlamatDanJadwalDelivery();

            CheckoutModel.bersihkanKeranjang();
            return Boolean.TRUE;
        }, ok -> {
            AppState.get().setJumlahCart(0);
            if (done != null) {
                done.run(null);
            }
        });
    }

    /** BLOCKING. null when the request failed. */
    public static Integer jumlahItemDiantar(String idTransaksi) {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/Transaksi/getJumlahItemDiantar/" + idTransaksi + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.intVal(dataObject, "jumlahBarangDiantar");
        }

        return null;
    }

    /** BLOCKING. null when the request failed. */
    public static Integer getLastTransactionID() {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/Transaksi/getLastTransactionID?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.intVal(dataObject, "lastTransactionID");
        }

        return null;
    }

    /** BLOCKING. null when the request failed. */
    public static JsonObject belanjaUlang(String idTransaksi) {
        String cartCode = LocalStorage.getCartCode();

        JsonObject jadwalDeliveryTerpilih = LocalStorage.savedJadwalDeliveryMap();
        JsonObject savedADJD = LocalStorage.getSavedLatitudeAndLongitude();

        String dayDelivery = (jadwalDeliveryTerpilih != null)
                ? Json.str(jadwalDeliveryTerpilih, "day")
                : "Hari Pengantaran";
        String jadwalDeliveryCode = Json.str(savedADJD, "jadwalDeliveryCode");

        Map<String, String> body = new LinkedHashMap<>();
        body.put("dayDelivery", dayDelivery);
        body.put("jadwalDeliveryCode", jadwalDeliveryCode);
        body.put("cartCode", cartCode);

        // Dart quirk kept: '/api-v2/' + '/Transaksi/belanjaUlang/$id' (double slash).
        Http.Response httpResponse = Http.post(
                ServerPath.baseURL + "/api-v2/" + "/Transaksi/belanjaUlang/" + idTransaksi, body);

        if (httpResponse.statusCode == 200) {
            return httpResponse.jsonObject();
        }

        return null;
    }

    /** Dart: responseBelanjaUlang(context, response). `done` runs when the dialog is closed. */
    public static void responseBelanjaUlang(final BaseActivity activity, JsonObject response,
                                            Async.Done<Void> done) {
        String title = "Belanja Ulang";
        final boolean statusBelanjaUlang = Json.bool(response, "statusBelanjaUlang");
        String messageBelanjaUlang = Json.strOrNull(response, "message");

        String subTitle;
        String btnText;
        if (statusBelanjaUlang) {
            subTitle = "Berhasil belanja ulang!";
            btnText = "OK";
        } else {
            subTitle = "Gagal belanja ulang!";
            btnText = "Coba Lagi!";
        }

        if (messageBelanjaUlang != null) {
            subTitle = subTitle + " " + messageBelanjaUlang;
        }

        final String btn = btnText;
        Modal.Content<Void> actions = handle -> CustomAlert.actionsRow(activity,
                CustomButton.button(activity, btn, () -> {
                    if (statusBelanjaUlang) {
                        Async.run(() -> HeaderModel.getJumlahCart(), (Integer jumlahKeranjang) -> {
                            AppState.get().setJumlahCart(jumlahKeranjang == null ? 0 : jumlahKeranjang);

                            // Dart: Navigator.pushReplacement(context, Keranjang) replaces the topmost route,
                            // which is this dialog: the dialog goes away and Keranjang opens over the screen.
                            handle.close();
                            Nav.push(activity, KeranjangActivity.class);
                        });
                    } else {
                        handle.close();
                    }
                }, new CustomButton.Opts().color(CustomColors.green)));
        CustomAlert.<Void>textComponent(activity, null, title, subTitle, actions, null, false, done);
    }
}
