package id.laukpauk.model;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.AppState;
import id.laukpauk.core.Async;
import id.laukpauk.core.AuthModel;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.detailproduk.ShowSatuanProduk;

/** Port of lib/models/ProdukModel.dart. */
public final class ProdukModel {
    private ProdukModel() {}

    /**
     * Dart: adjdNotSetAlert(context, {namaProduk = ''}) -> Future&lt;bool?&gt;.
     * `done` receives true ("Pilih Jadwal"), false ("Tutup") or null.
     */
    public static void adjdNotSetAlert(final BaseActivity activity, String namaProduk,
                                       Async.Done<Boolean> done) {
        String title = "Pilih Jadwal Dulu";
        String subTitle = "Untuk bisa order sayur, ikan, daging segar, tentukan lokasi pengantaran dulu ya...";
        Modal.Content<Boolean> actions = handle -> CustomAlert.actionsRow(activity,
                CustomButton.button(activity, "Tutup", () -> handle.close(Boolean.FALSE),
                        new CustomButton.Opts().color(CustomColors.textMuted).outline()),
                Ui.spaceW(activity, 5),
                CustomButton.button(activity, "Pilih Jadwal", () -> handle.close(Boolean.TRUE),
                        new CustomButton.Opts().color(CustomColors.green)));
        CustomAlert.<Boolean>textComponent(activity, null, title, subTitle, actions, null, false, done);
    }

    public static void adjdNotSetAlert(BaseActivity activity, Async.Done<Boolean> done) {
        adjdNotSetAlert(activity, "", done);
    }

    /** What the background part of showSatuanProduk collects before the sheet is shown. */
    private static final class SatuanData {
        Boolean isThisProductInCAVariable;
        Boolean isPenjualAvailable;
        String ongkirPerKMString = "";
    }

    /**
     * Dart: showSatuanProduk(context, dataProduk, {dataPromo}) -> Future&lt;void&gt;. Shows the
     * bottom sheet with ShowSatuanProduk; like the Dart code `done` runs as soon as the sheet is
     * shown (the sheet itself is not awaited). `dataPromo` is accepted but unused (as in Dart).
     */
    public static void showSatuanProduk(final BaseActivity activity, final JsonObject dataProduk,
                                        JsonObject dataPromo, final Async.Done<Void> done) {
        final String idProduk = Json.str(dataProduk, "id");

        activity.async(() -> {
            SatuanData data = new SatuanData();

            JsonObject adjdSet = LocalStorage.getSavedLatitudeAndLongitude();
            String latitude = Json.strOrNull(adjdSet, "latitude");
            String longitude = Json.strOrNull(adjdSet, "longitude");
            String jadwalDeliveryCode = Json.strOrNull(adjdSet, "jadwalDeliveryCode");

            if (latitude != null && longitude != null && jadwalDeliveryCode != null) {
                data.isThisProductInCAVariable = isThisProductInCA(idProduk, latitude, longitude);
                data.isPenjualAvailable = isProductSellerAvailable(idProduk, jadwalDeliveryCode);
                JsonElement getCostPerKM = SettingModel.getSettingValue("costPerKm");
                if (getCostPerKM != null && !Json.isFalse(getCostPerKM) && !Json.isNull(getCostPerKM)) {
                    int costPerKM = Json.parseInt(Json.asStr(getCostPerKM), Integer.MIN_VALUE);
                    if (costPerKM != Integer.MIN_VALUE) {
                        data.ongkirPerKMString = Currency.formatRupiah(costPerKM, 0);
                    }
                }
            }
            return data;
        }, data -> {
            if (data == null) {
                if (done != null) {
                    done.run(null);
                }
                return;
            }
            // ShowSatuanProduk unwraps both flags with `!` in Dart; null (failed request) is treated as false.
            final boolean isPenjualAvailable = Boolean.TRUE.equals(data.isPenjualAvailable);
            final boolean isThisProductInCAVariable = Boolean.TRUE.equals(data.isThisProductInCAVariable);
            final String ongkirPerKMString = data.ongkirPerKMString;

            Modal.<Object>bottomSheet(activity, true, handle -> {
                View sheet = new ShowSatuanProduk(activity, idProduk, isPenjualAvailable,
                        isThisProductInCAVariable, dataProduk, ongkirPerKMString,
                        (JsonObject addToCartResponse) -> {
                            handle.close(); // Navigator.pop(context)
                            onAddToCartResponse(activity, addToCartResponse);
                        });

                return ShopModelUi.roundedSheet(activity, sheet);
            }, null);

            if (done != null) {
                done.run(null);
            }
        });
    }

    public static void showSatuanProduk(BaseActivity activity, JsonObject dataProduk,
                                        Async.Done<Void> done) {
        showSatuanProduk(activity, dataProduk, null, done);
    }

