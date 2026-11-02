package id.laukpauk.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Background and main-thread executors (the AppExecutors pattern from the Android architecture samples).
 * Network calls go through Retrofit's own dispatcher; use {@link #diskIO()} for other blocking work
 * (preferences, files, image processing) and {@link #mainThread()} to post results to the UI.
 */
public final class AppExecutors {

    private final ExecutorService diskIO = Executors.newFixedThreadPool(2);
    private final Executor mainThread = new MainThreadExecutor();

    public ExecutorService diskIO() {
        return diskIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    private static final class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            handler.post(command);
        }
    }
}
