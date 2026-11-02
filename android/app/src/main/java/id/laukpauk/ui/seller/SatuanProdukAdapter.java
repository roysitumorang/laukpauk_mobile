package id.laukpauk.ui.seller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;
import java.util.Objects;

import id.laukpauk.R;
import id.laukpauk.data.model.seller.SellerNumbers;
import id.laukpauk.data.model.seller.products.SatuanProdukData;
import id.laukpauk.databinding.ItemSellerProdukSatuanBinding;
import id.laukpauk.util.Currency;

/** SatuanProdukItem: one unit/price row (optional number, editable or plain prices, edit/delete options). */
public class SatuanProdukAdapter extends ListAdapter<SatuanProdukData, SatuanProdukAdapter.Holder> {

    public interface Listener {
        void onEdit(SatuanProdukData item);

        void onHapus(SatuanProdukData item);
    }

    private final boolean showNomor;
    private final boolean updateAble;
    private final boolean showOptions;
    private final Listener listener;

    public SatuanProdukAdapter(boolean showNomor, boolean updateAble, boolean showOptions, Listener listener) {
        super(new DiffUtil.ItemCallback<SatuanProdukData>() {
            @Override
            public boolean areItemsTheSame(@NonNull SatuanProdukData a, @NonNull SatuanProdukData b) {
                return Objects.equals(a.id, b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull SatuanProdukData a, @NonNull SatuanProdukData b) {
                return Objects.equals(a.nama, b.nama) && Objects.equals(a.harga, b.harga)
                        && Objects.equals(a.diskon, b.diskon) && Objects.equals(a.jenisHarga, b.jenisHarga)
                        && Objects.equals(a.keterangan, b.keterangan);
            }
        });
        this.showNomor = showNomor;
        this.updateAble = updateAble;
        this.showOptions = showOptions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemSellerProdukSatuanBinding.inflate(LayoutInflater.from(parent.getContext()), parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        final SatuanProdukData s = getItem(position);
        ItemSellerProdukSatuanBinding b = h.binding;
        b.nomor.setVisibility(showNomor ? View.VISIBLE : View.GONE);
        b.nomor.setText((position + 1) + ".");
        b.nama.setText(s.nama);
        b.chip.setText(b.getRoot().getContext().getString(R.string.seller_produk_harga_chip,
                (s.jenisHarga == null ? "" : s.jenisHarga).toUpperCase(Locale.ROOT)));
        if (updateAble) {
            // The legacy item shows editable fields but never saves them.
            b.hargaEdit.setVisibility(View.VISIBLE);
            b.diskonEdit.setVisibility(View.VISIBLE);
            b.hargaText.setVisibility(View.GONE);
            b.diskonText.setVisibility(View.GONE);
            b.hargaEdit.setText(s.harga);
            b.diskonEdit.setText(s.diskon);
        } else {
            b.hargaEdit.setVisibility(View.GONE);
            b.diskonEdit.setVisibility(View.GONE);
            b.hargaText.setVisibility(View.VISIBLE);
            b.diskonText.setVisibility(View.VISIBLE);
            b.hargaText.setText(Currency.formatRupiah(SellerNumbers.toInt(s.harga), 0));
            b.diskonText.setText(Currency.formatRupiah(SellerNumbers.toInt(s.diskon), 0));
        }
        b.options.setVisibility(showOptions ? View.VISIBLE : View.GONE);
        b.edit.setOnClickListener(v -> listener.onEdit(s));
        b.hapus.setOnClickListener(v -> listener.onHapus(s));
        boolean hasKet = s.keterangan != null && !s.keterangan.isEmpty();
        b.keteranganBox.setVisibility(hasKet ? View.VISIBLE : View.GONE);
        b.keterangan.setText(s.keterangan);
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemSellerProdukSatuanBinding binding;

        Holder(ItemSellerProdukSatuanBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
