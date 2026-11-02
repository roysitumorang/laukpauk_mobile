package id.laukpauk.ui.orders;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import id.laukpauk.R;
import id.laukpauk.databinding.ViewOrdersStatusStepBinding;

/** StatusTransaksi.dart: the 4-step delivery indicator (Diterima, Proses, Kirim, Selesai). */
public class StatusTransaksiView extends LinearLayout {

    private static final int[] ICONS = {R.drawable.ic_fa_check_circle_regular, R.drawable.ic_fa_box_open,
            R.drawable.ic_fa_shipping_fast, R.drawable.ic_fa_people_carry};
    private static final int[] LABELS = {R.string.orders_step_diterima, R.string.orders_step_proses,
            R.string.orders_step_kirim, R.string.orders_step_selesai};

    public StatusTransaksiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(android.view.Gravity.CENTER_VERTICAL);
    }

    private int px(float dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics()));
    }

    /**
     * @param iconWidthDp diameter of each round step
     * @param iconSizeDp  size of the glyph inside it
     * @param showLabels  caption below each step
     */
    public void setup(@Nullable String status, float iconWidthDp, float iconSizeDp, boolean showLabels) {
        removeAllViews();
        String lc = status == null ? "" : status.toLowerCase();
        int reached = 0;
        if (lc.equals("approved")) {
            reached = 1;
        }
        if (lc.equals("processing")) {
            reached = 2;
        }
        if (lc.equals("otw")) {
            reached = 3;
        }
        if (lc.equals("received")) {
            reached = 4;
        }
        int green = ContextCompat.getColor(getContext(), R.color.green);
        int muted = ContextCompat.getColor(getContext(), R.color.text_muted);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < ICONS.length; i++) {
            ViewOrdersStatusStepBinding step = ViewOrdersStatusStepBinding.inflate(inflater, this, false);
            int index = i + 1;
            step.circle.setBackgroundTintList(ColorStateList.valueOf(index <= reached ? green : muted));
            LinearLayout.LayoutParams clp = (LinearLayout.LayoutParams) step.circle.getLayoutParams();
            clp.width = px(iconWidthDp);
            clp.height = px(iconWidthDp);
            step.circle.setLayoutParams(clp);
            step.icon.setImageResource(ICONS[i]);
            android.widget.FrameLayout.LayoutParams ilp =
                    (android.widget.FrameLayout.LayoutParams) step.icon.getLayoutParams();
            ilp.width = px(iconSizeDp);
            ilp.height = px(iconSizeDp);
            step.icon.setLayoutParams(ilp);
            if (index < ICONS.length) {
                step.line.setBackgroundColor(index + 1 <= reached ? green : muted);
                LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) step.line.getLayoutParams();
                llp.width = px(iconWidthDp / 3f);
                step.line.setLayoutParams(llp);
            } else {
                step.line.setVisibility(View.GONE);
            }
            step.label.setText(LABELS[i]);
            step.label.setVisibility(showLabels ? View.VISIBLE : View.GONE);
            addView(step.getRoot());
        }
    }
}
