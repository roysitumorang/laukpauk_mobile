package id.laukpauk.ui.startup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;


import java.util.Locale;
import java.util.Map;

import id.laukpauk.MainActivity;
import id.laukpauk.R;
import id.laukpauk.util.Json;

/**
 * Legacy ui/Routing (named routes of push notifications and deep links) on top of the navigation graph.
 *
 * {@link #handle} understands deep links (https://lp.me/&lt;route&gt;?params, lp2021.page.link) and the intent
 * {@link AppStartup} builds when a OneSignal notification is tapped.
 */
public final class MainRouter {
    private MainRouter() {}

    /** Notification tap extras (see {@link AppStartup}). */
    public static final String EXTRA_NOTIFICATION = "id.laukpauk.extra.NOTIFICATION";
    public static final String EXTRA_ROUTE_NAME = "id.laukpauk.extra.ROUTE_NAME";
    public static final String EXTRA_ROUTE_PARAMS = "id.laukpauk.extra.ROUTE_PARAMS";
    public static final String EXTRA_ID_NOTIFIKASI = "id.laukpauk.extra.ID_NOTIFIKASI";

    /** Deep links (VIEW intents) and notification taps that opened MainActivity. Each intent is handled once. */
    public static void handle(MainActivity activity, NavController nav, Intent intent) {
        if (activity == null || nav == null || intent == null) {
            return;
        }
        String notification = intent.getStringExtra(EXTRA_NOTIFICATION);
        if (notification != null) {
            String type = notification.toLowerCase(Locale.ROOT);
            String routeName = intent.getStringExtra(EXTRA_ROUTE_NAME);
            String routeParams = intent.getStringExtra(EXTRA_ROUTE_PARAMS);
            String idNotifikasi = intent.getStringExtra(EXTRA_ID_NOTIFIKASI);
            intent.removeExtra(EXTRA_NOTIFICATION);
            if (type.equals("khususuntukkamu")) {
                leaveSplash(nav);
                route(nav, routeName, routeParams);
            } else if (type.equals("update") && idNotifikasi != null) {
                leaveSplash(nav);
                Bundle args = new Bundle();
                args.putString("idNotifikasi", idNotifikasi);
                nav.navigate(R.id.detailNotifikasiGlobalFragment, args);
            }
            return;
        }

        Uri data = intent.getData();
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && data != null) {
            String path = data.getPath();
            intent.setData(null);
            if (path == null || path.isEmpty() || path.equals("/")) {
                return;
            }
            Map<String, Object> args = Json.newObject();
            for (String name : data.getQueryParameterNames()) {
                String value = data.getQueryParameter(name);
                if (value != null) {
                    args.put(name, value);
                }
            }
            leaveSplash(nav);
            route(nav, path, args);
        }
    }

    /** Navigates to a named Dart route ("/orderan", "/produk", ...) with JSON-text, Map or null arguments. */
    public static void route(NavController nav, String routeName, Object arguments) {
        if (nav == null) {
            return;
        }
        Map<String, Object> a = toObject(arguments);
        boolean hasArguments = arguments != null;
        String name = routeName == null ? "" : routeName;
        Bundle b = new Bundle();

        switch (name) {
            case "/dompet":
                putIfPresent(b, "jenisDompet", Json.strOrNull(a, "jenisDompet"));
                nav.navigate(R.id.historiDompetFragment, b);
                break;

            case "/orderan":
                b.putString("idTransaksi", Json.str(a, "idTransaksi"));
                nav.navigate(R.id.detailTransaksiFragment, b);
                break;

            case "/voucher":
                if (hasArguments && !isEmptyArgument(arguments)) {
                    b.putString("idVoucher", Json.str(a, "voucher"));
                    nav.navigate(R.id.detailVoucherFragment, b);
                } else {
                    nav.navigate(R.id.voucherFragment);
                }
                break;

            case "/produk":
                // legacy: no arguments -> an empty page
                if (hasArguments) {
                    b.putString("idProduk", Json.str(a, "produk"));
                    nav.navigate(R.id.detailProdukFragment, b);
                }
                break;

            case "/paket":
                if (hasArguments) {
                    b.putString("idPaket", Json.str(a, "paket"));
                    nav.navigate(R.id.detailPaketFragment, b);
                }
                break;

            case "/rpd":
                b.putString("dataPenjual", Json.toJson(Json.object(a, "dataPenjual")));
                b.putString("dataRPD", Json.toJson(Json.object(a, "dataRPD")));
                nav.navigate(R.id.responseRPDFragment, b);
                break;

            case "/rmp":
                nav.navigate(R.id.tokoSayaFragment);
                break;

            case "/poin":
                nav.navigate(R.id.historiPoinFragment);
                break;

            case "/promo": {
                // idPromoItem from a push notification, else 'promo' from a banner
                String idPromoItem = Json.strOrNull(a, "idPromoItem");
                if (idPromoItem == null) {
                    idPromoItem = Json.strOrNull(a, "promo");
                }
                b.putString("idPromoItem", idPromoItem == null ? "" : idPromoItem);
                nav.navigate(R.id.detailPromoItemFragment, b);
                break;
            }

            case "/resep":
                b.putString("idResep", Json.str(a, "resep"));
                nav.navigate(R.id.detailResepFragment, b);
                break;

            case "/penjualan":
                b.putString("idTransaksi", Json.str(a, "idTransaksi"));
                nav.navigate(R.id.prosesPesananFragment, b);
                break;

            case "/buatOrderBerhasil": {
                Map<String, Object> responseCheckout = Json.of("idTransaksi", Json.str(a, "idTransaksi"));
                Map<String, Object> dataFromCheckout = Json.of("responseCheckout", responseCheckout);
                b.putString("returnedDataFromCheckout", Json.toJson(dataFromCheckout));
                b.putBoolean("removeTransactionSession", true);
                nav.navigate(R.id.buatOrderanBerhasilFragment, b);
                break;
            }

            default:
                // "/" and unknown names opened the splash screen: i.e. the app's start
                goHome(nav);
                break;
        }
    }

    /** Back to the home screen (kept in the stack), or the start destination's replacement when it is not there. */
    public static void goHome(NavController nav) {
        NavDestination current = nav.getCurrentDestination();
        if (current != null && current.getId() == R.id.splashScreenFragment) {
            return; // the splash decides by itself
        }
        if (!nav.popBackStack(R.id.homePageFragment, false)) {
            nav.navigate(R.id.homePageFragment, null,
                    new NavOptions.Builder().setPopUpTo(R.id.splashScreenFragment, true).build());
        }
    }

    /** Opened from outside while the splash is showing: put the home screen under the target instead. */
    private static void leaveSplash(NavController nav) {
        NavDestination current = nav.getCurrentDestination();
        if (current != null && current.getId() == R.id.splashScreenFragment) {
            nav.navigate(R.id.homePageFragment, null,
                    new NavOptions.Builder().setPopUpTo(R.id.splashScreenFragment, true).build());
        }
    }

    private static void putIfPresent(Bundle b, String key, @Nullable String value) {
        if (value != null) {
            b.putString(key, value);
        }
    }

    private static boolean isEmptyArgument(Object arguments) {
        return arguments instanceof String && ((String) arguments).length() < 1;
    }

    /** Normalizes the supported argument kinds to a Map<String, Object> (never null). */
    private static Map<String, Object> toObject(Object arguments) {
        if (arguments instanceof String) {
            String s = ((String) arguments).trim();
            return s.isEmpty() ? Json.newObject() : Json.parseObject(s);
        }
        if (arguments instanceof Map) {
            // JSON trees and plain Java maps alike: keys as text, values kept (Json accessors coerce them)
            Map<String, Object> o = Json.newObject();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) arguments).entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    o.put(String.valueOf(e.getKey()), e.getValue());
                }
            }
            return o;
        }
        return Json.newObject();
    }
}
