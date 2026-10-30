package id.laukpauk.view.pembeli.components.homepage;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import id.laukpauk.ui.Ui;

/**
 * Flutter's Wrap(direction: horizontal): children flow left to right and wrap to the next run.
 * `center` = WrapAlignment.center (each run centred), otherwise WrapAlignment.start.
 */
public class FlowLayout extends ViewGroup {
    private final int hSpacing;
    private final int vSpacing;
    private final boolean center;

    public FlowLayout(Context ctx, float spacingDp, float runSpacingDp, boolean center) {
        super(ctx);
        this.hSpacing = Ui.dp(ctx, spacingDp);
        this.vSpacing = Ui.dp(ctx, runSpacingDp);
        this.center = center;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        boolean bounded = MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED;
        int x = 0;
        int y = 0;
        int rowHeight = 0;
        int usedWidth = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View c = getChildAt(i);
            if (c.getVisibility() == GONE) {
                continue;
            }
            measureChild(c, widthMeasureSpec, heightMeasureSpec);
            int w = c.getMeasuredWidth();
            int h = c.getMeasuredHeight();
            if (bounded && x > 0 && x + w > maxWidth) {
                x = 0;
                y += rowHeight + vSpacing;
                rowHeight = 0;
            }
            x += w + hSpacing;
            usedWidth = Math.max(usedWidth, x - hSpacing);
            rowHeight = Math.max(rowHeight, h);
        }
        int height = y + rowHeight;
        int width = bounded && MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY ? maxWidth : usedWidth;
        setMeasuredDimension(width, resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int maxWidth = r - l;
        int y = 0;
        int i = 0;
        int n = getChildCount();
        while (i < n) {
            // collect one run
            int start = i;
            int x = 0;
            int rowHeight = 0;
            int runWidth = 0;
            while (i < n) {
                View c = getChildAt(i);
                if (c.getVisibility() == GONE) {
                    i++;
                    continue;
                }
                int w = c.getMeasuredWidth();
                if (x > 0 && x + w > maxWidth) {
                    break;
                }
                x += w + hSpacing;
                runWidth = x - hSpacing;
                rowHeight = Math.max(rowHeight, c.getMeasuredHeight());
                i++;
            }
            int cx = center ? Math.max(0, (maxWidth - runWidth) / 2) : 0;
            for (int j = start; j < i; j++) {
                View c = getChildAt(j);
                if (c.getVisibility() == GONE) {
                    continue;
                }
                c.layout(cx, y, cx + c.getMeasuredWidth(), y + c.getMeasuredHeight());
                cx += c.getMeasuredWidth() + hSpacing;
            }
            y += rowHeight + vSpacing;
            if (i == start) {
                i++; // safety: never loop forever
            }
        }
    }
}
