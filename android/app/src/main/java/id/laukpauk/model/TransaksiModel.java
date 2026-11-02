package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.Async;
import id.laukpauk.core.AppState;
import id.laukpauk.util.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.util.Json;
import id.laukpauk.data.local.LocalStorage;
import id.laukpauk.util.ServerPath;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Nav;
import id.laukpauk.view.pembeli.KeranjangActivity;

/** Port of lib/models/TransaksiModel.dart. */
public final class TransaksiModel {
    private final AppState appState;
    private final Async async;
    private final HTTPRequest httpRequest;
    private final Http http;
    private final LocalStorage localStorage;
    private final ADJDModel adjdModel;
    private final CheckoutModel checkoutModel;
    private final HeaderModel headerModel;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public TransaksiModel(AppState appState, Async async, HTTPRequest httpRequest, Http http, LocalStorage localStorage, ADJDModel adjdModel, CheckoutModel checkoutModel, HeaderModel headerModel) {
        this.appState = appState;
        this.async = async;
        this.httpRequest = httpRequest;
        this.http = http;
        this.localStorage = localStorage;
        this.adjdModel = adjdModel;
        this.checkoutModel = checkoutModel;
        this.headerModel = headerModel;
    }

    /** BLOCKING. Returns dataObject['transaksi'] or the JSON boolean false. */
    public JsonElement getDetailTransaksi(String idTransaksi, String select) {
        String entitasTransaksiQS = (select == null) ? "" : "entitasTransaksi=" + select;

        String params = httpRequest.getQueryString(entitasTransaksiQS, true);

        Http.Response httpResponse = http.getApi("/Transaksi/getTransaksi/" + idTransaksi + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.get(dataObject, "transaksi");
        }

        return new JsonPrimitive(Boolean.FALSE);
    }

    public JsonElement getDetailTransaksi(String idTransaksi) {
        return getDetailTransaksi(idTransaksi, null);
    }

    /** Dart: removeTransactionSession(context). UI flow (see CONVENTIONS); `done` runs on the main thread once. */
    public void removeTransactionSession(BaseActivity activity, final Async.Done<Void> done) {
        // Not tied to the screen's lifetime (Async.run, not activity.async): the session must be cleared even
        // when the screen is gone.
        async.run(() -> {
            localStorage.clearCartCode();
            localStorage.clearUsedVoucher();
            adjdModel.deleteAlamatDanJadwalDelivery();
            adjdModel.deleteCoppiedADJD();

            checkoutModel.bersihkanKeranjang();
            return Boolean.TRUE;
        }, ok -> {
            appState.setJumlahCart(0);
            appState.setADJDSet(false);
            if (done != null) {
                done.run(null);
            }
        });
    }

    public void melewatiBatasRangeJadwalDeliveryBelumCheckout(BaseActivity activity,
                                                                    final Async.Done<Void> done) {
        async.run(() -> {
            adjdModel.deleteAlamatDanJadwalDelivery();

            checkoutModel.bersihkanKeranjang();
            return Boolean.TRUE;
        }, ok -> {
            appState.setJumlahCart(0);
            if (done != null) {
                done.run(null);
            }
        });
    }

    /** BLOCKING. null when the request failed. */
    public Integer jumlahItemDiantar(String idTransaksi) {
        String params = httpRequest.getQueryString(null, true);

        Http.Response httpResponse = http.getApi("/Transaksi/getJumlahItemDiantar/" + idTransaksi + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.intVal(dataObject, "jumlahBarangDiantar");
        }

        return null;
    }

    /** BLOCKING. null when the request failed. */
    public Integer getLastTransactionID() {
        String params = httpRequest.getQueryString(null, true);

        Http.Response httpResponse = http.getApi("/Transaksi/getLastTransactionID?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.intVal(dataObject, "lastTransactionID");
        }

        return null;
    }

    /** BLOCKING. null when the request failed. */
    public JsonObject belanjaUlang(String idTransaksi) {
        String cartCode = localStorage.getCartCode();

        JsonObject jadwalDeliveryTerpilih = localStorage.savedJadwalDeliveryMap();
        JsonObject savedADJD = localStorage.getSavedLatitudeAndLongitude();

        String dayDelivery = (jadwalDeliveryTerpilih != null)
                ? Json.str(jadwalDeliveryTerpilih, "day")
                : "Hari Pengantaran";
        String jadwalDeliveryCode = Json.str(savedADJD, "jadwalDeliveryCode");

        Map<String, String> body = new LinkedHashMap<>();
        body.put("dayDelivery", dayDelivery);
        body.put("jadwalDeliveryCode", jadwalDeliveryCode);
        body.put("cartCode", cartCode);

        // Dart quirk kept: '/api-v2/' + '/Transaksi/belanjaUlang/$id' (double slash).
        Http.Response httpResponse = http.post(
                ServerPath.baseURL + "/api-v2/" + "/Transaksi/belanjaUlang/" + idTransaksi, body);

        if (httpResponse.statusCode == 200) {
            return httpResponse.jsonObject();
        }

        return null;
    }

    /** Dart: responseBelanjaUlang(context, response). `done` runs when the dialog is closed. */
    public void responseBelanjaUlang(final BaseActivity activity, JsonObject response,
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
                        async.run(() -> headerModel.getJumlahCart(), (Integer jumlahKeranjang) -> {
                            appState.setJumlahCart(jumlahKeranjang == null ? 0 : jumlahKeranjang);

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
