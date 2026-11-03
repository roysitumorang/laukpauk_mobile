package id.laukpauk.ui.common;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;

/**
 * Offers the device's phone number for a phone field with Google's Phone Number Hint API: when the
 * (empty) field gets focus for the first time, a system sheet lists the SIM numbers; picking one fills the
 * field in the local format the backend expects ({@code +62812...} → {@code 0812...}). Needs no permission;
 * does nothing when Play services can't offer a number (no SIM, emulator, user declined before).
 *
 * <pre>
 * private final PhoneNumberHint phoneHint = new PhoneNumberHint(this);   // field: registers the launcher
 * ...
 * phoneHint.attach(binding.nomorHP);                                      // in onViewCreated
 * </pre>
 * The sheet is offered at most once per screen (kept across rotation); tapping the field again doesn't
 * re-open it, so typing a different number is never interrupted.
 */
public final class PhoneNumberHint {

    private static final String TAG = "PhoneNumberHint";
    private static final String STATE_KEY = "id.laukpauk.phoneNumberHint";

    private final Fragment fragment;
    private final ActivityResultLauncher<IntentSenderRequest> launcher;
    @Nullable private EditText field;
    private boolean offered;
    private boolean stateRegistered;

    /** Create it as a field initializer (or in onCreate) of the fragment. */
    public PhoneNumberHint(Fragment fragment) {
        this.fragment = fragment;
        this.launcher = fragment.registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                    if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null
                            || field == null || !fragment.isAdded()) {
                        return;
                    }
                    try {
                        String number = Identity.getSignInClient(fragment.requireActivity())
                                .getPhoneNumberFromIntent(result.getData());
                        String local = toLocal(number);
                        if (!local.isEmpty() && TextUtils.isEmpty(field.getText())) {
                            field.setText(local);
                            field.setSelection(local.length());
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "phone number hint result", e);
                    }
                });
    }

    /** Binds the hint to the phone field of the current view (call in onViewCreated). */
    public void attach(EditText phoneField) {
        this.field = phoneField;
        if (!stateRegistered) {
            stateRegistered = true;
            Bundle restored = fragment.getSavedStateRegistry().consumeRestoredStateForKey(STATE_KEY);
            if (restored != null) {
                offered = restored.getBoolean("offered");
            }
            fragment.getSavedStateRegistry().registerSavedStateProvider(STATE_KEY, () -> {
                Bundle b = new Bundle();
                b.putBoolean("offered", offered);
                return b;
            });
        }
        phoneField.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                offer();
            }
        });
        if (phoneField.hasFocus()) {
            offer();
        }
    }

    private void offer() {
        if (offered || field == null || !TextUtils.isEmpty(field.getText()) || !fragment.isAdded()) {
            return;
        }
        offered = true;
        Identity.getSignInClient(fragment.requireActivity())
                .getPhoneNumberHintIntent(GetPhoneNumberHintIntentRequest.builder().build())
                .addOnSuccessListener(pendingIntent -> {
                    if (fragment.isAdded()) {
                        launcher.launch(new IntentSenderRequest.Builder(pendingIntent).build());
                    }
                })
                .addOnFailureListener(e -> Log.i(TAG, "no phone number hint available: " + e.getMessage()));
    }

    /**
     * The picked number (E.164, e.g. {@code +6281234567890}) as the app's phone fields expect it: digits only,
     * Indonesian numbers with the leading 0 ({@code 081234567890}).
     */
    @VisibleForTesting
    public static String toLocal(@Nullable String number) {
        if (number == null) {
            return "";
        }
        boolean international = number.trim().startsWith("+");
        String digits = number.replaceAll("[^0-9]", "");
        if (digits.startsWith("62") && (international || digits.length() >= 11)) {
            return "0" + digits.substring(2);
        }
        return digits;
    }
}
