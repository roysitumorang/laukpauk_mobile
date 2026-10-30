package id.laukpauk.model;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.google.gson.JsonObject;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;
import com.onesignal.notifications.INotification;
import com.onesignal.notifications.INotificationClickEvent;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import id.laukpauk.BuildConfig;
import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Environment;
import id.laukpauk.core.Json;
import id.laukpauk.core.Prefs;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Routing;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.SplashScreenActivity;

/**
 * Port of lib/models/MainModel.dart plus the native pieces of android/.../MainActivity.java / MyIntentService.java
 * (delivery-schedule expiry watcher, Play in-app update check). MyJobService did nothing useful and is not ported.
 */
public final class MainModel {
    private MainModel() {}

    private static final String PLAY_URL = "https://play.google.com/store/apps/details?id=id.laukpauk";
    private static final long WATCH_INTERVAL_MS = 5000L;

    // ------------------------------------------------------------------ OneSignal

    /** Dart: initOneSignal(globalKey, buildContext). Call from Application.onCreate. */
    public static void initOneSignal(final Application app) {
        String oneSignalKey;
        if (Environment.isProduction()) {
            oneSignalKey = BuildConfig.ONESIGNAL_APP_ID_PRODUCTION;
        } else {
            oneSignalKey = BuildConfig.ONESIGNAL_APP_ID_DEVELOPMENT;
        }

        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        OneSignal.getDebug().setAlertLevel(LogLevel.NONE);
        OneSignal.setConsentRequired(false);

        OneSignal.initWithContext(app, oneSignalKey);

        OneSignal.getNotifications().addClickListener((INotificationClickEvent event) -> {
            try {
                onNotificationOpened(app, event);
            } catch (RuntimeException e) {
                // Dart would have thrown inside the handler too (malformed additionalData)
            }
        });
    }

