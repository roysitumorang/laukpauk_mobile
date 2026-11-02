package id.laukpauk.ui.account;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import id.laukpauk.R;
import id.laukpauk.data.model.account.RiwayatPoint;
import id.laukpauk.databinding.ItemAccountHistoriPoinBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;

/** One row of the points history. */
public class HistoriPoinAdapter extends ListAdapter<RiwayatPoint, HistoriPoinAdapter.Holder> {

    public HistoriPoinAdapter() {
        super(new DiffUtil.ItemCallback<RiwayatPoint>() {
            @Override
            public boolean areItemsTheSame(@NonNull RiwayatPoint a, @NonNull RiwayatPoint b) {
                return a == b;
            }

            @Override
            public boolean areContentsTheSame(@NonNull RiwayatPoint a, @NonNull RiwayatPoint b) {
                return a.nilai == b.nilai && safe(a.keterangan).equals(safe(b.keterangan))
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
        return new Holder(ItemAccountHistoriPoinBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        RiwayatPoint item = getItem(position);
        ItemAccountHistoriPoinBinding b = holder.binding;
        Images.asset(b.icon, "poin.png");
        String keterangan = safe(item.keterangan);
        if (RiwayatPoint.CREATED_FROM_TRANSAKSI.equals(item.createdFrom)) {
            keterangan = "Transaksi " + keterangan;
        }
        b.nilai.setText(Currency.formatRupiah(item.nilai, 0));
        b.nilai.setTextColor(ContextCompat.getColor(b.getRoot().getContext(),
                item.nilai >= 0 ? R.color.text_success : R.color.text_danger));
        b.keterangan.setText(keterangan);
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemAccountHistoriPoinBinding binding;

        Holder(ItemAccountHistoriPoinBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
