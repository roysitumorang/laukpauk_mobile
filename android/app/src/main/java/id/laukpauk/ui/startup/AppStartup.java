package id.laukpauk.ui.startup;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.gson.JsonObject;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;
import com.onesignal.notifications.INotification;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

import id.laukpauk.BuildConfig;
import id.laukpauk.MainActivity;
import id.laukpauk.R;
import id.laukpauk.databinding.DialogStartupUpdateBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.delivery.DeliveryFlow;
import id.laukpauk.util.Environment;
import id.laukpauk.util.Json;

/**
 * Everything the app does at start (legacy MainModel + LaukpaukApp + native MainActivity/MyIntentService):
 * preference flags, OneSignal (init, external user id, notification taps), the delivery-schedule expiry
 * watcher (every 5 s while MainActivity is resumed), the Play in-app update check and the notification
 * permission request.
 */
public final class AppStartup {
    private AppStartup() {}

    private static final String PLAY_URL = "https://play.google.com/store/apps/details?id=id.laukpauk";
    private static final long WATCH_INTERVAL_MS = 5000L;
    private static final int REQUEST_POST_NOTIFICATIONS = 4101;

    private static boolean started;

    /** App-wide start-up: push notifications, delivery-schedule watcher, Play update check. */
    public static void start(Application app, AppContainer container) {
        if (started) {
            return;
        }
        started = true;

        // main.dart: _sP.setBool('showMakeSureADJD', true); _sP.setBool('showAlertGift', true);
        container.prefs.setBool("showMakeSureADJD", true);
        container.prefs.setBool("showAlertGift", true);

        initOneSignal(app, container);
        app.registerActivityLifecycleCallbacks(new Watcher(app, container));
    }

    // ------------------------------------------------------------------ OneSignal

    private static void initOneSignal(final Application app, AppContainer container) {
        String oneSignalKey = Environment.isProduction()
                ? BuildConfig.ONESIGNAL_APP_ID_PRODUCTION
                : BuildConfig.ONESIGNAL_APP_ID_DEVELOPMENT;

        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        OneSignal.getDebug().setAlertLevel(LogLevel.NONE);
        OneSignal.setConsentRequired(false);
        OneSignal.initWithContext(app, oneSignalKey);

        // signed-in users are known to OneSignal by their user id
        String idUser = container.session.getCurrentUserID();
        if (!container.session.getAuthToken().isEmpty() && !idUser.isEmpty()) {
            OneSignal.login(idUser);
        }

        OneSignal.getNotifications().addClickListener(event -> {
            try {
                onNotificationOpened(app, event.getNotification());
            } catch (RuntimeException e) {
                // malformed additionalData: ignore the tap
            }
        });
    }

    /** Opens MainActivity with an intent {@link MainRouter#handle} understands. */
    private static void onNotificationOpened(Application app, INotification notification) {
        JSONObject additionalData = notification.getAdditionalData();
        if (additionalData == null) {
            return;
        }
        String notifikasi = additionalData.optString("notifikasi", null);
        if (notifikasi == null) {
            return;
        }
        Intent intent = new Intent(app, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainRouter.EXTRA_NOTIFICATION, notifikasi);
        intent.putExtra(MainRouter.EXTRA_ROUTE_NAME, additionalData.optString("routeName", null));
        intent.putExtra(MainRouter.EXTRA_ROUTE_PARAMS, additionalData.optString("routeParams", null));
        Object rawId = additionalData.opt("idNotifikasi");
        if (rawId != null) {
            intent.putExtra(MainRouter.EXTRA_ID_NOTIFIKASI, String.valueOf(rawId));
        }
        app.startActivity(intent);
    }

    // ------------------------------------------------------------------ watchers

    /** Tracks the resumed MainActivity; runs the expiry loop, the update check and the permission request. */
    private static final class Watcher implements Application.ActivityLifecycleCallbacks {
        private final Application app;
        private final AppContainer c;
        private final Handler handler = new Handler(Looper.getMainLooper());
        private WeakReference<MainActivity> resumed = new WeakReference<>(null);
        private boolean notificationPermissionAsked;
        private boolean updateChecked;
        private boolean updateAvailable;
        private boolean updatePromptShowing;
        private boolean expiryRunning;

        private final Runnable loop = new Runnable() {
            @Override
            public void run() {
                try {
                    checkExpiry();
                } catch (RuntimeException e) {
                    // never let the loop die
                }
                if (resumed.get() != null) {
                    handler.postDelayed(this, WATCH_INTERVAL_MS);
                }
            }
        };

