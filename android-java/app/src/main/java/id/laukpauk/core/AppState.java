package id.laukpauk.core;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import id.laukpauk.model.ADJDModel;
import id.laukpauk.model.HeaderModel;

/**
 * Replacement for lib/view-models/HeaderViewModel.dart (a Provider ChangeNotifier).
 *
 * Holds the cart badge, the notification badge and the saved delivery address that the header
 * shows. Screens that display them register a Listener in onStart/onResume and remove it in
 * onStop/onPause; listeners are always invoked on the main thread.
 *
 *   AppState.get().setJumlahCart(3);            // Dart: Provider.of<HeaderViewModel>(..).jumlahCart = 3
 *   int n = AppState.get().getJumlahCart();     // Dart: headerViewModel.jumlahCart
 */
public final class AppState {
    /** Called on the main thread whenever any value changes. */
    public interface Listener {
        void onChanged();
    }

    private static final AppState INSTANCE = new AppState();

    public static AppState get() {
        return INSTANCE;
    }

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private volatile int jumlahCart = 0;
    private volatile int jumlahNotifikasi = 0;
    private volatile boolean isADJDSet = false;
    private volatile String alamatDelivery = "";
    private volatile JsonObject savedADJD = null;

    private AppState() {}

    public void addListener(Listener l) {
        if (l != null && !listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    private void notifyListeners() {
        Async.main(new Runnable() {
            @Override
            public void run() {
                for (Listener l : listeners) {
                    l.onChanged();
                }
            }
        });
    }

    public int getJumlahCart() {
        return jumlahCart;
    }

    public void setJumlahCart(int value) {
        jumlahCart = value;
        notifyListeners();
    }

    public int getJumlahNotifikasi() {
        return jumlahNotifikasi;
    }

    public void setJumlahNotifikasi(int value) {
        jumlahNotifikasi = value;
        notifyListeners();
    }

    public boolean isADJDSet() {
        return isADJDSet;
    }

    public void setADJDSet(boolean value) {
        isADJDSet = value;
        notifyListeners();
    }

    public String getAlamatDelivery() {
        return alamatDelivery;
    }

    public void setAlamatDelivery(String value) {
        alamatDelivery = value == null ? "" : value;
        notifyListeners();
    }

    /** Keys: latitude, longitude, jadwalDeliveryCode, alamat (see LocalStorage). May be null. */
    public JsonObject getSavedADJD() {
        return savedADJD;
    }

    public void setSavedADJD(JsonObject value) {
        savedADJD = value;
        notifyListeners();
    }

    /**
     * Port of the HeaderViewModel constructor: loads the cart count, the notification count
     * (when logged in) and the saved delivery address. Call once from Application.onCreate;
     * runs in the background.
     */
    public void init() {
        Async.bg(new Runnable() {
            @Override
            public void run() {
                refreshAll();
            }
        });
    }

    /** BLOCKING (network). Reloads everything the header displays. */
    public void refreshAll() {
        jumlahCart = HeaderModel.getJumlahCart();

        if (AuthModel.isLogin()) {
            jumlahNotifikasi = HeaderModel.getJumlahNotifikasi();
        } else {
            jumlahNotifikasi = 0;
        }

        boolean adjdSet = LocalStorage.isAlamatDeliveryDanJadwalDeliverySet();
        isADJDSet = adjdSet;
        if (adjdSet) {
            JsonObject saved = LocalStorage.getSavedLatitudeAndLongitude();
            String alamat = Json.strOrNull(saved, "alamat");
            alamatDelivery = alamat != null ? alamat : ADJDModel.defaultAlamat;
            savedADJD = saved;
        }
        notifyListeners();
    }

    /** Background refresh of just the two badges (cart + notifications). */
    public void refreshBadges() {
        Async.bg(new Runnable() {
            @Override
            public void run() {
                jumlahCart = HeaderModel.getJumlahCart();
                if (AuthModel.isLogin()) {
                    jumlahNotifikasi = HeaderModel.getJumlahNotifikasi();
                } else {
                    jumlahNotifikasi = 0;
                }
                notifyListeners();
            }
        });
    }
}
