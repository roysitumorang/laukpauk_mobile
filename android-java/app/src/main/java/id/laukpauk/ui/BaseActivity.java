package id.laukpauk.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;

/**
 * Base class of every screen (the Dart "views/*.dart" StatefulWidgets that were pushed on the
 * Navigator).
 *
 * What it gives a screen, mapped to Flutter:
 *  - setRoot(view)          : Scaffold(body: ...)
 *  - args()                 : the constructor arguments of the widget (see {@link Nav})
 *  - async(work, done)      : `await` that is dropped when the screen is gone (no setState-after-dispose)
 *  - Nav.pushForResult(..)  : `final x = await Navigator.push(...)`;  finishWithResult(v): `Navigator.pop(context, v)`
 *  - showBusy()/hideBusy()  : a blocking progress dialog
 *  - onShown()              : WidgetsBinding.addPostFrameCallback / initState-time loading
 *
 * Screens build their view tree in code (see {@link Ui}); there are no layout XML files.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private boolean alive;
    private JsonObject args;
    private FrameLayout rootHolder;
    private ProgressDialog busy;
    private Async.Done<JsonElement> pendingResult;

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Async.Done<JsonElement> cb = pendingResult;
                pendingResult = null;
                if (cb == null) {
                    return;
                }
                JsonElement value = null;
                Intent data = result.getData();
                if (data != null && data.hasExtra(Nav.EXTRA_RESULT)) {
                    value = Json.parse(data.getStringExtra(Nav.EXTRA_RESULT));
                }
                cb.run(value);
            });

    // Runtime permissions (geolocator / image_picker permission prompts) and raw intent results
    // (camera, gallery, location-settings...). One request of each kind may be pending at a time.
    private Async.Done<Map<String, Boolean>> pendingPermissions;
    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Async.Done<Map<String, Boolean>> cb = pendingPermissions;
                pendingPermissions = null;
                if (cb != null) {
                    cb.run(result);
                }
            });

    private Async.Done<ActivityResult> pendingRawResult;
    private final ActivityResultLauncher<Intent> rawResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Async.Done<ActivityResult> cb = pendingRawResult;
                pendingRawResult = null;
                if (cb != null) {
                    cb.run(result);
                }
            });

    // ------------------------------------------------------------------ lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alive = true;
        args = Json.parseObject(getIntent() == null ? null : getIntent().getStringExtra(Nav.EXTRA_ARGS));

        // Transparent status bar over a light-green header, like SystemUiOverlayStyle(statusBarColor: transparent).
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setStatusBarColor(Color.TRANSPARENT);
        WindowCompat.setDecorFitsSystemWindows(w, false);

        rootHolder = new FrameLayout(this);
        rootHolder.setBackgroundColor(CustomColors.nearlyWhite);
        super.setContentView(rootHolder, new ViewGroup.LayoutParams(Ui.MATCH, Ui.MATCH));
    }

    @Override
    protected void onDestroy() {
        alive = false;
        hideBusy();
        super.onDestroy();
    }

    /** False once the screen is finishing/destroyed. Dart: `mounted`. */
    public boolean isAlive() {
        return alive && !isFinishing() && !isDestroyed();
    }

    // ------------------------------------------------------------------ content

    /**
     * Sets the screen content (Scaffold body). Status-bar padding is NOT added: screens whose
     * header is green put a {@link Layouts#statusBarSpace} on top, like Header.dart's
     * `padding: EdgeInsets.only(top: MediaQuery.of(context).padding.top)`.
     */
    public void setRoot(View content) {
        rootHolder.removeAllViews();
        rootHolder.addView(content, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }

    /** Alias that reads like the Dart widget tree. */
    public void setBody(View content) {
        setRoot(content);
    }

    /** Re-run a build function (Dart: setState(() {})). Call on the main thread. */
    public void rebuild(Runnable build) {
        if (isAlive()) {
            build.run();
        }
    }

    // ------------------------------------------------------------------ arguments

    /** The widget's constructor arguments as JSON (an empty object when none were passed). */
    public JsonObject args() {
        return args;
    }

    // ------------------------------------------------------------------ async

    /** Runs `work` in the background and `done` on the main thread ONLY while the screen is alive. */
    public <T> void async(Async.Work<T> work, final Async.Done<T> done) {
        Async.run(work, result -> {
            if (isAlive() && done != null) {
                done.run(result);
            }
        });
    }

    public <T> void async(Async.Work<T> work, final Async.Done<T> done, final Async.Failed failed) {
        Async.run(work, result -> {
            if (isAlive() && done != null) {
                done.run(result);
            }
        }, error -> {
            if (isAlive() && failed != null) {
                failed.run(error);
            }
        });
    }

    /** Post to the main thread when still alive. */
    public void post(Runnable r) {
        Async.main(() -> {
            if (isAlive()) {
                r.run();
            }
        });
    }

    public void postDelayed(Runnable r, long millis) {
        Async.mainDelayed(() -> {
            if (isAlive()) {
                r.run();
            }
        }, millis);
    }

    // ------------------------------------------------------------------ navigation results

    /** Used by {@link Nav#pushForResult}; one pending result at a time (like one awaited push). */
    void launchForResult(Intent intent, Async.Done<JsonElement> callback) {
        pendingResult = callback;
        resultLauncher.launch(intent);
    }

    // ------------------------------------------------------------------ permissions / raw results

    /** True when the permission is already granted. */
    public boolean hasPermission(String permission) {
        return checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Asks for runtime permissions and reports {permission -> granted} on the main thread (immediately
     * when all are already granted). Dart: Geolocator.requestPermission(), ImagePicker permissions.
     */
    public void askPermissions(String[] permissions, final Async.Done<Map<String, Boolean>> callback) {
        Map<String, Boolean> granted = new LinkedHashMap<>();
        boolean all = true;
        for (String p : permissions) {
            boolean ok = hasPermission(p);
            granted.put(p, ok);
            all = all && ok;
        }
        if (all) {
            if (callback != null) {
                callback.run(granted);
            }
            return;
        }
        pendingPermissions = callback;
        permissionLauncher.launch(permissions);
    }

    /**
     * startActivityForResult with the raw result (resultCode + data Intent): camera / gallery / settings
     * screens that are not our own Activities. For our own screens use {@link Nav#pushForResult}.
     */
    public void startForRawResult(Intent intent, Async.Done<ActivityResult> callback) {
        pendingRawResult = callback;
        rawResultLauncher.launch(intent);
    }

    /** Navigator.pop(context). */
    public void pop() {
        finish();
    }

    /** Navigator.pop(context, value). `value` may be a JsonObject, JsonArray, primitive or null. */
    public void finishWithResult(JsonElement value) {
        Intent data = new Intent();
        if (value != null) {
            data.putExtra(Nav.EXTRA_RESULT, Json.toJson(value));
        }
        setResult(RESULT_OK, data);
        finish();
    }

    public void finishWithResult(boolean value) {
        finishWithResult(new com.google.gson.JsonPrimitive(value));
    }

    public void finishWithResult(String value) {
        finishWithResult(value == null ? null : new com.google.gson.JsonPrimitive(value));
    }

    // ------------------------------------------------------------------ busy indicator

    @SuppressWarnings("deprecation")
    public void showBusy() {
        if (!isAlive()) {
            return;
        }
        if (busy == null) {
            busy = new ProgressDialog(this);
            busy.setCancelable(false);
            busy.setMessage("Mohon tunggu...");
        }
        if (!busy.isShowing()) {
            busy.show();
        }
    }

    public void hideBusy() {
        if (busy != null && busy.isShowing()) {
            try {
                busy.dismiss();
            } catch (RuntimeException ignored) {
                // window already gone
            }
        }
    }

    // ------------------------------------------------------------------ misc

    public void hideKeyboard() {
        View v = getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            v.clearFocus();
        }
    }

    public void toast(String message) {
        Ui.toast(this, message);
    }
}
