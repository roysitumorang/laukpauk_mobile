package id.laukpauk.ui.promo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.model.promo.ProdukPromo;
import id.laukpauk.databinding.ItemPromoProdukBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.DefaultImage;
import id.laukpauk.util.ServerPath;

/** The promo product cards (legacy PromoItem). */
public class PromoAdapter extends ListAdapter<ProdukPromo, PromoAdapter.Holder> {

    public interface Listener {
        void onOpen(ProdukPromo item);

        void onBeli(ProdukPromo item);
    }

    private final Listener listener;

    public PromoAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<ProdukPromo>() {
            @Override
            public boolean areItemsTheSame(@NonNull ProdukPromo a, @NonNull ProdukPromo b) {
                return String.valueOf(a.idPromoItem).equals(String.valueOf(b.idPromoItem));
            }

            @Override
            public boolean areContentsTheSame(@NonNull ProdukPromo a, @NonNull ProdukPromo b) {
                return a.hargaBersihPromo() == b.hargaBersihPromo() && a.hargaKotor() == b.hargaKotor()
                        && String.valueOf(a.namaProduk).equals(String.valueOf(b.namaProduk))
                        && String.valueOf(a.fotoProduk).equals(String.valueOf(b.fotoProduk))
                        && String.valueOf(a.keteranganSingkat).equals(String.valueOf(b.keteranganSingkat))
                        && String.valueOf(a.maxOrder).equals(String.valueOf(b.maxOrder));
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemPromoProdukBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        h.bind(getItem(position), listener);
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final ItemPromoProdukBinding b;

        Holder(ItemPromoProdukBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(ProdukPromo item, Listener listener) {
            String foto = item.fotoProduk == null ? "" : item.fotoProduk;
            boolean isDefaultImage = foto.equals(DefaultImage.defaultProduk);
            b.foto.setScaleType(isDefaultImage ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.FIT_XY);
            Images.url(b.foto, ServerPath.baseURL + "/" + ServerPath.uploadGambarProduk + "/compress/" + foto);

            int hargaKotor = item.hargaKotor();
            int diskon = item.diskon();
            double persentase = diskon >= 1 && hargaKotor >= 1 ? (double) diskon / hargaKotor * 100 : 0;
            if (persentase >= 0.1) {
                b.hemat.setVisibility(View.VISIBLE);
                b.hemat.setText(b.getRoot().getContext().getString(R.string.promo_hemat,
                        String.format(Locale.US, "%.1f", persentase)));
            } else {
                b.hemat.setVisibility(View.GONE);
            }
            b.max.setText(b.getRoot().getContext().getString(R.string.promo_max,
                    item.maxOrder == null ? "" : item.maxOrder));

            b.nama.setText(item.namaProduk);
            boolean hasDesc = item.keteranganSingkat != null && !item.keteranganSingkat.isEmpty();
            b.deskripsiScroll.setVisibility(hasDesc ? View.VISIBLE : View.GONE);
            b.deskripsi.setText(item.keteranganSingkat);

            b.hargaBersih.setText(Currency.formatRupiah(item.hargaBersihPromo(), 0));
            b.hargaKotor.setText(Currency.formatRupiah(hargaKotor, 0));
            b.hargaKotor.setPaintFlags(b.hargaKotor.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);

            b.card.setOnClickListener(v -> listener.onOpen(item));
            b.beli.setOnClickListener(v -> listener.onBeli(item));
        }
    }
}
