package id.laukpauk.ui.location;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import id.laukpauk.data.model.delivery.PlaceSuggestion;
import id.laukpauk.databinding.ItemDeliverySuggestionBinding;

/** Address search suggestions (legacy CariGoogleMaps tiles). */
public class SuggestionAdapter extends ListAdapter<PlaceSuggestion, SuggestionAdapter.Holder> {

    public interface OnClick {
        void onClick(PlaceSuggestion s);
    }

    private final OnClick onClick;

    public SuggestionAdapter(OnClick onClick) {
        super(new DiffUtil.ItemCallback<PlaceSuggestion>() {
            @Override
            public boolean areItemsTheSame(@NonNull PlaceSuggestion a, @NonNull PlaceSuggestion b) {
                return a.name.equals(b.name) && a.formattedAddress.equals(b.formattedAddress);
            }

            @Override
            public boolean areContentsTheSame(@NonNull PlaceSuggestion a, @NonNull PlaceSuggestion b) {
                return a.lat == b.lat && a.lng == b.lng && areItemsTheSame(a, b);
            }
        });
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemDeliverySuggestionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        PlaceSuggestion s = getItem(position);
        h.b.name.setText(s.name);
        h.b.address.setText(s.formattedAddress);
        h.b.getRoot().setOnClickListener(v -> onClick.onClick(s));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemDeliverySuggestionBinding b;

        Holder(ItemDeliverySuggestionBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
