package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Currency;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.core.Prefs;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.BuatOrderanBerhasilActivity;
import id.laukpauk.view.pembeli.TransferBankActivity;
import id.laukpauk.view.pembeli.components.checkout.PilihanMetodePembayaran;
import id.laukpauk.view.pembeli.components.checkout.VoucherItemCheckout;

/** Port of lib/models/CheckoutModel.dart. */
public final class CheckoutModel {
    private final Async async;
    private final HTTPRequest httpRequest;
    private final Http http;
    private final LocalStorage localStorage;
    private final Prefs prefs;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public CheckoutModel(Async async, HTTPRequest httpRequest, Http http, LocalStorage localStorage, Prefs prefs) {
        this.async = async;
        this.httpRequest = httpRequest;
        this.http = http;
        this.localStorage = localStorage;
        this.prefs = prefs;
    }

    /** BLOCKING. */
    public JsonArray getVoucher(int totalBelanja) {
        String totalBelanjaString = String.valueOf(totalBelanja);
        String entitasVoucher = "id, nama, foto, minimumBelanja, nilai, jenis, kode, maximumUsage";

        String params = httpRequest.getQueryString(
                "entitasVoucher=" + entitasVoucher + "&forThisTotalBelanja=" + totalBelanjaString, true);

        Http.Response httpResponse = http.getApi("/User/getVoucher?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "listVoucher");
        }

        return new JsonArray();
    }

