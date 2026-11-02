package id.laukpauk.ui.startup;

import android.app.Application;

import id.laukpauk.di.AppContainer;

/** PLACEHOLDER (contract only) - implemented by the startup slice. */
public final class AppStartup {
    private AppStartup() {}

    /** App-wide start-up: push notifications, delivery-schedule watcher, Play update check. */
    public static void start(Application app, AppContainer container) {
    }
}
