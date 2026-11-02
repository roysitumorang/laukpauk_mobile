package id.laukpauk.ui.startup;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import id.laukpauk.data.repository.CartRepository;
import id.laukpauk.data.repository.StartupRepository;

/** Decides where the splash screen continues after its delay (legacy SplashScreen._initialization). */
public class SplashViewModel extends ViewModel {

    /** Continue to the destination given in the {@code routeTo} argument. */
    public static final int GO_ROUTE = 100;

    private final StartupRepository startup;
    private final CartRepository cart;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<Integer> destination = new MutableLiveData<>();
    private boolean started;

    public SplashViewModel(StartupRepository startup, CartRepository cart) {
        this.startup = startup;
        this.cart = cart;
    }

    /** One of StartupRepository.GO_* or {@link #GO_ROUTE}; set once. */
    public LiveData<Integer> getDestination() {
        return destination;
    }

    public void start(boolean removeTransactionSession, int routeTo) {
        if (started) {
            return;
        }
        started = true;
        if (removeTransactionSession) {
            cart.clearTransactionSession(result -> decideLater(routeTo));
        } else {
            decideLater(routeTo);
        }
    }

    private void decideLater(int routeTo) {
        if (routeTo != 0) {
            handler.postDelayed(() -> destination.setValue(GO_ROUTE), 3000);
        } else {
            // 1.5 s page delay + the 1 s of SplashScreenModel.checkHasAppStarted
            handler.postDelayed(() -> destination.setValue(startup.decideDestination()), 2500);
        }
    }

    @Override
    protected void onCleared() {
        handler.removeCallbacksAndMessages(null);
    }
}
