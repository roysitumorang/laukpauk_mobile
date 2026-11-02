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
import id.laukpauk.data.model.account.RiwayatDompet;
import id.laukpauk.databinding.ItemAccountHistoriDompetBinding;
import id.laukpauk.util.Currency;

/** One row of the wallet history (HistoriDompetItem.dart). */
public class HistoriDompetAdapter extends ListAdapter<RiwayatDompet, HistoriDompetAdapter.Holder> {

    public HistoriDompetAdapter() {
        super(new DiffUtil.ItemCallback<RiwayatDompet>() {
            @Override
            public boolean areItemsTheSame(@NonNull RiwayatDompet a, @NonNull RiwayatDompet b) {
                return a == b;
            }

            @Override
            public boolean areContentsTheSame(@NonNull RiwayatDompet a, @NonNull RiwayatDompet b) {
                return a.nilai == b.nilai && safe(a.jenis).equals(safe(b.jenis))
                        && safe(a.keterangan).equals(safe(b.keterangan)) && safe(a.createdAt).equals(safe(b.createdAt));
            }
        });
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemAccountHistoriDompetBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        RiwayatDompet item = getItem(position);
        ItemAccountHistoriDompetBinding b = holder.binding;
        int black = ContextCompat.getColor(b.getRoot().getContext(), R.color.account_text);
        int muted = ContextCompat.getColor(b.getRoot().getContext(), R.color.text_muted);
        b.nomor.setText((position + 1) + ".");
        if (safe(item.jenis).length() >= 1) {
            b.jenis.setText(item.jenis.toUpperCase(Locale.ROOT));
            b.jenis.setTextColor(black);
            b.jenis.setTypeface(null, Typeface.BOLD);
        } else {
            b.jenis.setText(R.string.account_dompet_tanpa_jenis);
            b.jenis.setTextColor(muted);
            b.jenis.setTypeface(null, Typeface.BOLD_ITALIC);
        }
        if (safe(item.keterangan).length() >= 1) {
            b.keterangan.setText(item.keterangan);
            b.keterangan.setTextColor(black);
            b.keterangan.setTypeface(null, Typeface.NORMAL);
        } else {
            b.keterangan.setText(R.string.account_dompet_tanpa_keterangan);
            b.keterangan.setTextColor(muted);
            b.keterangan.setTypeface(null, Typeface.BOLD_ITALIC);
        }
        b.tanggal.setText("Pada " + safe(item.createdAt));
        b.nilai.setText(Currency.formatRupiah(item.nilai, 0));
        b.nilai.setTextColor(ContextCompat.getColor(b.getRoot().getContext(),
                item.nilai >= 0 ? R.color.green : R.color.text_danger));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemAccountHistoriDompetBinding binding;

        Holder(ItemAccountHistoriDompetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
