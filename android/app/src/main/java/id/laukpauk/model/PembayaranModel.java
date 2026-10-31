package id.laukpauk.model;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import id.laukpauk.core.Async;
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
import id.laukpauk.view.SplashScreenActivity;
import id.laukpauk.view.pembeli.DoPaymentActivity;

/** Port of lib/models/PembayaranModel.dart. */
public final class PembayaranModel {
    private PembayaranModel() {}

    /**
     * BLOCKING. Dart: buatOrder({metodePembayaranTerpilihMap, checkoutData, moreData}).
     * Returns the decoded response, or {statusCheckout: false, message: 'Nande'} on any failure.
     */
    public static JsonObject buatOrder(JsonObject metodePembayaranTerpilihMap, JsonObject checkoutData,
                                       String moreData) {
        try {
            String channelCode = Json.str(metodePembayaranTerpilihMap, "channel_code");
            String namaMetodePembayaran = Json.str(metodePembayaranTerpilihMap, "name");

            JsonObject delivery = LocalStorage.getSavedLatitudeAndLongitude();
            String alamatDelivery = Json.str(delivery, "alamat");

            // {...checkoutData, 'metodePembayaran': ..., ...}
            JsonObject httpBody = new JsonObject();
            if (checkoutData != null) {
                for (Map.Entry<String, JsonElement> e : checkoutData.entrySet()) {
                    httpBody.add(e.getKey(), e.getValue());
                }
            }
            httpBody.addProperty("metodePembayaran", channelCode);
            httpBody.addProperty("namaMetodePembayaran", namaMetodePembayaran);
            httpBody.addProperty("moreDataMetodePembayaran", moreData);
            httpBody.addProperty("alamatDelivery", alamatDelivery);

            String params = HTTPRequest.getQueryString(null, true);
            Map<String, String> body = HTTPRequest.postBody(ShopModelUi.form(httpBody));

            Http.Response httpResponse = Http.post(
                    ServerPath.baseURL + "/api-v2/Transaksi/checkout?" + params, body);

            if (httpResponse.statusCode == 200) {
                return httpResponse.jsonObject();
            }
        } catch (RuntimeException e) {
            // Dart: catch (e) {}
        }

        JsonObject failed = new JsonObject();
        failed.addProperty("statusCheckout", false);
        failed.addProperty("message", "Nande");
        return failed;
    }

    private static void urlTidakValid(final BaseActivity activity) {
        String title = "Laukpauk";
        String subTitle = "URL Tidak Valid! Silahkan coba lagi!";
        Modal.Content<Object> actions = handle -> CustomAlert.actionsRow(activity,
                CustomAlert.textButton(activity, "Coba Lagi!", () -> handle.close()));

        CustomAlert.<Object>textComponent(activity, null, title, subTitle, actions, null, false, null);
    }

