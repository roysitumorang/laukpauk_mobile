package id.laukpauk;

import android.app.Application;

import id.laukpauk.core.AppContainer;
import id.laukpauk.ui.startup.AppStartup;

/**
 * Replaces Flutter's main(): creates the dependency container once, before any screen, and starts the
 * app-wide services (push notifications, the delivery-schedule expiry watcher, the Play update check).
 */
public class LaukpaukApp extends Application {

    private id.laukpauk.di.AppContainer appContainer;
    /** LEGACY: the pre-Fragment screens' container; removed with them. */
    private AppContainer container;

    @Override
    public void onCreate() {
        super.onCreate();
        appContainer = new id.laukpauk.di.AppContainer(this);
        container = new AppContainer(this);
        AppStartup.start(this, appContainer);
    }

    /** The app's dependency container (see {@link id.laukpauk.di.AppContainer}). */
    public id.laukpauk.di.AppContainer appContainer() {
        return appContainer;
    }

    /** LEGACY accessor used by the old Activity-based screens; removed with them. */
    public AppContainer container() {
        return container;
    }
}
