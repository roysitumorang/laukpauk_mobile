package id.laukpauk.ui.common;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

/**
 * Returning a result to the previous screen with Navigation (the replacement for the Dart
 * {@code Navigator.pop(context, value)} / {@code await Navigator.push(...)} pair).
 *
 * <pre>
 * // screen B, when done:
 * NavResults.setResult(this, PinGMAPSFragment.RESULT_KEY, json.toString());   // then:
 * NavHostFragment.findNavController(this).popBackStack();
 *
 * // screen A, in onViewCreated:
 * NavResults.observe(this, PinGMAPSFragment.RESULT_KEY, (String json) -&gt; { ... });
 * </pre>
 * Values must be Bundle-able (String, Boolean, Integer, Long, Parcelable...). Complex objects are passed as
 * JSON strings, like navigation arguments. The observer fires once per result.
 */
public final class NavResults {
    private NavResults() {}

    /** Stores {@code value} on the previous back-stack entry (the screen that will be shown after popping). */
    public static void setResult(Fragment from, String key, Object value) {
        NavController nav = NavHostFragment.findNavController(from);
        NavBackStackEntry previous = nav.getPreviousBackStackEntry();
        if (previous != null) {
            previous.getSavedStateHandle().set(key, value);
        }
    }

    /** Observes results sent to this screen under {@code key}; each result is delivered once. */
    public static <T> void observe(Fragment owner, String key, Observer<T> observer) {
        NavController nav = NavHostFragment.findNavController(owner);
        NavBackStackEntry current = nav.getCurrentBackStackEntry();
        if (current == null) {
            return;
        }
        final androidx.lifecycle.SavedStateHandle handle = current.getSavedStateHandle();
        MutableLiveData<T> live = handle.getLiveData(key);
        live.observe(owner.getViewLifecycleOwner(), value -> {
            if (value != null) {
                handle.set(key, null);
                observer.onChanged(value);
            }
        });
    }
}
