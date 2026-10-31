package id.laukpauk;

import android.app.Application;

import id.laukpauk.core.App;
import id.laukpauk.core.AppState;
import id.laukpauk.model.MainModel;

/**
 * Replaces Flutter's main(): WidgetsFlutterBinding.ensureInitialized(), the HeaderViewModel provider
 * and the push-notification set-up that lived in _MyAppState._initialization.
 */
public class LaukpaukApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        App.init(this);

        // main.dart: _sP.setBool('showMakeSureADJD', true); _sP.setBool('showAlertGift', true);
        id.laukpauk.core.Prefs.setBool("showMakeSureADJD", true);
        id.laukpauk.core.Prefs.setBool("showAlertGift", true);

        MainModel.initOneSignal(this);
        // MainActivity.java (delivery-schedule expiry timer + Play in-app update check), now process-wide
        MainModel.startBackgroundWatchers(this);
        AppState.get().init();
    }
}
