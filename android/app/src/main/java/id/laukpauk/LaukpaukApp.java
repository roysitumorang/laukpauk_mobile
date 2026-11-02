package id.laukpauk;

import android.app.Application;

import id.laukpauk.core.AppContainer;

/**
 * Replaces Flutter's main(): WidgetsFlutterBinding.ensureInitialized(), the HeaderViewModel provider
 * and the push-notification set-up that lived in _MyAppState._initialization.
 *
 * Also the owner of the composition root: the {@link AppContainer} is built once here, before any
 * Activity is created, and lives as long as the process.
 */
public class LaukpaukApp extends Application {

    private AppContainer container;

    @Override
    public void onCreate() {
        super.onCreate();
        container = new AppContainer(this);

        // main.dart: _sP.setBool('showMakeSureADJD', true); _sP.setBool('showAlertGift', true);
        container.prefs.setBool("showMakeSureADJD", true);
        container.prefs.setBool("showAlertGift", true);

        container.mainModel.initOneSignal(this);
        // MainActivity.java (delivery-schedule expiry timer + Play in-app update check), now process-wide
        container.mainModel.startBackgroundWatchers(this);
        container.appState.init();
    }

    /** The app's single dependency container. */
    public AppContainer container() {
        return container;
    }
}
