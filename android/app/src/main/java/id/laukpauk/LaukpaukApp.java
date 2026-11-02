package id.laukpauk;

import android.app.Application;

import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.startup.AppStartup;

/**
 * Creates the dependency container once, before any screen, and starts the app-wide services (push
 * notifications, the delivery-schedule expiry watcher, the Play update check).
 */
public class LaukpaukApp extends Application {

    private AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        appContainer = new AppContainer(this);
        AppStartup.start(this, appContainer);
    }

    /** The app's dependency container. Screens use {@link AppContainer#from(android.content.Context)}. */
    public AppContainer appContainer() {
        return appContainer;
    }
}