    /** The addCartCallback of ShowSatuanProduk (after the sheet was popped). */
    private static void onAddToCartResponse(final BaseActivity activity, JsonObject addToCartResponse) {
        if (addToCartResponse == null) {
            return;
        }
        boolean statusAddCart = Json.bool(addToCartResponse, "statusAddCart");
        if (statusAddCart) {
            ShopModelUi.keranjangSnackbar(activity, "Berhasil ditambahkan ke dalam keranjang !", 2);

            Async.run(() -> HeaderModel.getJumlahCart(), (Integer jumlahKeranjang) -> {
                if (jumlahKeranjang != null) {
                    AppState.get().setJumlahCart(jumlahKeranjang);
                }
            });
        } else {
            View moreWidgets = null;
            if (addToCartResponse.has("message")) {
                String message = Json.str(addToCartResponse, "message");
                TextView text = Ui.text(activity, message,
                        new Txt.Style(Ui.hb(activity) * 3.25f).color(CustomColors.textDanger));
                text.setGravity(Gravity.CENTER);
                moreWidgets = text;
            }

            String title = "Keranjang";
            String subTitle = "Item gagal ditambahkan ke dalam keranjang! ";

            Modal.Content<Object> actions = handle -> CustomAlert.actionsRow(activity,
                    CustomAlert.textButton(activity, "OK", handle::close));
            CustomAlert.<Object>textComponent(activity, null, title, subTitle, actions, moreWidgets,
                    false, null);
        }
    }

    /**
     * Dart: addToCart(idProduk, idSatuan, quantity, {idPromo}) -> Future&lt;Map?&gt;. BLOCKING.
     * Null when the request failed (or no delivery schedule is saved).
     */
    public static JsonObject addToCart(String idProduk, String idSatuan, String quantity,
                                       String idPromo) {
        boolean isLoggedIn = AuthModel.isLogin();

        String cartCode = LocalStorage.getCartCode();

        JsonObject jadwalDeliveryTerpilih = LocalStorage.savedJadwalDeliveryMap();
        JsonObject savedADJD = LocalStorage.getSavedLatitudeAndLongitude();

        String dayDelivery = (jadwalDeliveryTerpilih != null)
                ? Json.str(jadwalDeliveryTerpilih, "day")
                : "Hari Pengantaran";
        String jadwalDeliveryCode = Json.strOrNull(savedADJD, "jadwalDeliveryCode");
        if (jadwalDeliveryCode == null) {
            return null; // Dart: `String jadwalDeliveryCode = savedADJD[...]` throws on null
        }

        Map<String, String> cartData = new LinkedHashMap<>();
        cartData.put("cartCode", cartCode);
        cartData.put("idProduk", idProduk);
        cartData.put("jenisProduk", "nonpaket");
        cartData.put("idSatuan", idSatuan);
        cartData.put("quantity", quantity);
        cartData.put("jadwalDeliveryCode", jadwalDeliveryCode);
        cartData.put("dayDelivery", dayDelivery);

        if (idPromo != null) {
            cartData.put("idPromo", idPromo);
        }
        if (isLoggedIn) {
            String currentUserID = LocalStorage.getCurrentUserID();
            cartData.put("idUser", currentUserID);
        }

        Map<String, String> body = HTTPRequest.postBody(cartData);

        Http.Response httpResponse = Http.postApi("/Keranjang/saveCart", body);

        if (httpResponse.statusCode == 200) {
            return httpResponse.jsonObject();
        }
        return null;
    }

    public static JsonObject addToCart(String idProduk, String idSatuan, String quantity) {
        return addToCart(idProduk, idSatuan, quantity, null);
    }

    /** Dart: isThisProductInCA(idProduk, {latitude, longitude}) -> Future&lt;bool?&gt;. BLOCKING. */
    public static Boolean isThisProductInCA(String idProduk, String latitude, String longitude) {
        String params = HTTPRequest.getQueryString(
                "idProduk=" + idProduk + "&latitude=" + latitude + "&longitude=" + longitude);

        Http.Response httpResponse = Http.getApi("/Produk/isThisProductInCA?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject serverResponse = httpResponse.jsonObject();
            return Json.bool(serverResponse, "isThisProductInCA");
        }
        return null;
    }

    /** Dart: isProductSellerAvailable(idProduk, {jadwalDeliveryCode}) -> Future&lt;bool?&gt;. BLOCKING. */
    public static Boolean isProductSellerAvailable(String idProduk, String jadwalDeliveryCode) {
        String params = HTTPRequest.getQueryString(
                "idProduk=" + idProduk + "&jadwalDeliveryCode=" + jadwalDeliveryCode);

        Http.Response httpResponse = Http.getApi("/Produk/isProductSellerAvailable?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject serverResponse = httpResponse.jsonObject();
            return Json.bool(serverResponse, "isSellerAvailable");
        }

        return null;
    }

    /**
     * Dart: getDetailProduk(idProduk) -> Future&lt;dynamic&gt;. BLOCKING. The `detailProduk`
     * member (an object, or JSON null / false when the product does not exist); the JSON
     * primitive false when the request failed.
     */
    public static JsonElement getDetailProduk(String idProduk) {
        idProduk = idProduk == null ? "" : idProduk;

        String params = HTTPRequest.getQueryString("idProduk=" + idProduk);

        Http.Response httpResponse = Http.getApi("/Produk/detailProduk?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return ShopModelUi.dyn(dataObject.get("detailProduk"));
        }

        return ShopModelUi.FALSE;
    }

    /** Dart: getLowestPrice(idProduk) -> Future&lt;dynamic&gt;. BLOCKING. `lowestPrice`, or false on failure. */
    public static JsonElement getLowestPrice(String idProduk) {
        Http.Response httpResponse = Http.getApi("/Produk/getLowestPriceOfThisProduct/" + idProduk);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return ShopModelUi.dyn(dataObject.get("lowestPrice"));
        }

        return ShopModelUi.FALSE;
    }
}
