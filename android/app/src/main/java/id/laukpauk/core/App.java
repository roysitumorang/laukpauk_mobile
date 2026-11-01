package id.laukpauk.core;

import android.app.Application;
import android.content.Context;

/**
 * Holds the application context so that non-UI helper classes (Prefs, Http, LocalStorage, ...)
 * can be called statically, the way the Dart code called its static helpers.
 */
public final class App {
    private App() {}

    private static Application application;

    public static void init(Application app) {
        application = app;
    }

    public static Context context() {
        if (application == null) {
            throw new IllegalStateException("App.init(Application) has not been called");
        }
        return application;
    }

    public static Application application() {
        return application;
    }
}
