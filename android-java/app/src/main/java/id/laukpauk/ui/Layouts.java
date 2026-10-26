package id.laukpauk.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.List;

/**
 * More Flutter-widget equivalents that do not belong in {@link Ui}:
 * MediaQuery padding.top, GridView.count (fixed cell aspect ratio), InkWell ripple, elevation,
 * horizontal ListView.
 */
public final class Layouts {
    private Layouts() {}

    // ------------------------------------------------------------------ status bar

    public static int statusBarHeightPx(Context ctx) {
        int id = ctx.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? ctx.getResources().getDimensionPixelSize(id) : Ui.dp(ctx, 24);
    }

    /**
     * MediaQuery.of(context).padding.top: a spacer as tall as the status bar. Put it at the top of
     * the screen (or give the green header this much top padding) because BaseActivity draws edge to edge.
     */
    public static View statusBarSpace(Context ctx) {
        View v = new View(ctx);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, statusBarHeightPx(ctx)));
        return v;
    }

    /** Adds the status-bar height to the view's top padding (Container(padding: top: padding.top)). */
    public static void padTopForStatusBar(View v) {
        v.setPadding(v.getPaddingLeft(), v.getPaddingTop() + statusBarHeightPx(v.getContext()),
                v.getPaddingRight(), v.getPaddingBottom());
    }

    // ------------------------------------------------------------------ aspect ratio / grid

    /** A FrameLayout whose height is width / ratio (Flutter AspectRatio, GridView childAspectRatio). */
    public static class AspectFrame extends FrameLayout {
        private final float ratio; // width / height

        public AspectFrame(Context ctx, float widthOverHeight) {
            super(ctx);
            this.ratio = widthOverHeight <= 0 ? 1f : widthOverHeight;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int w = MeasureSpec.getSize(widthMeasureSpec);
            int h = Math.round(w / ratio);
            super.onMeasure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
        }
    }

    /** AspectRatio(aspectRatio: widthOverHeight, child: child). */
    public static AspectFrame aspect(Context ctx, View child, float widthOverHeight) {
        AspectFrame f = new AspectFrame(ctx, widthOverHeight);
        f.addView(child, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        return f;
    }

    /**
     * GridView.count(crossAxisCount: columns, crossAxisSpacing, mainAxisSpacing, childAspectRatio)
     * placed inside a scroll view of the caller's choice (the grid itself does not scroll and
     * wraps its content). Every cell has the same width and a height of width / cellAspectRatio.
     * Pass cellAspectRatio <= 0 to let each cell wrap its own content height.
     */
    public static LinearLayout grid(Context ctx, List<? extends View> items, int columns,
                                    float crossSpacingDp, float mainSpacingDp, float cellAspectRatio) {
        LinearLayout column = Ui.column(ctx);
        int cols = Math.max(1, columns);
        for (int start = 0; start < items.size(); start += cols) {
            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.TOP);
            for (int c = 0; c < cols; c++) {
                int index = start + c;
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.WRAP, 1f);
                if (c > 0) {
                    lp.leftMargin = Ui.dp(ctx, crossSpacingDp);
                }
                if (index < items.size()) {
                    View item = items.get(index);
                    if (item.getParent() instanceof ViewGroup) {
                        ((ViewGroup) item.getParent()).removeView(item);
                    }
                    View cell = cellAspectRatio > 0 ? aspect(ctx, item, cellAspectRatio) : item;
                    row.addView(cell, lp);
                } else {
                    row.addView(new View(ctx), lp); // keep the last row's cells the same width
                }
            }
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
            if (start > 0) {
                rowLp.topMargin = Ui.dp(ctx, mainSpacingDp);
            }
            column.addView(row, rowLp);
        }
        return column;
    }

    // ------------------------------------------------------------------ scrolling

    /** A horizontally scrolling Row (ListView(scrollDirection: Axis.horizontal)). */
    public static HorizontalScrollView hscroll(Context ctx, View child) {
        HorizontalScrollView s = new HorizontalScrollView(ctx);
        s.setHorizontalScrollBarEnabled(false);
        s.addView(child, new ViewGroup.LayoutParams(Ui.WRAP, Ui.WRAP));
        return s;
    }

    // ------------------------------------------------------------------ feedback / depth

    /** InkWell ripple on top of the view's current background (call after setting the background). */
    public static void ripple(View v) {
        ripple(v, 0x33000000, 0f);
    }

    public static void ripple(View v, int rippleColor, float cornerRadiusDp) {
        Context ctx = v.getContext();
        Drawable content = v.getBackground();
        GradientDrawable mask = new GradientDrawable();
        mask.setColor(Color.WHITE);
        mask.setCornerRadius(Ui.dp(ctx, cornerRadiusDp));
        v.setBackground(new RippleDrawable(ColorStateList.valueOf(rippleColor), content, mask));
        v.setClickable(true);
    }

    /** Material elevation (Card/Material elevation: n). */
    public static void elevation(View v, float dp) {
        v.setElevation(Ui.dp(v.getContext(), dp));
    }

    /** A rounded, white, optionally elevated card (Card / Container with BoxShadow). */
    public static LinearLayout card(Context ctx, int fill, float radiusDp, float elevationDp) {
        LinearLayout l = Ui.column(ctx);
        l.setBackground(Ui.shape(ctx, fill, radiusDp));
        l.setElevation(Ui.dp(ctx, elevationDp));
        return l;
    }

    /** Stroke-only rounded border (BoxDecoration(border: Border.all(...))). */
    public static GradientDrawable border(Context ctx, int strokeColor, float strokeWidthDp, float radiusDp) {
        return Ui.shape(ctx, Color.TRANSPARENT, radiusDp, strokeColor, strokeWidthDp);
    }

    /** Positions `child` inside a FrameLayout at an absolute top/right offset (Positioned(top, right)). */
    public static void positionTopRight(FrameLayout parent, View child, float topDp, float rightDp) {
        Context ctx = parent.getContext();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP,
                Gravity.TOP | Gravity.END);
        lp.topMargin = Ui.dp(ctx, topDp);
        lp.rightMargin = Ui.dp(ctx, rightDp);
        parent.addView(child, lp);
    }
}
