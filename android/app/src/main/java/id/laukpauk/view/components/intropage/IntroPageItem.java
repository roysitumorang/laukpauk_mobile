package id.laukpauk.view.components.intropage;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.gson.JsonObject;

import id.laukpauk.core.ClientPath;
import id.laukpauk.core.Json;
import id.laukpauk.core.Prefs;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.SplashScreenActivity;

/**
 * lib/views/components/IntroPage/IntroPageItem.dart. Dart: IntroPageItem(context, indexPage, dataPage); the
 * BuildContext is the Context here. dataPage['img'] is ClientPath.assetsImg + '/intro-2.jpg'.
 */
public class IntroPageItem extends FrameLayout {
    public IntroPageItem(final Context ctx, int indexPage, JsonObject dataPage) {
        super(ctx);
        float hb = Ui.hb(ctx);

        String img = Json.str(dataPage, "img");
        String prefix = ClientPath.assetsImg + "/";
        if (img.startsWith(prefix)) {
            img = img.substring(prefix.length());
        }
        ImageView background = new ImageView(ctx);
        background.setScaleType(ImageView.ScaleType.FIT_XY);
        Ui.setAsset(background, img);
        addView(background, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        if (indexPage == 1) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            lp.bottomMargin = Ui.dp(ctx, hb * 22.5f);
            FrameLayout holder = new FrameLayout(ctx);
            holder.addView(CustomButton.button(ctx, "Ke Pasar Sekarang", this::startApp,
                    new CustomButton.Opts().outline().pill().color(CustomButton.themeColor)),
                    new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.CENTER));
            addView(holder, lp);
        }
    }

    private void startApp() {
        Prefs.setBool("isAppStarted", true);
        Nav.push(getContext(), SplashScreenActivity.class, null);
    }
}
