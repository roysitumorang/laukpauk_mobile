package id.laukpauk.ui.account;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.model.account.WalletRecords;
import id.laukpauk.databinding.ItemAccountWalletHistoryBinding;
import id.laukpauk.util.Currency;

/** One row of the wallet history (HistoriDompetItem.dart). */
public class WalletHistoryAdapter extends ListAdapter<WalletRecords, WalletHistoryAdapter.Holder> {

    public WalletHistoryAdapter() {
        super(new DiffUtil.ItemCallback<WalletRecords>() {
            @Override
            public boolean areItemsTheSame(@NonNull WalletRecords a, @NonNull WalletRecords b) {
                return a == b;
            }

            @Override
            public boolean areContentsTheSame(@NonNull WalletRecords a, @NonNull WalletRecords b) {
                return a.value == b.value && safe(a.type).equals(safe(b.type))
                        && safe(a.remarks).equals(safe(b.remarks)) && safe(a.createdAt).equals(safe(b.createdAt));
            }
        });
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemAccountWalletHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        WalletRecords item = getItem(position);
        ItemAccountWalletHistoryBinding b = holder.binding;
        int black = ContextCompat.getColor(b.getRoot().getContext(), R.color.account_text);
        int muted = ContextCompat.getColor(b.getRoot().getContext(), R.color.text_muted);
        b.number.setText((position + 1) + ".");
        if (safe(item.type).length() >= 1) {
            b.type.setText(item.type.toUpperCase(Locale.ROOT));
            b.type.setTextColor(black);
            b.type.setTypeface(null, Typeface.BOLD);
        } else {
            b.type.setText(R.string.account_wallet_without_type);
            b.type.setTextColor(muted);
            b.type.setTypeface(null, Typeface.BOLD_ITALIC);
        }
        if (safe(item.remarks).length() >= 1) {
            b.remarks.setText(item.remarks);
            b.remarks.setTextColor(black);
            b.remarks.setTypeface(null, Typeface.NORMAL);
        } else {
            b.remarks.setText(R.string.account_wallet_without_description);
            b.remarks.setTextColor(muted);
            b.remarks.setTypeface(null, Typeface.BOLD_ITALIC);
        }
        b.date.setText("Pada " + safe(item.createdAt));
        b.value.setText(Currency.formatRupiah(item.value, 0));
        b.value.setTextColor(ContextCompat.getColor(b.getRoot().getContext(),
                item.value >= 0 ? R.color.green : R.color.text_danger));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemAccountWalletHistoryBinding binding;

        Holder(ItemAccountWalletHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
