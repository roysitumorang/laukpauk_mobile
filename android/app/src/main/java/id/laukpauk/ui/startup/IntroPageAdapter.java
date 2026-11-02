package id.laukpauk.ui.startup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import id.laukpauk.databinding.ItemStartupIntroBinding;
import id.laukpauk.ui.common.Images;

/** IntroPageItem.dart: one full-screen image per page, the last page has the start button. */
class IntroPageAdapter extends RecyclerView.Adapter<IntroPageAdapter.Holder> {

    static final String[] IMAGES = {"intro-2.jpg", "intro-3.jpg"};

    private final Runnable onStart;

    IntroPageAdapter(Runnable onStart) {
        this.onStart = onStart;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemStartupIntroBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Images.asset(holder.binding.background, IMAGES[position]);
        // legacy: the button is on indexPage == 1 (the second page)
        holder.binding.start.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        holder.binding.start.setOnClickListener(v -> onStart.run());
    }

    @Override
    public int getItemCount() {
        return IMAGES.length;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemStartupIntroBinding binding;

        Holder(ItemStartupIntroBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
