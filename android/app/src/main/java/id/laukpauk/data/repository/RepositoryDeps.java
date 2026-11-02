package id.laukpauk.data.repository;

import android.app.Application;

import id.laukpauk.data.local.SessionStore;
import id.laukpauk.data.local.Prefs;
import id.laukpauk.data.remote.ApiClient;
import id.laukpauk.util.AppExecutors;

/**
 * The infrastructure every repository is built with. One instance, created by
 * {@link id.laukpauk.di.AppContainer}; repositories receive it in their constructor
 * ({@code public XxxRepository(RepositoryDeps deps)}), which keeps AppContainer generated and lets features
 * be added without editing a shared wiring file.
 */
public final class RepositoryDeps {
    public final Application application;
    public final ApiClient apiClient;
    public final SessionStore session;
    public final Prefs prefs;
    public final AppExecutors executors;

    public RepositoryDeps(Application application, ApiClient apiClient, SessionStore session, Prefs prefs,
                          AppExecutors executors) {
        this.application = application;
        this.apiClient = apiClient;
        this.session = session;
        this.prefs = prefs;
        this.executors = executors;
    }
}
