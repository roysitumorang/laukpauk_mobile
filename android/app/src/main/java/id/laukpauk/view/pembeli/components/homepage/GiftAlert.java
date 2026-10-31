package id.laukpauk.view.pembeli.components.homepage;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import id.laukpauk.R;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Prefs;
import id.laukpauk.core.ServerPath;
import id.laukpauk.element.Gift;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Ui;

/**
 * Port of lib/views/pembeli/components/HomePage/GiftAlert.dart: a PageView of gift pictures, each
 * with a close button, and a dot indicator below.
 */
public class GiftAlert extends LinearLayout {
    private final List<Gift> gifts;
    private final Runnable onClose;
    private int activeGift = 1;
    private LinearLayout indicator;

    public GiftAlert(Context ctx, List<Gift> gifts, Runnable onClose) {
        super(ctx);
        this.gifts = gifts;
        this.onClose = onClose;
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        build();
    }

    /**
     * HeaderModel._showGift: showDialog(GiftAlert(gifts, onClose: sP.setBool('showAlertGift', false);
     * onClose())). The dialog is closed by the X button (Dart's onClose pops the dialog route).
     */
    public static void show(final BaseActivity a, final List<Gift> gifts, final Runnable onClose) {
        if (gifts == null || gifts.size() < 1) {
            return;
        }
        Modal.<Object>dialog(a, true, handle -> new GiftAlert(a, gifts, () -> {
            Prefs.setBool("showAlertGift", false);
            handle.close();
            if (onClose != null) {
                onClose.run();
            }
        }), null);
    }

    private void build() {
        final Context ctx = getContext();
        final float hb = Ui.hb(ctx);
        final float fullWidth = hb * 100.0f;
        float height = Ui.vb(ctx) * 50.0f;

        ViewPager2 pager = new ViewPager2(ctx);
        pager.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                FrameLayout holder = new FrameLayout(parent.getContext());
                holder.setLayoutParams(new ViewGroup.LayoutParams(Ui.MATCH, Ui.MATCH));
                return new RecyclerView.ViewHolder(holder) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
                FrameLayout holder = (FrameLayout) h.itemView;
                holder.removeAllViews();
                holder.addView(page(gifts.get(position), hb), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            }

            @Override
            public int getItemCount() {
                return gifts.size();
            }
        });
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                activeGift = position + 1;
                renderIndicator();
            }
        });
        addView(pager, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, height)));

        addView(Ui.spaceH(ctx, 25));

        indicator = new LinearLayout(ctx);
        indicator.setOrientation(HORIZONTAL);
        addView(indicator, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        renderIndicator();
    }

    private View page(Gift gift, float hb) {
        Context ctx = getContext();
        String giftFoto = gift.foto != null ? gift.foto : "gift-default.png";
        String src = ServerPath.baseURL + "/upload/gambar/gift/" + giftFoto;

        FrameLayout page = new FrameLayout(ctx);
        ImageView img = new ImageView(ctx);
        img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        Ui.loadNetwork(img, src);
        page.addView(img, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        FrameLayout close = new FrameLayout(ctx);
        Ui.background(close, CustomColors.white, 25);
        close.setElevation(Ui.dp(ctx, 12));
        close.addView(Ui.icon(ctx, R.drawable.ic_close, 24, CustomColors.black),
                new FrameLayout.LayoutParams(Ui.dp(ctx, 24), Ui.dp(ctx, 24), Gravity.CENTER));
        close.setPadding(Ui.dp(ctx, 0), 0, 0, 0);
        Ui.onTap(close, () -> onClose.run());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.dp(ctx, 24), Ui.dp(ctx, 24),
                Gravity.TOP | Gravity.END);
        lp.topMargin = Ui.dp(ctx, 10);
        lp.rightMargin = Ui.dp(ctx, hb * 20.0f);
        page.addView(close, lp);
        return page;
    }

    /** IntroPageIndicator(activePage, indicatorLength): white dots, the active one green and larger. */
    private void renderIndicator() {
        Context ctx = getContext();
        indicator.removeAllViews();
        for (int i = 1; i <= gifts.size(); i++) {
            float size = 10.0f;
            int color = CustomColors.nearlyWhite;
            if (activeGift == i) {
                size = 15.0f;
                color = CustomColors.green;
            }
            View dot = new View(ctx);
            dot.setBackground(Ui.circle(color));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(ctx, size), Ui.dp(ctx, size));
            lp.rightMargin = Ui.dp(ctx, 5);
            indicator.addView(dot, lp);
        }
    }
}
