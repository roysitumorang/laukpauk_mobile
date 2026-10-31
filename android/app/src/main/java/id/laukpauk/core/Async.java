package id.laukpauk.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tiny stand-in for Dart's Future/await.
 *
 *   Async.run(() -> ProdukModel.getDetailProduk(id),      // background thread (blocking calls OK)
 *             detail -> render(detail));                  // main thread
 *
 * In Activities prefer BaseActivity.async(...) which skips the UI callback once the screen is gone.
 */
public final class Async {
    private Async() {}

    private static final String TAG = "Async";
    private static final ExecutorService POOL = Executors.newFixedThreadPool(4);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /** Background work that may throw. */
    public interface Work<T> {
        T run() throws Exception;
    }

    /** Receives a result on the main thread. Also the callback type of UI-flow model methods. */
    public interface Done<T> {
        void run(T result);
    }

    /** Receives the failure of a Work on the main thread. */
    public interface Failed {
        void run(Exception error);
    }

    /** Runs `work` off the main thread, then `done` on the main thread. On exception: done(null). */
    public static <T> void run(final Work<T> work, final Done<T> done) {
        run(work, done, null);
    }

    public static <T> void run(final Work<T> work, final Done<T> done, final Failed failed) {
        POOL.execute(new Runnable() {
            @Override
            public void run() {
                T result = null;
                Exception error = null;
                try {
                    result = work.run();
                } catch (Exception e) {
                    error = e;
                    Log.w(TAG, "background work failed", e);
                }
                final T finalResult = result;
                final Exception finalError = error;
                MAIN.post(new Runnable() {
                    @Override
                    public void run() {
                        if (finalError != null && failed != null) {
                            failed.run(finalError);
                        } else if (done != null) {
                            done.run(finalResult);
                        }
                    }
                });
            }
        });
    }

    /** Fire-and-forget background task. */
    public static void bg(Runnable task) {
        POOL.execute(task);
    }

    /** Post to the main thread. */
    public static void main(Runnable task) {
        MAIN.post(task);
    }

    public static void mainDelayed(Runnable task, long delayMillis) {
        MAIN.postDelayed(task, delayMillis);
    }

    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /** Dart: await Future.delayed(...) - only call this from a background thread. */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
