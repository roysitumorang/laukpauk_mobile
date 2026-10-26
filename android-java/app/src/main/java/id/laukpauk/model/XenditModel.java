package id.laukpauk.model;

import android.app.Activity;

import com.google.gson.JsonObject;
import com.xendit.Models.Card;
import com.xendit.Models.Token;
import com.xendit.Models.XenditError;
import com.xendit.TokenCallback;
import com.xendit.Xendit;

import java.util.concurrent.atomic.AtomicBoolean;

import id.laukpauk.BuildConfig;
import id.laukpauk.core.Async;
import id.laukpauk.core.Json;

/**
 * Port of the native credit-card tokenization that the Flutter app reached through
 * MethodChannel('laukpauk.xendit') -> 'createCreditCartToken' (android/.../MainActivity.java,
 * TokenizationArgument.java). The Dart call site was CreditCardData.dart.
 */
public final class XenditModel {
    private XenditModel() {}

    /**
     * Creates a single-use Xendit token (3DS authentication enabled, currency IDR).
     *
     * @param activity the hosting screen (the Xendit SDK shows its 3DS authentication on it)
     * @param argument keys of the native TokenizationArgument class: totalTagihan (number),
     *                 cardNumber, expiryMonth, expiryYear, cardCVN (strings)
     * @param done     receives {"tokenID", "authenticationID", "cardCVN"}, or null when Xendit
     *                 reports an error (the Dart side printed 'gagal membuat token!').
     *                 Always invoked exactly once, on the main thread.
     *                 Dart then used jsonEncode of this map as moreDataMetodePembayaran, i.e.
     *                 {@code result.toString()}.
     */
    public static void createCreditCardToken(final Activity activity, final JsonObject argument,
                                             final Async.Done<JsonObject> done) {
        if (!Async.isMainThread()) {
            Async.main(new Runnable() {
                @Override
                public void run() {
                    createCreditCardToken(activity, argument, done);
                }
            });
            return;
        }

        final AtomicBoolean delivered = new AtomicBoolean(false);
        final Async.Done<JsonObject> deliver = new Async.Done<JsonObject>() {
            @Override
            public void run(final JsonObject result) {
                if (!delivered.compareAndSet(false, true)) {
                    return;
                }
                if (done == null) {
                    return;
                }
                if (Async.isMainThread()) {
                    done.run(result);
                } else {
                    Async.main(new Runnable() {
                        @Override
                        public void run() {
                            done.run(result);
                        }
                    });
                }
            }
        };

        if (activity == null || argument == null) {
            deliver.run(null);
            return;
        }

        try {
            final int totalTagihan = Json.intVal(argument, "totalTagihan");
            final String cardNumber = Json.str(argument, "cardNumber");
            final String expiryMonth = Json.str(argument, "expiryMonth");
            final String expiryYear = Json.str(argument, "expiryYear");
            final String cardCVN = Json.str(argument, "cardCVN");

            Xendit xendit = new Xendit(activity.getApplicationContext(), BuildConfig.XENDIT_PUBLIC_KEY, activity);

            String totalTagihanString = Integer.toString(totalTagihan);

            Card card = new Card(cardNumber, expiryMonth, expiryYear, cardCVN);
            TokenCallback tokenCallback = new TokenCallback() {
                @Override
                public void onSuccess(Token token) {
                    if (token != null) {
                        JsonObject returnedData = new JsonObject();
                        returnedData.addProperty("tokenID", token.getId());
                        returnedData.addProperty("authenticationID", token.getAuthenticationId());
                        returnedData.addProperty("cardCVN", cardCVN);
                        deliver.run(returnedData);
                    } else {
                        // The native code never answered in this case; answer "no token" instead of hanging.
                        deliver.run(null);
                    }
                }

                @Override
                public void onError(XenditError xenditError) {
                    deliver.run(null);
                }
            };

            xendit.createSingleUseToken(card, totalTagihanString, true, "IDR", tokenCallback);
        } catch (RuntimeException e) {
            deliver.run(null);
        }
    }
}