    private static void onNotificationOpened(Application app, INotificationClickEvent event) {
        INotification notification = event.getNotification();
        JSONObject additionalData = notification.getAdditionalData();

        if (additionalData != null) {
            String notifikasi = additionalData.optString("notifikasi", null);
            if (notifikasi == null) {
                return;
            }
            String notifikasiLC = notifikasi.toLowerCase(Locale.ROOT);

            BaseActivity current = resumed.get();
            Context ctx = (current != null && current.isAlive()) ? current : app;

            if (notifikasiLC.equals("khususuntukkamu")) {
                String routeName = additionalData.optString("routeName", null);
                String routeParams = additionalData.optString("routeParams", null);

                Intent intent = Routing.intentFor(ctx, routeName, routeParams);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                ctx.startActivity(intent);
            }

            if (notifikasiLC.equals("update")) {
                Object rawId = additionalData.opt("idNotifikasi");
                String idNotifikasi = String.valueOf(rawId);
                JsonObject args = Json.of(
                        "routeToClass", "id.laukpauk.view.pembeli.components.notifikasi.DetailNotifikasiGlobalActivity",
                        "routeToArgs", Json.of("idNotifikasi", idNotifikasi));
                Intent intent = Nav.intent(ctx, SplashScreenActivity.class, args);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intent);
            }
        }
    }

    // ------------------------------------------------------------------ deep links

    /**
     * Replaces firebaseOnLink (Firebase Dynamic Links is shut down): an ACTION_VIEW intent whose data is
     * https://host/<route>?<params> starts the routed screen as the only screen. Returns true when handled.
     */
    public static boolean handleDeepLink(Activity a, Intent intent) {
        if (a == null || intent == null) {
            return false;
        }
        Uri data = intent.getData();
        if (data == null) {
            return false;
        }
        String path = data.getPath();
        if (path == null || path.length() == 0 || path.equals("/")) {
            return false;
        }

        Map<String, String> decodedArguments = new LinkedHashMap<>();
        for (String name : data.getQueryParameterNames()) {
            List<String> values = data.getQueryParameters(name);
            if (values != null && !values.isEmpty()) {
                decodedArguments.put(name, values.get(values.size() - 1)); // Dart queryParameters: last one wins
            }
        }

        Intent routed = Routing.intentFor(a, path, decodedArguments);
        routed.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        a.startActivity(routed);
        return true;
    }

    // ------------------------------------------------------------------ update dialog

    /** Dart: askToUpdate(context) -> bool?. */
    public static void askToUpdate(final BaseActivity activity, Async.Done<Boolean> done) {
        View icon = Ui.asset(activity, "laukpauk-icon.png", 50f);
        String subTitle = "Update Yuk! Banyak fitur-fitur baru menanti kamu nih...";

        Modal.Content<Boolean> actions = handle -> CustomAlert.actionsRow(activity,
                CustomButton.button(activity, "Tidak", () -> handle.close(Boolean.FALSE),
                        new CustomButton.Opts().outline().color(CustomColors.textMuted)),
                Ui.spaceW(activity, 5),
                CustomButton.button(activity, "Update", () -> handle.close(Boolean.TRUE),
                        new CustomButton.Opts().color(CustomColors.green)));

        CustomAlert.<Boolean>textComponent(activity, icon, "Update Aplikasi Terbaru", subTitle, actions, null,
                false, done);
    }

    // ------------------------------------------------------------------ background watchers

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static WeakReference<BaseActivity> resumed = new WeakReference<>(null);
    private static boolean started;
    private static boolean notificationPermissionAsked;
    private static boolean updateChecked;
    private static boolean updateAvailable;
    private static boolean updatePromptShowing;
    private static String expiredHandledMap;
    private static boolean expiryRunning;

    /**
     * Replaces MyIntentService (Timer every 5 s) + the MainActivity update check + the native.code channel:
     * a main-thread Handler loop while the app process lives, acting on the resumed BaseActivity.
     */
    public static void startBackgroundWatchers(final Application app) {
        if (started) {
            return;
        }
        started = true;

        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityResumed(Activity activity) {
                if (!(activity instanceof BaseActivity)) {
                    return;
                }
                BaseActivity base = (BaseActivity) activity;
                resumed = new WeakReference<>(base);

                if (!notificationPermissionAsked) {
                    notificationPermissionAsked = true;
                    if (Build.VERSION.SDK_INT >= 33) {
                        // OneSignal.Notifications.requestPermission is a Kotlin suspend function: ask the platform directly.
                        base.askPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, null);
                    }
                }
                if (!updateChecked) {
                    updateChecked = true;
                    checkForUpdate(app);
                }
                if (updateAvailable) {
                    promptUpdate(base);
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                if (resumed.get() == activity) {
                    resumed = new WeakReference<>(null);
                }
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(Activity activity) {}

            @Override
            public void onActivityStopped(Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

            @Override
            public void onActivityDestroyed(Activity activity) {}
        });

        HANDLER.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    checkExpiry();
                } catch (RuntimeException e) {
                    // never let the loop die
                }
                HANDLER.postDelayed(this, WATCH_INTERVAL_MS);
            }
        }, WATCH_INTERVAL_MS);
    }

    private static void checkForUpdate(Application app) {
        try {
            AppUpdateManager manager = AppUpdateManagerFactory.create(app);
            manager.getAppUpdateInfo().addOnSuccessListener(info -> {
                if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    updateAvailable = true;
                    BaseActivity current = resumed.get();
                    if (current != null) {
                        promptUpdate(current);
                    }
                }
            });
        } catch (RuntimeException e) {
            // Play services / store not available: no update prompt
        }
    }

    /** Dart: native.code 'updateAvailable'. */
    private static void promptUpdate(final BaseActivity activity) {
        if (updatePromptShowing || !activity.isAlive()) {
            return;
        }
        updatePromptShowing = true;
        updateAvailable = false;
        askToUpdate(activity, (Boolean wannaUpdate) -> {
            updatePromptShowing = false;
            if (wannaUpdate != null && wannaUpdate) {
                try {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_URL)));
                } catch (RuntimeException e) {
                    // canLaunchUrl was false
                }
            } else if (activity.isAlive()) {
                activity.finishAffinity(); // Dart: exit(0)
            } else {
                updateAvailable = true; // the screen went away before the answer: ask again on the next resume
            }
        });
    }

    /** Dart: native.code 'jadwalDeliveryKadaluarsa', fired by the 5 s expiry check. */
    private static void checkExpiry() {
        String saved = Prefs.getString("savedJadwalDeliveryMap");
        if (saved == null) {
            expiredHandledMap = null;
            return;
        }
        final BaseActivity activity = resumed.get();
        // the original broadcast only reached MainActivity while it was resumed
        if (activity == null || !activity.isAlive() || expiryRunning || saved.equals(expiredHandledMap)) {
            return;
        }
        if (!isExpired(saved)) {
            return;
        }

        expiredHandledMap = saved;
        expiryRunning = true;
        TransaksiModel.removeTransactionSession(activity, v -> {
            BaseActivity current = resumed.get();
            HeaderModel.alertADJDKadaluarsa(current != null ? current : activity, d -> expiryRunning = false);
        });
    }

    /** The rules of MyIntentService.java. */
    private static boolean isExpired(String savedJadwalDeliveryMap) {
        try {
            JsonObject saved = Json.parseObject(savedJadwalDeliveryMap);

            String id = Json.str(saved, "id");
            String dayDeliveryLC = Json.str(saved, "day").toLowerCase(Locale.ROOT);

            LocalDateTime now = LocalDateTime.now();
            LocalDate nowDate = now.toLocalDate();
            LocalTime nowTime = now.toLocalTime();

            LocalTime savedADJDMaxTime = LocalTime.parse(Json.str(saved, "maxTime"));
            LocalDate savedADJDMaxDay = LocalDate.parse(Json.str(saved, "currentDate"));

            if (dayDeliveryLC.equals("besok")) {
                savedADJDMaxDay = savedADJDMaxDay.plusDays(1); // 1 hari kemudian
            }
            if (dayDeliveryLC.equals("lusa")) {
                savedADJDMaxDay = savedADJDMaxDay.plusDays(2); // 2 hari kemudian
            }

            if (id.equals("1")) {
                if (dayDeliveryLC.equals("besok") || dayDeliveryLC.equals("lusa")) {
                    // Pengantaran sessi pagi (id 1): pembatalan transaction session harus dijalankan sebelum
                    // pengantaran, jadi tanggal kadaluarsanya adalah savedADJDMaxDay - 1 hari.
                    savedADJDMaxDay = savedADJDMaxDay.minusDays(1);
                }
            }

            return nowDate.isEqual(savedADJDMaxDay) && nowTime.isAfter(savedADJDMaxTime);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
