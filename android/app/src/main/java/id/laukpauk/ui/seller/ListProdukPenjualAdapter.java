package id.laukpauk.ui.seller;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;
import java.util.Objects;

import id.laukpauk.R;
import id.laukpauk.data.model.seller.SellerNumbers;
import id.laukpauk.data.model.seller.products.ProdukPenjual;
import id.laukpauk.databinding.ItemSellerProdukTileBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.ServerPath;

/** ListProdukPenjualItem / ProdukPaketPenjualItem: a product (or paket) tile with edit and delete. */
public class ListProdukPenjualAdapter extends ListAdapter<ProdukPenjual, ListProdukPenjualAdapter.Holder> {

    public interface Listener {
        void onOpen(ProdukPenjual item);

        void onEdit(ProdukPenjual item);

        void onHapus(ProdukPenjual item);
    }

    private final boolean paket;
    private final Listener listener;

    public ListProdukPenjualAdapter(boolean paket, Listener listener) {
        super(new DiffUtil.ItemCallback<ProdukPenjual>() {
            @Override
            public boolean areItemsTheSame(@NonNull ProdukPenjual a, @NonNull ProdukPenjual b) {
                return Objects.equals(a.id, b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull ProdukPenjual a, @NonNull ProdukPenjual b) {
                return Objects.equals(a.nama, b.nama) && Objects.equals(a.foto, b.foto)
                        && Objects.equals(a.keterangan, b.keterangan) && Objects.equals(a.harga, b.harga)
                        && Objects.equals(a.diskon, b.diskon)
                        && Objects.equals(a.namaKategoriProduk, b.namaKategoriProduk);
            }
        });
        this.paket = paket;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemSellerProdukTileBinding.inflate(LayoutInflater.from(parent.getContext()), parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        final ProdukPenjual item = getItem(position);
        ItemSellerProdukTileBinding b = h.binding;
        String foto = item.foto == null ? "" : item.foto;
        if (foto.isEmpty()) {
            Images.asset(b.foto, "placeholder-150.png");
        } else {
            Images.circle(b.foto, Images.server((paket ? ServerPath.uploadGambarPaket
                    : ServerPath.uploadGambarProduk) + "/compress", foto));
        }
        b.nama.setText(item.nama == null ? "" : item.nama.toUpperCase(Locale.ROOT));
        if (paket) {
            b.subtitle.setVisibility(android.view.View.GONE);
            b.harga.setVisibility(android.view.View.VISIBLE);
            b.harga.setText(Currency.formatRupiah(
                    SellerNumbers.toInt(item.harga) - SellerNumbers.toInt(item.diskon), 0));
        } else {
            b.harga.setVisibility(android.view.View.GONE);
            b.subtitle.setVisibility(android.view.View.VISIBLE);
            boolean unknown = item.namaKategoriProduk == null;
            b.subtitle.setText(unknown ? b.getRoot().getContext()
                    .getString(R.string.seller_produk_kategori_tidak_diketahui) : item.namaKategoriProduk);
            b.subtitle.setTypeface(b.subtitle.getTypeface(), unknown ? Typeface.ITALIC : Typeface.NORMAL);
        }
        boolean hasKeterangan = item.keterangan != null && !item.keterangan.isEmpty();
        b.keteranganBox.setVisibility(hasKeterangan ? android.view.View.VISIBLE : android.view.View.GONE);
        b.keterangan.setText(item.keterangan);
        b.row.setOnClickListener(v -> listener.onOpen(item));
        b.edit.setOnClickListener(v -> listener.onEdit(item));
        b.hapus.setOnClickListener(v -> listener.onHapus(item));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemSellerProdukTileBinding binding;

        Holder(ItemSellerProdukTileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
