package id.laukpauk.ui.startup;

import android.content.Intent;

import androidx.navigation.NavController;

import id.laukpauk.MainActivity;

/** PLACEHOLDER (contract only) - implemented by the startup slice. */
public final class MainRouter {
    private MainRouter() {}

    /** Deep links (VIEW intents) and notification taps that opened MainActivity. */
    public static void handle(MainActivity activity, NavController nav, Intent intent) {
    }

    /** Navigates to a named Dart route ("/orderan", "/produk", ...) with JSON-text, Map or null arguments. */
    public static void route(NavController nav, String routeName, Object arguments) {
    }
}
