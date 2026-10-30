package id.laukpauk.view.components.intropage;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.core.CustomColors;
import id.laukpauk.ui.Ui;

/** lib/views/components/IntroPage/IntroPageIndicator.dart: dots with an animated active one (250 ms). */
public class IntroPageIndicator extends LinearLayout {
    private final List<View> dots = new ArrayList<>();
    private final List<Float> sizes = new ArrayList<>();
    private int activePage;

    public IntroPageIndicator(Context ctx, int indicatorLength, int activePage) {
        super(ctx);
        this.activePage = activePage;
        setOrientation(HORIZONTAL);
        for (int i = 1; i <= indicatorLength; i++) {
            View dot = new View(ctx);
            float size = target(i);
            LayoutParams lp = new LayoutParams(Ui.dp(ctx, size), Ui.dp(ctx, size));
            lp.rightMargin = Ui.dp(ctx, 5);
            addView(dot, lp);
            dots.add(dot);
            sizes.add(size);
            dot.setBackground(Ui.shape(ctx, color(i), size));
        }
    }

    private float target(int page) {
        return activePage == page ? 15f : 10f;
    }

    private int color(int page) {
        return activePage == page ? CustomColors.green : CustomColors.nearlyWhite;
    }

    public void setActivePage(int page) {
        if (page == activePage) {
            return;
        }
        activePage = page;
        final Context ctx = getContext();
        for (int i = 0; i < dots.size(); i++) {
            final int index = i;
            final View dot = dots.get(i);
            final float from = sizes.get(i);
            final float to = target(i + 1);
            dot.setBackground(Ui.shape(ctx, color(i + 1), to));
            sizes.set(i, to);
            ValueAnimator anim = ValueAnimator.ofFloat(from, to);
            anim.setDuration(250);
            anim.addUpdateListener(a -> {
                float s = (Float) a.getAnimatedValue();
                LayoutParams lp = (LayoutParams) dot.getLayoutParams();
                lp.width = Ui.dp(ctx, s);
                lp.height = Ui.dp(ctx, s);
                dot.setLayoutParams(lp);
            });
            anim.start();
        }
    }
}
