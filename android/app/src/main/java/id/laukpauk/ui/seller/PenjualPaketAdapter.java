package id.laukpauk.ui.seller;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import id.laukpauk.R;
import id.laukpauk.data.model.product.Paket;
import id.laukpauk.databinding.ItemSellerPenjualPaketBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.DefaultImage;
import id.laukpauk.util.ServerPath;

/** Paket tile of a seller's shop (legacy PaketPenjualCard): "Beli", or "Edit" on the seller's own paket. */
public class PenjualPaketAdapter extends ListAdapter<Paket, PenjualPaketAdapter.Holder> {

    public interface Listener {
        void onOpen(Paket paket);

        void onButton(Paket paket, boolean mine);
    }

    private final String currentUserId;
    private final Listener listener;

    public PenjualPaketAdapter(String currentUserId, Listener listener) {
        super(new DiffUtil.ItemCallback<Paket>() {
            @Override
            public boolean areItemsTheSame(@NonNull Paket a, @NonNull Paket b) {
                return TextUtils.equals(a.id, b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Paket a, @NonNull Paket b) {
                return TextUtils.equals(a.nama, b.nama) && a.harga == b.harga && a.diskon == b.diskon
                        && TextUtils.equals(a.foto, b.foto);
            }
        });
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSellerPenjualPaketBinding b = ItemSellerPenjualPaketBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        float density = parent.getResources().getDisplayMetrics().density;
        int cellWidth = (int) ((parent.getResources().getDisplayMetrics().widthPixels - 20 * density) / 3);
        b.getRoot().setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, cellWidth * 2));
        return new Holder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        h.bind(getItem(position));
    }

    class Holder extends RecyclerView.ViewHolder {
        private final ItemSellerPenjualPaketBinding b;

        Holder(ItemSellerPenjualPaketBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(Paket p) {
            boolean mine = p.idPenjual != null && p.idPenjual.equals(currentUserId) && !currentUserId.isEmpty();
            boolean isDefault = DefaultImage.defaultPaket.equals(p.foto);
            b.image.setScaleType(isDefault ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.FIT_XY);
            Images.url(b.image, Images.server(ServerPath.uploadGambarPaket + "/compress", p.foto == null ? "" : p.foto));
            b.nama.setText(p.nama);
            double persen = p.harga == 0 ? 0 : (double) p.diskon / p.harga * 100;
            b.diskonRow.setVisibility(persen > 0 ? View.VISIBLE : View.GONE);
            b.hargaKotor.setText(Currency.formatRupiah(p.harga, 0));
            b.badge.setVisibility(p.diskon >= 1 ? View.VISIBLE : View.GONE);
            b.badge.setText("-" + Currency.formatSatuan((long) persen, "%"));
            b.hargaBersih.setText(Currency.formatRupiah(p.harga - p.diskon, 0));
            b.button.setText(mine ? R.string.product_edit : R.string.product_beli);
            b.button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(
                    b.getRoot().getContext(), mine ? R.color.product_warning : R.color.green)));
            b.button.setOnClickListener(v -> listener.onButton(p, mine));
            b.card.setOnClickListener(v -> listener.onOpen(p));
        }
    }
}
