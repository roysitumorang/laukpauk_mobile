package id.laukpauk.view.pembeli.components.homepage;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import id.laukpauk.ui.Ui;

/**
 * carousel_slider's CarouselSlider.builder(autoPlay: true, viewportFraction: 1,
 * enableInfiniteScroll: false): a full-width pager that advances every 4 seconds (800 ms slide) and
 * jumps back to the first page after the last one. The timer only runs while the view is attached.
 */
public class AutoPager extends FrameLayout {
    public interface PageFactory {
        View create(int index);
    }

    private static final long INTERVAL_MS = 4000L;

    private final ViewPager2 pager;
    private final int count;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable advance = new Runnable() {
        @Override
        public void run() {
            if (count <= 1) {
                return;
            }
            int current = pager.getCurrentItem();
            int next = (current + 1 >= count) ? 0 : current + 1;
            pager.setCurrentItem(next, true);
            schedule();
        }
    };

    public AutoPager(Context ctx, int count, final PageFactory factory) {
        super(ctx);
        this.count = count;
        pager = new ViewPager2(ctx);
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
                holder.addView(factory.create(position), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            }

            @Override
            public int getItemCount() {
                return AutoPager.this.count;
            }
        });
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    handler.removeCallbacks(advance);
                } else if (state == ViewPager2.SCROLL_STATE_IDLE && isAttachedToWindow()) {
                    schedule();
                }
            }
        });
        addView(pager, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }

    private void schedule() {
        handler.removeCallbacks(advance);
        if (count > 1) {
            handler.postDelayed(advance, INTERVAL_MS);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        schedule();
    }

    @Override
    protected void onDetachedFromWindow() {
        handler.removeCallbacks(advance);
        super.onDetachedFromWindow();
    }
}