        Watcher(Application app, AppContainer c) {
            this.app = app;
            this.c = c;
        }

        @Override
        public void onActivityResumed(Activity activity) {
            if (!(activity instanceof MainActivity)) {
                return;
            }
            MainActivity main = (MainActivity) activity;
            resumed = new WeakReference<>(main);

            if (!notificationPermissionAsked) {
                notificationPermissionAsked = true;
                if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(main,
                        Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(main,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATIONS);
                }
            }
            if (!updateChecked) {
                updateChecked = true;
                checkForUpdate();
            }
            if (updateAvailable) {
                promptUpdate(main);
            }
            handler.removeCallbacks(loop);
            handler.postDelayed(loop, WATCH_INTERVAL_MS);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            if (resumed.get() == activity) {
                resumed = new WeakReference<>(null);
                handler.removeCallbacks(loop);
            }
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }

        // ---- Play in-app update

        private void checkForUpdate() {
            try {
                AppUpdateManager manager = AppUpdateManagerFactory.create(app);
                manager.getAppUpdateInfo().addOnSuccessListener(info -> {
                    if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        updateAvailable = true;
                        MainActivity current = resumed.get();
                        if (current != null) {
                            promptUpdate(current);
                        }
                    }
                });
            } catch (RuntimeException e) {
                // Play services / store not available: no update prompt
            }
        }

        /** Legacy askToUpdate: "Update Aplikasi Terbaru" with Tidak (exit) / Update (Play Store). */
        private void promptUpdate(final MainActivity activity) {
            if (updatePromptShowing || activity.isFinishing()) {
                return;
            }
            updatePromptShowing = true;
            updateAvailable = false;
            DialogStartupUpdateBinding b = DialogStartupUpdateBinding.inflate(LayoutInflater.from(activity));
            Images.asset(b.icon, "laukpauk-icon.png");
            new MaterialAlertDialogBuilder(activity)
                    .setView(b.getRoot())
                    .setCancelable(false)
                    .setNegativeButton(R.string.startup_update_tidak, (d, w) -> {
                        updatePromptShowing = false;
                        activity.finishAffinity(); // Dart: exit(0)
                    })
                    .setPositiveButton(R.string.startup_update_update, (d, w) -> {
                        updatePromptShowing = false;
                        try {
                            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_URL)));
                        } catch (RuntimeException e) {
                            // no browser / store
                        }
                    })
                    .show();
        }

        // ---- delivery schedule expiry

        private void checkExpiry() {
            JsonObject saved = c.session.savedJadwalDeliveryMap();
            if (saved == null || expiryRunning) {
                return;
            }
            final MainActivity activity = resumed.get();
            if (activity == null || activity.isFinishing() || !isExpired(saved)) {
                return;
            }
            expiryRunning = true;
            c.cartRepository.clearTransactionSession(result -> {
                MainActivity current = resumed.get();
                Fragment fragment = current == null ? null : current.currentFragment();
                if (fragment == null) {
                    expiryRunning = false;
                    return;
                }
                DeliveryFlow.alertExpired(fragment, () -> expiryRunning = false);
            });
        }

        /** The rules of MyIntentService.java. */
        private boolean isExpired(JsonObject saved) {
            try {
                String id = Json.str(saved, "id");
                String dayDeliveryLC = Json.str(saved, "day").toLowerCase(Locale.ROOT);

                LocalDateTime now = LocalDateTime.now();
                LocalDate nowDate = now.toLocalDate();
                LocalTime nowTime = now.toLocalTime();

                LocalTime savedADJDMaxTime = LocalTime.parse(Json.str(saved, "maxTime"));
                LocalDate savedADJDMaxDay = LocalDate.parse(Json.str(saved, "currentDate"));

                if (dayDeliveryLC.equals("besok")) {
                    savedADJDMaxDay = savedADJDMaxDay.plusDays(1);
                }
                if (dayDeliveryLC.equals("lusa")) {
                    savedADJDMaxDay = savedADJDMaxDay.plusDays(2);
                }
                if (id.equals("1") && (dayDeliveryLC.equals("besok") || dayDeliveryLC.equals("lusa"))) {
                    // morning session: the transaction session must be cancelled the day before delivery
                    savedADJDMaxDay = savedADJDMaxDay.minusDays(1);
                }
                return nowDate.isEqual(savedADJDMaxDay) && nowTime.isAfter(savedADJDMaxTime);
            } catch (RuntimeException e) {
                return false;
            }
        }
    }
}
