package id.laukpauk.view.pembeli;

import android.app.Activity;

import com.google.gson.JsonObject;

import id.laukpauk.core.Json;
import id.laukpauk.ui.Nav;

/**
 * Replacement for the Dart `Widget? routeTo` constructor parameter of Login / Register / OTPRegistrasi /
 * OTPVerifikasiUser / OTPNotCompleted / KirimUlangOTP / SplashScreen.
 *
 * A Widget cannot travel through an Intent, so the target screen is described by two argument keys:
 *   routeToClass : fully-qualified Activity class name (e.g. CheckoutActivity.class.getName())
 *   routeToArgs  : JsonObject with the target's own constructor arguments (optional)
 * "routeTo == null" in Dart is "routeToClass missing or empty" here.
 */
public final class AuthRoute {
    private AuthRoute() {}

    public static final String KEY_CLASS = "routeToClass";
    public static final String KEY_ARGS = "routeToArgs";

    /** The Activity class named by `className`, or null when it is empty / unknown / not an Activity. */
    public static Class<? extends Activity> resolve(String className) {
        if (className == null || className.trim().isEmpty()) {
            return null;
        }
        try {
            return Class.forName(className.trim()).asSubclass(Activity.class);
        } catch (ClassNotFoundException | ClassCastException e) {
            return null;
        }
    }

    /** The routeToClass argument, or null when there is none. */
    public static String routeClass(JsonObject args) {
        String c = Json.strOrNull(args, KEY_CLASS);
        if (c == null || c.trim().isEmpty()) {
            return null;
        }
        return c.trim();
    }

    /** The routeToArgs argument, or null when there is none. */
    public static JsonObject routeArgs(JsonObject args) {
        return Json.objectOrNull(args, KEY_ARGS);
    }

    /** Arguments that hand the same routeTo on to the next auth screen (`OTPRegistrasi(routeTo: widget.routeTo)`). */
    public static JsonObject carry(String routeToClass, JsonObject routeToArgs) {
        JsonObject o = new JsonObject();
        if (routeToClass != null && !routeToClass.trim().isEmpty()) {
            o.addProperty(KEY_CLASS, routeToClass);
            if (routeToArgs != null) {
                o.add(KEY_ARGS, routeToArgs);
            }
        }
        return o;
    }

    public static JsonObject carry(JsonObject args) {
        return carry(routeClass(args), routeArgs(args));
    }

    /**
     * Navigator.pushAndRemoveUntil(context, MaterialPageRoute(builder: (_) => routeTo ?? Fallback()), (route) => false).
     */
    public static void pushAndRemoveUntil(Activity from, String routeToClass, JsonObject routeToArgs,
                                          Class<? extends Activity> fallback) {
        Class<? extends Activity> target = resolve(routeToClass);
        if (target != null) {
            Nav.pushAndRemoveUntil(from, target, routeToArgs);
        } else {
            Nav.pushAndRemoveUntil(from, fallback, null);
        }
    }

    /** Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => routeTo ?? Fallback())). */
    public static void pushReplacement(Activity from, String routeToClass, JsonObject routeToArgs,
                                       Class<? extends Activity> fallback) {
        Class<? extends Activity> target = resolve(routeToClass);
        if (target != null) {
            Nav.pushReplacement(from, target, routeToArgs);
        } else {
            Nav.pushReplacement(from, fallback, null);
        }
    }
}
