package id.laukpauk.ui;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import id.laukpauk.core.Async;

/**
 * Flutter's showDialog / showModalBottomSheet.
 *
 * Dart:
 *   final x = await showDialog<bool>(context: context, barrierDismissible: false,
 *       builder: (ctx) => Dialog(child: ... onPressed: () => Navigator.pop(ctx, true) ...));
 * Java:
 *   Modal.<Boolean>dialog(activity, false, false, handle ->
 *           /* build the view; call handle.close(Boolean.TRUE) from a button *\/ view,
 *       x -> { ... x is null when dismissed without a value ... });
 *
 * `onClosed` always runs on the main thread, exactly once, after the dialog is gone.
 */
public final class Modal {
    private Modal() {}

    /** `Navigator.pop(dialogContext, result)` for the dialog being built. */
    public interface Handle<T> {
        /** Closes the dialog and delivers `result` (may be null) to onClosed. */
        void close(T result);

        /** Closes without a value. */
        void close();

        /** The dialog's Activity (Dart: the BuildContext). */
        Activity activity();
    }

    /** Builds the dialog content; may keep the handle to close later (e.g. after a network call). */
    public interface Content<T> {
        View build(Handle<T> handle);
    }

    private static final class Holder<T> implements Handle<T> {
        final Activity activity;
        Dialog dialog;
        T result;
        boolean closed;

        Holder(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void close(T value) {
            result = value;
            closed = true;
            dismiss();
        }

        @Override
        public void close() {
            close(null);
        }

        private void dismiss() {
            if (dialog != null && dialog.isShowing()) {
                try {
                    dialog.dismiss();
                } catch (RuntimeException ignored) {
                    // window already detached
                }
            }
        }

        @Override
        public Activity activity() {
            return activity;
        }
    }

    private static boolean usable(Activity a) {
        return a != null && !a.isFinishing() && !a.isDestroyed();
    }

    /**
     * A centred dialog with a transparent window: the content view draws its own background and
     * rounded corners (like Flutter's Dialog(child: Container(decoration: ...))).
     *
     * @param fullScreen true = showDialog(useSafeArea: false) covering the whole screen.
     */
    public static <T> void dialog(final Activity activity, boolean dismissible, boolean fullScreen,
                                  Content<T> content, final Async.Done<T> onClosed) {
        if (!usable(activity)) {
            if (onClosed != null) {
                onClosed.run(null);
            }
            return;
        }
        final Holder<T> holder = new Holder<>(activity);
        final Dialog d = new Dialog(activity);
        holder.dialog = d;
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setCancelable(dismissible);
        d.setCanceledOnTouchOutside(dismissible);
        View view = content.build(holder);
        d.setContentView(view, new ViewGroup.LayoutParams(Ui.MATCH, fullScreen ? Ui.MATCH : Ui.WRAP));
        Window w = d.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(fullScreen ? Color.WHITE : Color.TRANSPARENT));
            int margin = fullScreen ? 0 : Ui.dp(activity, 16);
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    fullScreen ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT);
            if (!fullScreen) {
                w.getDecorView().setPadding(margin, 0, margin, 0);
            }
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        d.setOnDismissListener(di -> {
            if (onClosed != null) {
                onClosed.run(holder.closed ? holder.result : null);
            }
        });
        d.show();
    }

    /** A centred, non-fullscreen dialog. */
    public static <T> void dialog(Activity activity, boolean dismissible, Content<T> content,
                                  Async.Done<T> onClosed) {
        dialog(activity, dismissible, false, content, onClosed);
    }

    /** showModalBottomSheet. */
    public static <T> void bottomSheet(final Activity activity, boolean dismissible, Content<T> content,
                                       final Async.Done<T> onClosed) {
        if (!usable(activity)) {
            if (onClosed != null) {
                onClosed.run(null);
            }
            return;
        }
        final Holder<T> holder = new Holder<>(activity);
        final BottomSheetDialog d = new BottomSheetDialog(activity);
        holder.dialog = d;
        d.setCancelable(dismissible);
        d.setCanceledOnTouchOutside(dismissible);
        d.setContentView(content.build(holder));
        d.setOnDismissListener(di -> {
            if (onClosed != null) {
                onClosed.run(holder.closed ? holder.result : null);
            }
        });
        d.show();
    }
}
