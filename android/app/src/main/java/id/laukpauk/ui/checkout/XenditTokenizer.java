package id.laukpauk.ui.checkout;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.xendit.Models.Card;
import com.xendit.Models.Token;
import com.xendit.Models.XenditError;
import com.xendit.TokenCallback;
import com.xendit.Xendit;

import java.util.concurrent.atomic.AtomicBoolean;

import id.laukpauk.BuildConfig;
import id.laukpauk.data.model.payment.CardToken;

/**
 * Xendit single-use credit card token (3DS enabled, IDR). Port of the legacy XenditModel. Needs an Activity
 * because the SDK shows its 3DS authentication on it. Card data is never logged.
 */
final class XenditTokenizer {
    private XenditTokenizer() {}

    interface Callback {
        /** The token, or null when Xendit reported an error. Always called once, on the main thread. */
        void onResult(@Nullable CardToken token);
    }

    static void createToken(Activity activity, long totalTagihan, String cardNumber, String expiryMonth,
                            String expiryYear, final String cardCVN, final Callback callback) {
        final Handler main = new Handler(Looper.getMainLooper());
        final AtomicBoolean delivered = new AtomicBoolean(false);
        final Callback deliver = token -> {
            if (delivered.compareAndSet(false, true)) {
                main.post(() -> callback.onResult(token));
            }
        };
        try {
            Xendit xendit = Xendit.create(activity.getApplicationContext(), BuildConfig.XENDIT_PUBLIC_KEY, activity);
            Card card = new Card(cardNumber, expiryMonth, expiryYear, cardCVN);
            xendit.createSingleUseToken(card, Long.toString(totalTagihan), true, "IDR", new TokenCallback() {
                @Override
                public void onSuccess(Token token) {
                    if (token == null) {
                        deliver.onResult(null);
                        return;
                    }
                    CardToken t = new CardToken();
                    t.tokenID = token.getId();
                    t.authenticationID = token.getAuthenticationId();
                    t.cardCVN = cardCVN;
                    deliver.onResult(t);
                }

                @Override
                public void onError(XenditError xenditError) {
                    deliver.onResult(null);
                }
            });
        } catch (RuntimeException e) {
            deliver.onResult(null);
        }
    }
}
