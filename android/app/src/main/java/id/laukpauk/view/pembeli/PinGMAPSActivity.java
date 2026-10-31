package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.ADJDModel;
import id.laukpauk.ui.Ui;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.view.pembeli.components.homepage.CariGoogleMaps;

/**
 * Port of lib/views/pembeli/PinGMAPS.dart: pick a delivery address on a map (long-press, search box
 * or the given start position). Args: currentLatitude, currentLongitude (strings, default Merdeka
 * Walk Medan), appBarText (default 'Pilih Alamat Pengantaran'), formattedAddress (unused, as in Dart).
 * Result (finishWithResult): {formattedAddress, lat, lng} (strings); null when the user just goes back.
 */
public class PinGMAPSActivity extends BaseActivity {
    private static final double DEFAULT_LAT = 3.5889242;
    private static final double DEFAULT_LNG = 98.6790958;

    private MapView mapView;
    private GoogleMap googleMap;
    private final List<Runnable> whenMapReady = new ArrayList<>();
    private JsonObject selectedPosition; // {formattedAddress, lat, lng}
    private TextView tetapkan;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        JsonObject a = args();
        final String currentLatitude = Json.has(a, "currentLatitude") ? Json.str(a, "currentLatitude") : "3.5889242";
        final String currentLongitude = Json.has(a, "currentLongitude") ? Json.str(a, "currentLongitude") : "98.6790958";
        String appBarText = Json.has(a, "appBarText") ? Json.str(a, "appBarText") : "Pilih Alamat Pengantaran";

        LinearLayout root = Ui.column(this);
        root.addView(DetailUi.appBar(this, appBarText));

        FrameLayout body = new FrameLayout(this);
        root.addView(body, Ui.weightH(1f));

        GoogleMapOptions options = new GoogleMapOptions()
                .camera(new CameraPosition(new LatLng(DEFAULT_LAT, DEFAULT_LNG), 15.0f, 0f, 0f));
        mapView = new MapView(this, options);
        mapView.onCreate(b);
        body.addView(mapView, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        mapView.getMapAsync(map -> {
            googleMap = map;
            // initial marker: MarkerId('defaultMarker') at Merdeka Walk Medan
            map.addMarker(new MarkerOptions().position(new LatLng(DEFAULT_LAT, DEFAULT_LNG)));
            map.setOnMapLongClickListener(newLatLong -> async(
                    () -> ADJDModel.reverseGeocoding(newLatLong.latitude, newLatLong.longitude),
                    formattedAddress -> pindahLokasi(formattedAddress, newLatLong.latitude,
                            newLatLong.longitude)));
            List<Runnable> pending = new ArrayList<>(whenMapReady);
            whenMapReady.clear();
            for (Runnable r : pending) {
                r.run();
            }
        });

        // Positioned(top: verticalBlock * 1.5, child: CariGoogleMaps(...))
        CariGoogleMaps cari = new CariGoogleMaps(this, (JsonObject selectedSuggestion) -> {
            JsonObject location = Json.object(selectedSuggestion, "location");
            double latitude = Json.dbl(location, "lat");
            double longitude = Json.dbl(location, "lng");
            String formattedAddress = Json.str(selectedSuggestion, "formattedAddress");
            pindahLokasi(formattedAddress, latitude, longitude);
        });
        FrameLayout.LayoutParams cariLp = new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP, Gravity.TOP);
        cariLp.topMargin = Ui.dp(this, Ui.vb(this) * 1.5f);
        body.addView(cari, cariLp);

        // 'Tetapkan Lokasi' button, only once a position is selected
        tetapkan = Ui.text(this, "Tetapkan Lokasi", Ui.hb(this) * 3.5f, CustomColors.white);
        tetapkan.setGravity(Gravity.CENTER);
        Ui.pad(tetapkan, 25, 10, 25, 10);
        Ui.background(tetapkan, CustomColors.green, 25);
        tetapkan.setElevation(Ui.dp(this, 8));
        tetapkan.setVisibility(android.view.View.GONE);
        Ui.onTap(tetapkan, () -> {
            if (selectedPosition != null) {
                finishWithResult(selectedPosition);
            }
        });
        FrameLayout.LayoutParams tLp = new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        tLp.bottomMargin = Ui.dp(this, Ui.vb(this) * 3.5f);
        body.addView(tetapkan, tLp);

        setRoot(root);

        addInitialMarker(currentLatitude, currentLongitude);
    }

    /** addInitialMarker(): reverse-geocode the given position and move there. */
    private void addInitialMarker(String latitudeText, String longitudeText) {
        final double currentLatitude;
        final double currentLongitude;
        try {
            currentLatitude = Double.parseDouble(latitudeText.trim());
            currentLongitude = Double.parseDouble(longitudeText.trim());
        } catch (NumberFormatException e) {
            return; // Dart: double.parse throws inside an un-awaited future
        }
        async(() -> ADJDModel.reverseGeocoding(currentLatitude, currentLongitude),
                formattedAddress -> pindahLokasi(formattedAddress, currentLatitude, currentLongitude));
    }

    private void pindahLokasi(final String formattedAddress, final double latitude, final double longitude) {
        final String stringLat = String.valueOf(latitude);
        final String stringLng = String.valueOf(longitude);

        Runnable move = () -> {
            LatLng newLatLong = new LatLng(latitude, longitude);
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition(newLatLong, 16.0f, 0f, 0f)));

            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(newLatLong)
                    .icon(BitmapDescriptorFactory.defaultMarker()));

            selectedPosition = Json.of("formattedAddress", formattedAddress, "lat", stringLat, "lng", stringLng);
            tetapkan.setVisibility(android.view.View.VISIBLE);
        };
        if (googleMap != null) {
            move.run();
        } else {
            whenMapReady.add(move);
        }
    }

    // ------------------------------------------------------------------ MapView lifecycle

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
