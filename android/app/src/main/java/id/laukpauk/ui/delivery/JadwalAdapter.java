package id.laukpauk.ui.delivery;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import id.laukpauk.R;
import id.laukpauk.data.model.delivery.JadwalDelivery;
import id.laukpauk.databinding.ItemDeliveryJadwalBinding;

/** The schedule tiles of the "Jadwal Delivery" dialog (legacy JadwalPengantaran). */
public class JadwalAdapter extends ListAdapter<JadwalDelivery, JadwalAdapter.Holder> {

    public interface OnSelect {
        void onSelect(JadwalDelivery jadwal);
    }

    private final OnSelect onSelect;
    private String selectedKey = "";

    public JadwalAdapter(OnSelect onSelect) {
        super(new DiffUtil.ItemCallback<JadwalDelivery>() {
            @Override
            public boolean areItemsTheSame(@NonNull JadwalDelivery a, @NonNull JadwalDelivery b) {
                return a.readable().equals(b.readable());
            }

            @Override
            public boolean areContentsTheSame(@NonNull JadwalDelivery a, @NonNull JadwalDelivery b) {
                return a.raw.equals(b.raw);
            }
        });
        this.onSelect = onSelect;
    }

    public void setSelected(JadwalDelivery j) {
        selectedKey = j == null ? "" : j.readable();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemDeliveryJadwalBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        JadwalDelivery j = getItem(position);
        ItemDeliveryJadwalBinding b = h.b;
        android.content.Context c = b.getRoot().getContext();
        boolean selected = selectedKey.equals(j.readable());
        boolean disabled = !j.isSelectable();
        int textColor = ContextCompat.getColor(c, disabled ? R.color.text_muted
                : selected ? R.color.nearly_white : R.color.green);
        b.card.setCardBackgroundColor(ContextCompat.getColor(c, disabled ? R.color.text_light_muted
                : selected ? R.color.green : R.color.nearly_white));
        b.card.setStrokeColor(ContextCompat.getColor(c, disabled ? R.color.text_light_muted : R.color.green));
        b.day.setText(j.day);
        b.sessi.setText(c.getString(R.string.delivery_jadwal_delivery, j.sessi));
        b.jam.setText(c.getString(R.string.delivery_jadwal_jam, j.rentangWaktu));
        b.day.setTextColor(textColor);
        b.sessi.setTextColor(textColor);
        b.jam.setTextColor(textColor);
        b.full.setVisibility(j.isFull ? android.view.View.VISIBLE : android.view.View.GONE);
        b.buka.setText(j.isOpened ? R.string.delivery_buka : R.string.delivery_full);
        b.buka.setTextColor(ContextCompat.getColor(c, j.isOpened ? R.color.text_success : R.color.text_danger));
        b.card.setOnClickListener(v -> {
            if (j.isSelectable()) {
                onSelect.onSelect(j);
            }
        });
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemDeliveryJadwalBinding b;

        Holder(ItemDeliveryJadwalBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
