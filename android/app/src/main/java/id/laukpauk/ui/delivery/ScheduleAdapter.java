package id.laukpauk.ui.delivery;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import id.laukpauk.R;
import id.laukpauk.data.model.delivery.DeliverySchedule;
import id.laukpauk.databinding.ItemDeliveryScheduleBinding;

/** The schedule tiles of the "Jadwal Delivery" dialog (legacy JadwalPengantaran). */
public class ScheduleAdapter extends ListAdapter<DeliverySchedule, ScheduleAdapter.Holder> {

    public interface OnSelect {
        void onSelect(DeliverySchedule schedule);
    }

    private final OnSelect onSelect;
    private String selectedKey = "";

    public ScheduleAdapter(OnSelect onSelect) {
        super(new DiffUtil.ItemCallback<DeliverySchedule>() {
            @Override
            public boolean areItemsTheSame(@NonNull DeliverySchedule a, @NonNull DeliverySchedule b) {
                return a.readable().equals(b.readable());
            }

            @Override
            public boolean areContentsTheSame(@NonNull DeliverySchedule a, @NonNull DeliverySchedule b) {
                return a.raw.equals(b.raw);
            }
        });
        this.onSelect = onSelect;
    }

    public void setSelected(DeliverySchedule j) {
        selectedKey = j == null ? "" : j.readable();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemDeliveryScheduleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        DeliverySchedule j = getItem(position);
        ItemDeliveryScheduleBinding b = h.b;
        android.content.Context c = b.getRoot().getContext();
        boolean selected = selectedKey.equals(j.readable());
        boolean disabled = !j.isSelectable();
        int textColor = ContextCompat.getColor(c, disabled ? R.color.text_muted
                : selected ? R.color.nearly_white : R.color.green);
        b.card.setCardBackgroundColor(ContextCompat.getColor(c, disabled ? R.color.text_light_muted
                : selected ? R.color.green : R.color.nearly_white));
        b.card.setStrokeColor(ContextCompat.getColor(c, disabled ? R.color.text_light_muted : R.color.green));
        b.day.setText(j.day);
        b.session.setText(c.getString(R.string.delivery_delivery_schedule, j.session));
        b.hour.setText(c.getString(R.string.delivery_schedule_hour, j.timeRange));
        b.day.setTextColor(textColor);
        b.session.setTextColor(textColor);
        b.hour.setTextColor(textColor);
        b.full.setVisibility(j.isFull ? android.view.View.VISIBLE : android.view.View.GONE);
        b.open.setText(j.isOpened ? R.string.delivery_open : R.string.delivery_full);
        b.open.setTextColor(ContextCompat.getColor(c, j.isOpened ? R.color.text_success : R.color.text_danger));
        b.card.setOnClickListener(v -> {
            if (j.isSelectable()) {
                onSelect.onSelect(j);
            }
        });
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemDeliveryScheduleBinding b;

        Holder(ItemDeliveryScheduleBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
