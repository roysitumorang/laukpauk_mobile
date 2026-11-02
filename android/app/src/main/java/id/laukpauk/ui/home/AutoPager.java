package id.laukpauk.ui.home;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

/**
 * carousel_slider's autoPlay: a full-width pager that advances every 4 seconds and jumps back to the first page
 * after the last one. Runs only while the pager is attached to a window and not being dragged.
 */
public final class AutoPager {
    private static final long INTERVAL_MS = 4000L;

    private final ViewPager2 pager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable advance = new Runnable() {
        @Override
        public void run() {
            RecyclerView.Adapter<?> adapter = pager.getAdapter();
            int count = adapter == null ? 0 : adapter.getItemCount();
            if (count <= 1) {
                return;
            }
            int next = pager.getCurrentItem() + 1 >= count ? 0 : pager.getCurrentItem() + 1;
            pager.setCurrentItem(next, true);
            schedule();
        }
    };

    private AutoPager(ViewPager2 pager) {
        this.pager = pager;
    }

    public static void attach(ViewPager2 pager) {
        AutoPager auto = new AutoPager(pager);
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    auto.handler.removeCallbacks(auto.advance);
                } else if (state == ViewPager2.SCROLL_STATE_IDLE && pager.isAttachedToWindow()) {
                    auto.schedule();
                }
            }
        });
        pager.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                auto.schedule();
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                auto.handler.removeCallbacks(auto.advance);
            }
        });
        if (pager.isAttachedToWindow()) {
            auto.schedule();
        }
    }

    private void schedule() {
        handler.removeCallbacks(advance);
        handler.postDelayed(advance, INTERVAL_MS);
    }
}
