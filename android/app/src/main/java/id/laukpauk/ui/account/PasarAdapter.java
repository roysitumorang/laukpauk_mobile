package id.laukpauk.ui.account;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import id.laukpauk.R;
import id.laukpauk.data.model.account.Pasar;
import id.laukpauk.databinding.ItemAccountPasarBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** Horizontal list of the available markets (legacy ListPasar). */
public class PasarAdapter extends ListAdapter<Pasar, PasarAdapter.Holder> {

    public interface OnClick {
        void onPasar(Pasar pasar);
    }

    private final OnClick onClick;
    private String selected = "0";

    public PasarAdapter(OnClick onClick) {
        super(new DiffUtil.ItemCallback<Pasar>() {
            @Override
            public boolean areItemsTheSame(@NonNull Pasar a, @NonNull Pasar b) {
                return String.valueOf(a.id).equals(String.valueOf(b.id));
            }

            @Override
            public boolean areContentsTheSame(@NonNull Pasar a, @NonNull Pasar b) {
                return String.valueOf(a.nama).equals(String.valueOf(b.nama))
                        && String.valueOf(a.gambar).equals(String.valueOf(b.gambar));
            }
        });
        this.onClick = onClick;
    }

    public void setSelected(String id) {
        this.selected = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemAccountPasarBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Pasar item = getItem(position);
        ItemAccountPasarBinding b = holder.binding;
        b.nama.setText(item.nama);
        Images.circle(b.gambar, ServerPath.baseURL + "/" + ServerPath.uploadGambarPasar + "/compress/" + item.gambar);
        b.root.setBackgroundResource(String.valueOf(item.id).equals(selected) ? R.drawable.bg_account_pasar_selected : 0);
        b.root.setOnClickListener(v -> onClick.onPasar(item));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemAccountPasarBinding binding;

        Holder(ItemAccountPasarBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
