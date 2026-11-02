package id.laukpauk.ui.location;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.model.delivery.PlaceSuggestion;
import id.laukpauk.data.repository.DeliveryRepository;

/** State of the map picker: the selected position and the address search suggestions. */
public class PinGMAPSViewModel extends ViewModel {

    /** A chosen position (legacy selectedPosition). */
    public static final class Position {
        public final String formattedAddress;
        public final double lat;
        public final double lng;

        Position(String formattedAddress, double lat, double lng) {
            this.formattedAddress = formattedAddress;
            this.lat = lat;
            this.lng = lng;
        }
    }

    private static final long DEBOUNCE_MS = 300L;

    private final DeliveryRepository repo;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<Position> position = new MutableLiveData<>();
    /** null = suggestions hidden. */
    private final MutableLiveData<List<PlaceSuggestion>> suggestions = new MutableLiveData<>();
    private int requestCounter;
    private Runnable pendingQuery;
    private boolean initialized;

    public PinGMAPSViewModel(DeliveryRepository repo) {
        this.repo = repo;
    }

    public LiveData<Position> position() {
        return position;
    }

    public LiveData<List<PlaceSuggestion>> suggestions() {
        return suggestions;
    }

    /** Legacy addInitialMarker: geocode the given start position once and move there. */
    public void init(String latText, String lngText) {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            double lat = Double.parseDouble(latText.trim());
            double lng = Double.parseDouble(lngText.trim());
            moveTo(lat, lng);
        } catch (NumberFormatException | NullPointerException e) {
            // Dart: double.parse throws inside an un-awaited future
        }
    }

    /** Map long press / initial position: reverse geocode, then select. */
    public void moveTo(double lat, double lng) {
        repo.reverseGeocode(String.valueOf(lat), String.valueOf(lng), r -> {
            String address = r.isSuccess() && r.data != null ? r.data : "Alamat Tidak Diketahui";
            select(address, lat, lng);
        });
    }

    public void select(String formattedAddress, double lat, double lng) {
        position.setValue(new Position(formattedAddress, lat, lng));
    }

    /** Debounced Places search (legacy 300 ms). */
    public void query(String pattern) {
        if (pendingQuery != null) {
            handler.removeCallbacks(pendingQuery);
        }
        pendingQuery = () -> queryNow(pattern);
        handler.postDelayed(pendingQuery, DEBOUNCE_MS);
    }

    public void queryNow(String pattern) {
        final int mine = ++requestCounter;
        repo.searchPlaces(pattern, r -> {
            if (mine == requestCounter) {
                suggestions.setValue(r.data != null ? r.data : new java.util.ArrayList<PlaceSuggestion>());
            }
        });
    }

    public void hideSuggestions() {
        requestCounter++;
        if (pendingQuery != null) {
            handler.removeCallbacks(pendingQuery);
        }
        suggestions.setValue(null);
    }

    @Override
    protected void onCleared() {
        handler.removeCallbacksAndMessages(null);
    }
}