    /**
     * Dart: showVoucher(context, totalBelanja, voucherTerpilih, onVoucherSelected) - a modal bottom sheet.
     * `onVoucherSelected` gets the chosen voucher (the sheet closes right after). `done` runs when the sheet is gone.
     */
    public void showVoucher(final BaseActivity activity, final int totalBelanja,
                                   final JsonObject voucherTerpilih, final Async.Done<JsonObject> onVoucherSelected,
                                   Async.Done<Void> done) {
        Modal.<Void>bottomSheet(activity, true, handle -> {
            final float hb = Ui.hb(activity);

            LinearLayout column = Ui.column(activity);
            Ui.pad(column, 15, 20, 15, 20);

            column.addView(Ui.text(activity, "Voucher Kamu", new Txt.Style(hb * 4).bold()),
                    new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            column.addView(Ui.spaceH(activity, 2.5f));
            column.addView(Ui.text(activity, "Voucher aktif untuk minimal belanja "
                            + Currency.formatRupiah(totalBelanja, 0) + " : ", new Txt.Style(hb * 3.25f)),
                    new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            column.addView(Ui.spaceH(activity, 10));

            // FutureBuilder: spinner until getVoucher() answers.
            final FrameLayout holder = new FrameLayout(activity);
            ProgressBar spinner = new ProgressBar(activity);
            spinner.setIndeterminate(true);
            FrameLayout.LayoutParams spLp = new FrameLayout.LayoutParams(Ui.dp(activity, 25), Ui.dp(activity, 25));
            spLp.gravity = Gravity.CENTER;
            holder.addView(spinner, spLp);
            column.addView(holder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

            async.run(() -> getVoucher(totalBelanja), (JsonArray dataVoucher) -> {
                if (dataVoucher == null) {
                    return; // request threw: Dart's FutureBuilder keeps the spinner
                }
                holder.removeAllViews();
                if (dataVoucher.size() >= 1) {
                    LinearLayout list = Ui.column(activity);
                    Ui.pad(list, 5, 5, 5, 5);
                    for (int i = 0; i < dataVoucher.size(); i++) {
                        JsonObject voucher = Json.objAt(dataVoucher, i);
                        list.addView(new VoucherItemCheckout(activity, voucher, voucherTerpilih,
                                (JsonObject selectedVoucher) -> {
                                    if (onVoucherSelected != null) {
                                        onVoucherSelected.run(selectedVoucher);
                                    }
                                    handle.close();
                                }), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                    }
                    holder.addView(list, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                } else {
                    LinearLayout empty = Ui.column(activity);
                    empty.setGravity(Gravity.CENTER);
                    empty.addView(Ui.asset(activity, "empty.png", hb * 30),
                            new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
                    empty.addView(Ui.spaceH(activity, 10));
                    android.widget.TextView msg = Ui.text(activity, "Tidak ada voucher yang bisa anda gunakan.",
                            new Txt.Style(hb * 3.5f).color(CustomColors.textMuted));
                    msg.setGravity(Gravity.CENTER);
                    empty.addView(msg, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
                    empty.addView(Ui.spaceH(activity, 10));
                    holder.addView(empty, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                }
            });

            return ShopModelUi.roundedSheet(activity, Ui.scroll(activity, column));
        }, done);
    }

    /**
     * Dart: showMetodePembayaran(context, checkoutData, totalTagihan) - a modal bottom sheet (isScrollControlled).
     * When the checkout succeeds the app goes to TransferBank (tf_bca) or BuatOrderanBerhasil and clears the stack.
     */
    public void showMetodePembayaran(final BaseActivity activity, final JsonObject checkoutData,
                                            final int totalTagihan, Async.Done<Void> done) {
        final int totalTagihanLocal = totalTagihan;

        // Dart isDismissible: false (but back / drag still closed the sheet): dismissible here so the user is never trapped.
        Modal.<Void>bottomSheet(activity, true, handle -> {
            PilihanMetodePembayaran sheet = new PilihanMetodePembayaran(activity, activity, checkoutData,
                    totalTagihanLocal, (JsonObject returnedData) -> {
                JsonObject responseCheckout = Json.object(returnedData, "responseCheckout");

                boolean statusCheckout = Json.bool(responseCheckout, "statusCheckout");
                if (statusCheckout) {
                    String metodePembayaran = Json.str(responseCheckout, "metodePembayaran");
                    String metodePembayaranLC = metodePembayaran.toLowerCase();

                    handle.close();
                    if (metodePembayaranLC.equals("tf_bca")) {
                        Nav.pushAndRemoveUntil(activity, TransferBankActivity.class,
                                Json.of("returnedDataFromCheckout", returnedData));
                    } else {
                        Nav.pushAndRemoveUntil(activity, BuatOrderanBerhasilActivity.class,
                                Json.of("removeTransactionSession", true,
                                        "returnedDataFromCheckout", returnedData));
                    }
                }
            });
            return ShopModelUi.roundedSheet(activity, sheet);
        }, done);
    }

    /** BLOCKING. */
    public boolean bersihkanKeranjang() {
        String cartCode = localStorage.getCartCode();
        String params = httpRequest.getQueryString("cartCode=" + cartCode);

        Http.Response httpResponse = http.getApi("/Keranjang/bersihkanKeranjang?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.bool(dataObject, "statusBersihkanKeranjang");
        }

        return false;
    }

    /** Dart: konfirmasi(context) -> bool?. `done` gets true / false / null (dismissed). */
    public void konfirmasi(final BaseActivity activity, Async.Done<Boolean> done) {
        String title = "Konfirmasi Keranjang";
        String subTitle = "Benarkah anda akan menghapus seluruh item belanjaan anda di keranjang?";
        Modal.Content<Boolean> actions = handle -> CustomAlert.actionsRow(activity,
                CustomAlert.textButton(activity, "Tidak", () -> handle.close(Boolean.FALSE)),
                CustomAlert.textButton(activity, "Ya, lanjutkan !", () -> handle.close(Boolean.TRUE)));
        CustomAlert.<Boolean>textComponent(activity, null, title, subTitle, actions, null, false, done);
    }

    /** BLOCKING. null when the request failed. Returns the whole decoded JSON. */
    public JsonElement getJenisOngkir() {
        String cartCode = localStorage.getCartCode();
        JsonObject savedADJD = localStorage.getSavedLatitudeAndLongitude();

        String latDelivery = Json.str(savedADJD, "latitude");
        String longDelivery = Json.str(savedADJD, "longitude");

        String params = httpRequest.getQueryString(
                "cartCode=" + cartCode + "&latDelivery=" + latDelivery + "&longDelivery=" + longDelivery);

        Http.Response httpResponse = http.getApi("/Transaksi/getJenisOngkir?" + params);

        if (httpResponse.statusCode == 200) {
            return httpResponse.json();
        }
        return null;
    }

    /** BLOCKING. dataObject['walletConfiguration'] or the JSON boolean false. */
    public JsonElement getWalletConfiguration() {
        Http.Response httpResponse = http.getApi("/Pengaturan/getWalletConfiguration");

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.get(dataObject, "walletConfiguration");
        }

        return new JsonPrimitive(Boolean.FALSE);
    }

    /** BLOCKING. */
    public int getTotalKeranjang() {
        JsonObject savedADJD = localStorage.getSavedLatitudeAndLongitude();

        String cartCode = localStorage.getCartCode();
        String latDelivery = Json.str(savedADJD, "latitude");
        String longDelivery = Json.str(savedADJD, "longitude");

        String params = httpRequest.getQueryString(
                "cartCode=" + cartCode + "&latDelivery=" + latDelivery + "&longDelivery=" + longDelivery);

        Http.Response httpResponse = http.getApi("/User/getTotalKeranjang?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.intVal(dataObject, "totalKeranjang");
        }

        return 0;
    }

    /** BLOCKING. */
    public int getTotalOngkir() {
        String cartCode = localStorage.getCartCode();
        JsonObject savedADJD = localStorage.getSavedLatitudeAndLongitude();

        String latDelivery = Json.str(savedADJD, "latitude");
        String longDelivery = Json.str(savedADJD, "longitude");

        String params = httpRequest.getQueryString(
                "cartCode=" + cartCode + "&latDelivery=" + latDelivery + "&longDelivery=" + longDelivery);

        Http.Response httpResponse = http.getApi("/User/getTotalOngkir?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.intVal(dataObject, "totalOngkir");
        }

        return 0;
    }

    /** BLOCKING. */
    public int getJumlahCart() {
        String cartCode = localStorage.getCartCode();
        String params = httpRequest.getQueryString("cartCode=" + cartCode);

        Http.Response httpResponse = http.getApi("/User/getJumlahCart?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.intVal(dataObject, "jumlahKeranjang");
        }
        return 0;
    }

    /** BLOCKING. dataObject['diskonKategori'] or null when the request failed. */
    public JsonElement getDiskonKategori() {
        String cartCode = localStorage.getCartCode();
        String params = httpRequest.getQueryString("cartCode=" + cartCode);

        Http.Response httpResponse = http.getApi("/User/getDiskonKategori?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.get(dataObject, "diskonKategori");
        }
        return null;
    }

    /** BLOCKING. true when no schedule is saved or the request failed. */
    public boolean isJadwalDeliveryOpened() {
        String encodedJadwalDelivery = prefs.getString("savedJadwalDeliveryMap");
        if (encodedJadwalDelivery != null) {
            JsonObject decodedJadwalDelivery = Json.parseObject(encodedJadwalDelivery);

            String kode = Json.str(decodedJadwalDelivery, "kode");
            String day = Json.str(decodedJadwalDelivery, "day");
            String setOn = Json.str(decodedJadwalDelivery, "setOn");

            java.util.Map<String, String> body = new java.util.LinkedHashMap<>();
            body.put("kode", kode);
            body.put("day", day);
            body.put("setOn", setOn);
            Http.Response httpResponse = http.postApi("/JadwalDelivery/isJadwalDeliveryOpened", body);
            if (httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                return Json.bool(dataObject, "isJadwalDeliveryOpened");
            }
        }

        return true;
    }
}
