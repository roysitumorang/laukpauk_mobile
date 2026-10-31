package id.laukpauk.view.pembeli;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import id.laukpauk.core.AuthModel;
import id.laukpauk.core.CustomColors;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.model.ShopModelUi;

/**
 * Small helpers shared by the account / wallet / points / notification / profile / seller-request
 * screens: the "FutureBuilder(AuthModel.isLogin) ... Login(routeTo: X)" gate, the Material Card,
 * the Material AlertDialog with a single text button and file to base64.
 */
public final class AccountUi {
    private AccountUi() {}

    /**
     * Dart: FutureBuilder(future: AuthModel.isLogin(), ...). While the check runs a loader is shown;
     * when logged in `onLogin` runs (build the screen), otherwise the screen is replaced by
     * LoginActivity(routeTo: self(routeArgs)).
     *
     * @param plainSpinner true = the 25dp spinner (Dart CircularProgressIndicator), false = Loading()
     */
    public static void requireLogin(final BaseActivity a, final Class<? extends Activity> self,
                                    final JsonObject routeArgs, boolean plainSpinner,
                                    final Runnable onLogin) {
        a.setRoot(plainSpinner ? Loading.spinner(a) : Loading.view(a));
        a.async(AuthModel::isLogin, isLogin -> {
            if (isLogin != null && isLogin) {
                onLogin.run();
            } else {
                Nav.pushReplacement(a, LoginActivity.class,
                        AuthRoute.carry(self.getName(), routeArgs));
            }
        });
    }

    /**
     * Dart: Card(elevation: 5, shape: RoundedRectangleBorder(borderRadius: 10)) with the default
     * 4dp margin. The card is added to `parent` (a vertical LinearLayout) and returned.
     */
    public static LinearLayout card(Context ctx, LinearLayout parent) {
        LinearLayout card = Layouts.card(ctx, CustomColors.nearlyWhite, 10f, 5f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
        int m = Ui.dp(ctx, 4);
        lp.setMargins(m, m, m, m);
        parent.addView(card, lp);
        return card;
    }

    /** A weight-1 filler (MainAxisAlignment.spaceBetween). */
    public static View filler(Context ctx) {
        View v = new View(ctx);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        return v;
    }

    /**
     * Dart: showDialog(builder: (context) => AlertDialog(title: Text(title, style: fontSize titleSp),
     * content: Text(content, style: fontSize contentSp), actions: [TextButton(onPressed: ..., child: Text(label))])).
     * `onPressed` runs after the dialog is closed (null = just close).
     */
    public static void alert(final BaseActivity a, final String title, final String content,
                             final float titleSp, final float contentSp, final String buttonLabel,
                             final Runnable onPressed) {
        Modal.<Object>dialog(a, true, handle -> {
            View button = CustomAlert.textButton(a, buttonLabel, () -> {
                handle.close();
                if (onPressed != null) {
                    onPressed.run();
                }
            });
            LinearLayout actions = ShopModelUi.alertActions(a, button);
            actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            return ShopModelUi.alertDialogCard(a, title, content, titleSp, contentSp, actions);
        }, null);
    }

    /** Dart: base64Encode(await file.readAsBytes()). */
    public static String fileToBase64(File file) {
        try (FileInputStream in = new FileInputStream(file)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            return android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.NO_WRAP);
        } catch (IOException e) {
            return "";
        }
    }
}
