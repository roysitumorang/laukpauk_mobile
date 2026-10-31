package id.laukpauk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

import id.laukpauk.core.Json;
import id.laukpauk.view.SplashScreenActivity;
import id.laukpauk.view.penjual.ProsesPesananActivity;
import id.laukpauk.view.penjual.TokoSayaActivity;
import id.laukpauk.view.pembeli.BuatOrderanBerhasilActivity;
import id.laukpauk.view.pembeli.DetailPaketActivity;
import id.laukpauk.view.pembeli.DetailProdukActivity;
import id.laukpauk.view.pembeli.DetailPromoItemActivity;
import id.laukpauk.view.pembeli.DetailResepActivity;
import id.laukpauk.view.pembeli.DetailTransaksiActivity;
import id.laukpauk.view.pembeli.DetailVoucherActivity;
import id.laukpauk.view.pembeli.HistoriDompetActivity;
import id.laukpauk.view.pembeli.HistoriPoinActivity;
import id.laukpauk.view.pembeli.ResponseRPDActivity;
import id.laukpauk.view.pembeli.VoucherActivity;

/**
 * lib/libraries/Routing.dart: named routes used by push notifications and deep links.
 *
 * `arguments` may be a String (JSON text, Dart's `routeSettings.arguments as String`), a
 * Map&lt;String,String&gt; (deep-link query), a JsonObject, or null. Unknown names open the splash screen.
 */
public final class Routing {
    private Routing() {}

    /** Dart: Routing.routeWidget(RouteSettings) as an Intent for the screen. */
    public static Intent intentFor(Context ctx, String routeName, Object arguments) {
        Class<? extends Activity> screen = SplashScreenActivity.class;
        JsonObject args = null;
        JsonObject a = toObject(arguments);
        boolean hasArguments = arguments != null;
        String name = routeName == null ? "" : routeName;

        switch (name) {
            case "/":
                screen = SplashScreenActivity.class;
                break;

            case "/dompet":
                screen = HistoriDompetActivity.class;
                args = Json.of("jenisDompet", Json.strOrNull(a, "jenisDompet"));
                break;

            case "/orderan":
                screen = DetailTransaksiActivity.class;
                args = Json.of("idTransaksi", Json.str(a, "idTransaksi"));
                break;

            case "/voucher": {
                // Widget widget = Voucher(); if (arguments is non-empty) widget = DetailVoucher(arguments['voucher'])
                screen = VoucherActivity.class;
                if (hasArguments && !isEmptyArgument(arguments)) {
                    screen = DetailVoucherActivity.class;
                    args = Json.of("idVoucher", Json.str(a, "voucher"));
                }
                break;
            }

            case "/produk":
                // Dart: arguments == null -> SizedBox() (an empty page)
                if (hasArguments) {
                    screen = DetailProdukActivity.class;
                    args = Json.of("id", Json.str(a, "produk"));
                }
                break;

            case "/paket":
                if (hasArguments) {
                    screen = DetailPaketActivity.class;
                    args = Json.of("idPaket", Json.str(a, "paket"));
                }
                break;

            case "/rpd":
                screen = ResponseRPDActivity.class;
                args = Json.of("dataPenjual", Json.object(a, "dataPenjual"), "dataRPD", Json.object(a, "dataRPD"));
                break;

            case "/rmp":
                screen = TokoSayaActivity.class;
                break;

            case "/poin":
                screen = HistoriPoinActivity.class;
                break;

            case "/promo": {
                // idPromoItem from a push notification, else 'promo' from a banner
                String idPromoItem = Json.strOrNull(a, "idPromoItem");
                if (idPromoItem == null) {
                    idPromoItem = Json.strOrNull(a, "promo");
                }
                screen = DetailPromoItemActivity.class;
                args = Json.of("idPromoItem", idPromoItem);
                break;
            }

            case "/resep":
                screen = DetailResepActivity.class;
                args = Json.of("id", Json.str(a, "resep"));
                break;

            case "/penjualan":
                screen = ProsesPesananActivity.class;
                args = Json.of("idTransaksi", Json.str(a, "idTransaksi"));
                break;

            case "/buatOrderBerhasil": {
                JsonObject responseCheckout = Json.of("idTransaksi", Json.str(a, "idTransaksi"));
                JsonObject dataFromCheckout = Json.of("responseCheckout", responseCheckout);
                screen = BuatOrderanBerhasilActivity.class;
                args = Json.of("returnedDataFromCheckout", dataFromCheckout, "removeTransactionSession", true);
                break;
            }

            default:
                screen = SplashScreenActivity.class;
                break;
        }

        Intent intent = Nav.intent(ctx, screen, args);
        if (!(ctx instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    private static boolean isEmptyArgument(Object arguments) {
        if (arguments instanceof String) {
            return ((String) arguments).length() < 1;
        }
        return false;
    }

    /** Normalizes the supported argument kinds to a JsonObject (never null). */
    private static JsonObject toObject(Object arguments) {
        if (arguments instanceof JsonObject) {
            return (JsonObject) arguments;
        }
        if (arguments instanceof JsonElement) {
            return Json.obj((JsonElement) arguments);
        }
        if (arguments instanceof String) {
            String s = ((String) arguments).trim();
            return s.isEmpty() ? new JsonObject() : Json.parseObject(s);
        }
        if (arguments instanceof Map) {
            JsonObject o = new JsonObject();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) arguments).entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    o.addProperty(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
                }
            }
            return o;
        }
        return new JsonObject();
    }
}
