package id.laukpauk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.Json;

/**
 * Flutter's Navigator for Activities.
 *
 * ARGUMENTS CONVENTION (every screen and every caller follows it):
 *   A screen is opened with a JsonObject whose keys are the FIELD NAMES of the Dart widget's
 *   constructor (positional or named alike). Types: String->string, int/double->number,
 *   bool->boolean, List->array, Map->object, dynamic->any JSON. The screen reads them with
 *   {@code args()} (a null-safe JsonObject; use the Json.* helpers).
 *
 *   Dart:  Navigator.push(context, MaterialPageRoute(builder: (_) => DetailProduk(idProduk)))
 *   Java:  Nav.push(this, DetailProdukActivity.class, Json.of("idProduk", idProduk));
 *
 *   Dart:  final r = await Navigator.push(context, MaterialPageRoute(builder: (_) => PinGMAPS(
 *              currentLatitude: lat, currentLongitude: lng)));
 *   Java:  Nav.pushForResult(this, PinGMAPSActivity.class,
 *              Json.of("currentLatitude", lat, "currentLongitude", lng), r -> { ... r may be null ... });
 *   and PinGMAPSActivity ends with finishWithResult(Json.of("lat", ..., "lng", ...)).
 */
public final class Nav {
    private Nav() {}

    public static final String EXTRA_ARGS = "laukpauk.args";
    public static final String EXTRA_RESULT = "laukpauk.result";

    public static Intent intent(Context ctx, Class<? extends Activity> screen, JsonObject args) {
        Intent i = new Intent(ctx, screen);
        if (args != null) {
            i.putExtra(EXTRA_ARGS, Json.toJson(args));
        }
        return i;
    }

    /** Navigator.push(context, MaterialPageRoute(builder: (_) => Screen(...))). */
    public static void push(Context ctx, Class<? extends Activity> screen, JsonObject args) {
        Intent i = intent(ctx, screen, args);
        if (!(ctx instanceof Activity)) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        ctx.startActivity(i);
    }

    public static void push(Context ctx, Class<? extends Activity> screen) {
        push(ctx, screen, null);
    }

    /**
     * `await Navigator.push(...)`: `onResult` runs on the main thread when the pushed screen
     * closes; the value is whatever it passed to finishWithResult, or null (back button / pop()).
     */
    public static void pushForResult(BaseActivity from, Class<? extends Activity> screen,
                                     JsonObject args, Async.Done<JsonElement> onResult) {
        from.launchForResult(intent(from, screen, args), onResult);
    }

    /** Navigator.pushAndRemoveUntil(context, route, (route) => false): the screen becomes the only one. */
    public static void pushAndRemoveUntil(Context ctx, Class<? extends Activity> screen, JsonObject args) {
        Intent i = intent(ctx, screen, args);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ctx.startActivity(i);
        if (ctx instanceof Activity) {
            ((Activity) ctx).finish();
        }
    }

    public static void pushAndRemoveUntil(Context ctx, Class<? extends Activity> screen) {
        pushAndRemoveUntil(ctx, screen, null);
    }

    /** Navigator.pushReplacement. */
    public static void pushReplacement(Activity from, Class<? extends Activity> screen, JsonObject args) {
        from.startActivity(intent(from, screen, args));
        from.finish();
    }

    /** Navigator.pop(context). */
    public static void pop(Activity a) {
        a.finish();
    }

    /** Navigator.popUntil to the first screen (the app's root) - finishes everything but the root. */
    public static void popToRoot(Activity a) {
        Intent i = new Intent(a, id.laukpauk.view.SplashScreenActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        a.startActivity(i);
    }
}
