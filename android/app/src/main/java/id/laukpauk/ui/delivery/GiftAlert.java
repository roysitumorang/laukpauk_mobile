package id.laukpauk.ui.delivery;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.model.delivery.Gift;
import id.laukpauk.databinding.DialogDeliveryGiftBinding;
import id.laukpauk.databinding.ItemDeliveryGiftBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** Legacy GiftAlert: a pager of gift pictures, each with a close button, and dots below. */
public final class GiftAlert {
    private GiftAlert() {}

    /** Shows the dialog; {@code onClose} runs after the X button dismissed it. */
    public static void show(Fragment fragment, List<Gift> gifts, Runnable onClose) {
        if (!fragment.isAdded() || gifts == null || gifts.isEmpty()) {
            return;
        }
        android.content.Context ctx = fragment.requireContext();
        DialogDeliveryGiftBinding b = DialogDeliveryGiftBinding.inflate(LayoutInflater.from(ctx));
        Dialog dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(b.getRoot());
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        ViewGroup.LayoutParams lp = b.pager.getLayoutParams();
        lp.height = ctx.getResources().getDisplayMetrics().heightPixels / 2;
        b.pager.setLayoutParams(lp);

        Runnable close = () -> {
            dialog.dismiss();
            if (onClose != null) {
                onClose.run();
            }
        };
        b.pager.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ItemDeliveryGiftBinding ib = ItemDeliveryGiftBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);
                return new RecyclerView.ViewHolder(ib.getRoot()) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
                ItemDeliveryGiftBinding ib = ItemDeliveryGiftBinding.bind(h.itemView);
                String foto = gifts.get(position).foto != null ? gifts.get(position).foto : "gift-default.png";
                Images.url(ib.image, Images.server(ServerPath.uploadGambarGift, foto));
                ib.close.setOnClickListener(v -> close.run());
            }

            @Override
            public int getItemCount() {
                return gifts.size();
            }
        });
        renderDots(b.indicator, gifts.size(), 0);
        b.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                renderDots(b.indicator, gifts.size(), position);
            }
        });
        dialog.show();
    }

    /** IntroPageIndicator: white dots, the active one green and larger. */
    private static void renderDots(LinearLayout holder, int count, int active) {
        android.content.Context ctx = holder.getContext();
        float d = ctx.getResources().getDisplayMetrics().density;
        holder.removeAllViews();
        for (int i = 0; i < count; i++) {
            boolean on = i == active;
            int size = Math.round((on ? 15 : 10) * d);
            View dot = new View(ctx);
            GradientDrawable g = new GradientDrawable();
            g.setShape(GradientDrawable.OVAL);
            g.setColor(ContextCompat.getColor(ctx, on ? R.color.green : R.color.nearly_white));
            dot.setBackground(g);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMarginEnd(Math.round(5 * d));
            holder.addView(dot, lp);
        }
    }
}
