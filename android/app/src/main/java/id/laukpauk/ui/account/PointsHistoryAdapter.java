package id.laukpauk.ui.account;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import id.laukpauk.R;
import id.laukpauk.data.model.account.PointRecords;
import id.laukpauk.databinding.ItemAccountPointsHistoryBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;

/** One row of the points history. */
public class PointsHistoryAdapter extends ListAdapter<PointRecords, PointsHistoryAdapter.Holder> {

    public PointsHistoryAdapter() {
        super(new DiffUtil.ItemCallback<PointRecords>() {
            @Override
            public boolean areItemsTheSame(@NonNull PointRecords a, @NonNull PointRecords b) {
                return a == b;
            }

            @Override
            public boolean areContentsTheSame(@NonNull PointRecords a, @NonNull PointRecords b) {
                return a.value == b.value && safe(a.remarks).equals(safe(b.remarks))
                        && safe(a.createdFrom).equals(safe(b.createdFrom));
            }
        });
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemAccountPointsHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        PointRecords item = getItem(position);
        ItemAccountPointsHistoryBinding b = holder.binding;
        Images.asset(b.icon, "poin.png");
        String remarks = safe(item.remarks);
        if (PointRecords.CREATED_FROM_TRANSACTION.equals(item.createdFrom)) {
            remarks = "Transaksi " + remarks;
        }
        b.value.setText(Currency.formatRupiah(item.value, 0));
        b.value.setTextColor(ContextCompat.getColor(b.getRoot().getContext(),
                item.value >= 0 ? R.color.text_success : R.color.text_danger));
        b.remarks.setText(remarks);
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemAccountPointsHistoryBinding binding;

        Holder(ItemAccountPointsHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