    /** url_launcher canLaunchUrl: true when an installed app handles the URL. */
    private static boolean canLaunch(BaseActivity activity, Intent intent) {
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            return true;
        }
        // Package visibility (API 30+) hides handlers that are not declared in <queries>: custom schemes
        // (e.g. a ShopeePay deep link) cannot be probed, so let the launch itself decide.
        String scheme = intent.getData() != null ? intent.getData().getScheme() : null;
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && scheme != null
                && !scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https");
    }

    private static void launch(BaseActivity activity, Intent intent) {
        try {
            activity.startActivity(intent);
        } catch (RuntimeException e) {
            // ActivityNotFoundException / SecurityException: nothing handles it (Dart ignored launchUrl failures)
        }
    }

    private static Intent viewIntent(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        return intent;
    }

    /**
     * Dart: buatOrderNextStep({context, channelCodeTerpilihLC, returnedData, checkoutData,
     * metodePembayaranTerpilihMap, moreData, checkoutCallback}). `done` runs once, right after the branch is dispatched.
     */
    public static void buatOrderNextStep(BaseActivity activity, String channelCodeTerpilihLC,
                                         JsonObject returnedData, JsonObject checkoutData,
                                         JsonObject metodePembayaranTerpilihMap, String moreData,
                                         Async.Done<JsonObject> checkoutCallback, Async.Done<Void> done) {
        JsonObject dataCheckout = new JsonObject();
        if (checkoutData != null) {
            for (Map.Entry<String, JsonElement> e : checkoutData.entrySet()) {
                dataCheckout.add(e.getKey(), e.getValue());
            }
        }
        dataCheckout.add("metodePembayaran", Json.get(metodePembayaranTerpilihMap, "channel_code"));
        dataCheckout.add("namaMetodePembayaran", Json.get(metodePembayaranTerpilihMap, "name"));
        dataCheckout.addProperty("moreDataMetodePembayaran", moreData);

        boolean statusCheckout = Json.bool(returnedData, "statusCheckout");

        JsonElement paymentResponse = Json.get(returnedData, "paymentResponse");

        if (channelCodeTerpilihLC.equals("dana") || channelCodeTerpilihLC.equals("linkaja")) {
            if (statusCheckout) {
                // payment response untuk diluar dana dan link aja adalah idtransaksi
                String checkoutUrl = Json.strOrNull(Json.obj(paymentResponse), "checkout_url");
                Intent checkoutIntent = checkoutUrl != null ? viewIntent(checkoutUrl) : null;
                if (checkoutIntent != null && canLaunch(activity, checkoutIntent)) {
                    Nav.pushAndRemoveUntil(activity, SplashScreenActivity.class,
                            Json.of("removeTransactionSession", true));
                    launch(activity, checkoutIntent);
                } else {
                    urlTidakValid(activity);
                }
            } else {
                checkoutFailedAlert(activity, channelCodeTerpilihLC, paymentResponse);
            }
        } else {
            if (channelCodeTerpilihLC.equals("shopeepay")) {
                if (statusCheckout) {
                    JsonObject actionsObj = Json.object(Json.obj(paymentResponse), "actions");
                    String urlString = Json.strOrNull(actionsObj, "mobile_deeplink_checkout_url");

                    Intent actionsIntent = urlString != null ? viewIntent(urlString) : null;
                    if (actionsIntent != null && canLaunch(activity, actionsIntent)) {
                        Nav.pushAndRemoveUntil(activity, SplashScreenActivity.class,
                                Json.of("removeTransactionSession", true));

                        launch(activity, actionsIntent);
                    } else {
                        urlTidakValid(activity);
                    }
                } else {
                    checkoutFailedAlert(activity, channelCodeTerpilihLC, paymentResponse);
                }
            } else {
                if (statusCheckout) {
                    JsonObject returnedDataCheckoutCallback = new JsonObject();
                    returnedDataCheckoutCallback.add("responseCheckout", returnedData);
                    returnedDataCheckoutCallback.add("dataCheckout", dataCheckout);
                    if (channelCodeTerpilihLC.equals("ovo")) {
                        Nav.pushAndRemoveUntil(activity, DoPaymentActivity.class, null);
                    } else {
                        // cod dan tf_bca dan kartu_kredit
                        if (checkoutCallback != null) {
                            checkoutCallback.run(returnedDataCheckoutCallback);
                        }
                    }
                } else {
                    checkoutFailedAlert(activity, channelCodeTerpilihLC, paymentResponse);
                }
            }
        }

        if (done != null) {
            done.run(null);
        }
    }

    private static void checkoutFailedAlert(final BaseActivity activity, String channelCodeLC,
                                            JsonElement paymentResponse) {
        String title = "Checkout";
        String content = "Checkout gagal ! Coba lagi ya bun...";

        List<String> allowedShowMessagePaymentChannel = Arrays.asList("shopeepay", "dana", "linkaja", "credit_card");

        if (allowedShowMessagePaymentChannel.contains(channelCodeLC)) {
            content = Json.str(Json.obj(paymentResponse), "message");
        }

        Modal.Content<Object> actions = handle -> CustomAlert.actionsRow(activity,
                CustomButton.button(activity, "Coba Lagi", () -> handle.close(),
                        new CustomButton.Opts().color(CustomColors.green)));

        CustomAlert.<Object>textComponent(activity, null, title, content, actions, null, false, null);
    }
}
