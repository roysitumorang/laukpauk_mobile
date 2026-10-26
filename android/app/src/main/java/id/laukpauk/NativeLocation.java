package id.laukpauk;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

/** Runtime-permission-aware wrapper around Android fused location services. */
public final class NativeLocation {
    public static final int REQUEST_LOCATION_PERMISSION = 4208;

    public interface Callback {
        void onLocation(Location location);
        void onUnavailable(Exception error);
    }

    private NativeLocation() { }

    public static void requestCurrent(Activity activity, Callback callback) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            callback.onUnavailable(new SecurityException("Location permission is required."));
            return;
        }
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(activity);
        CancellationTokenSource token = new CancellationTokenSource();
        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, token.getToken())
                .addOnSuccessListener(activity, location -> {
                    if (location != null) callback.onLocation(location);
                    else callback.onUnavailable(new IllegalStateException("Current location is unavailable."));
                })
                .addOnFailureListener(activity, callback::onUnavailable);
    }
}
