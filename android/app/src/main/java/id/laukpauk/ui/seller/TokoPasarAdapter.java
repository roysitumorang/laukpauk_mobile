package id.laukpauk.ui.seller;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import id.laukpauk.R;
import id.laukpauk.data.model.seller.shop.Pasar;
import id.laukpauk.databinding.ItemSellerTokoPasarBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** Horizontal market picker of the "lokasi berjualan" sheet (ListPasar.dart). */
class TokoPasarAdapter extends ListAdapter<Pasar, TokoPasarAdapter.Holder> {

    interface OnClick {
        void onClick(Pasar pasar);
    }

    private final OnClick onClick;
    private String selectedId;

    TokoPasarAdapter(OnClick onClick) {
        super(new DiffUtil.ItemCallback<Pasar>() {
            @Override
            public boolean areItemsTheSame(@NonNull Pasar a, @NonNull Pasar b) {
                return a.id != null && a.id.equals(b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Pasar a, @NonNull Pasar b) {
                return java.util.Objects.equals(a.nama, b.nama) && java.util.Objects.equals(a.gambar, b.gambar);
            }
        });
        this.onClick = onClick;
    }

    void setSelectedId(String id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemSellerTokoPasarBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Pasar p = getItem(position);
        ItemSellerTokoPasarBinding b = holder.binding;
        b.nama.setText(p.nama == null ? "" : p.nama);
        b.root.setBackgroundResource(p.id != null && p.id.equals(selectedId)
                ? R.drawable.bg_seller_toko_pasar_selected : 0);
        Images.circle(b.gambar, Images.server(ServerPath.uploadGambarPasar, "compress/" + p.gambar));
        b.root.setOnClickListener(v -> onClick.onClick(p));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemSellerTokoPasarBinding binding;

        Holder(ItemSellerTokoPasarBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
