package id.laukpauk.ui.recipe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.model.recipe.SatuanProduk;
import id.laukpauk.databinding.ItemRecipeBahanBinding;
import id.laukpauk.util.Currency;

/** The ingredient rows of a recipe (legacy ResepItemItem). */
class BahanAdapter extends ListAdapter<BahanRow, BahanAdapter.Holder> {

    interface Listener {
        void onPickSatuan(BahanRow row, int satuanIndex);

        void onPickJumlah(BahanRow row, int quantity);

        void onCart(BahanRow row);
    }

    private final Listener listener;

    BahanAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<BahanRow>() {
            @Override
            public boolean areItemsTheSame(@NonNull BahanRow a, @NonNull BahanRow b) {
                return String.valueOf(a.item.id).equals(String.valueOf(b.item.id));
            }

            @Override
            public boolean areContentsTheSame(@NonNull BahanRow a, @NonNull BahanRow b) {
                return a.sameAs(b);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemRecipeBahanBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        BahanRow row = getItem(position);
        ItemRecipeBahanBinding b = h.b;
        Context ctx = b.getRoot().getContext();
        float d = ctx.getResources().getDisplayMetrics().density;

        int pad = row.selected ? Math.round(10 * d) : 0;
        b.root.setPadding(pad, pad, pad, pad);
        b.root.setBackgroundResource(row.selected ? R.drawable.bg_recipe_item_selected : 0);

        b.nomor.setText(row.index + ".");
        b.nama.setText(row.item.namaProduk == null ? "" : row.item.namaProduk.toUpperCase(Locale.ROOT));
        b.takaran.setText(ctx.getString(R.string.recipe_diperlukan, row.item.takaran == null ? "" : row.item.takaran));

        b.spinner.setVisibility(!row.satuanLoaded ? View.VISIBLE : View.GONE);
        b.tidakAdaSatuan.setVisibility(row.satuanLoaded && row.satuan.isEmpty() ? View.VISIBLE : View.GONE);
        boolean hasSatuan = row.satuanLoaded && !row.satuan.isEmpty();
        b.controls.setVisibility(hasSatuan ? View.VISIBLE : View.GONE);

        if (hasSatuan) {
            b.satuan.setText(row.selectedSatuan == null ? ctx.getString(R.string.recipe_pilih_satuan)
                    : row.selectedSatuan.nama);
            b.satuan.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(ctx, b.satuan);
                menu.getMenu().add(0, 0, 0, R.string.recipe_pilih_satuan);
                for (int i = 0; i < row.satuan.size(); i++) {
                    menu.getMenu().add(0, i + 1, i + 1, row.satuan.get(i).nama);
                }
                menu.setOnMenuItemClickListener(m -> {
                    if (m.getItemId() > 0) {
                        listener.onPickSatuan(row, m.getItemId() - 1);
                    }
                    return true;
                });
                menu.show();
            });

            SatuanProduk s = row.selectedSatuan;
            boolean tidakPas = s != null && s.isTidakPas();
            b.jumlah.setVisibility(tidakPas ? View.VISIBLE : View.GONE);
            b.jumlah.setText(ctx.getString(R.string.recipe_item_count, row.quantity));
            b.jumlah.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(ctx, b.jumlah);
                for (int q = 1; q <= 10; q++) {
                    menu.getMenu().add(0, q, q, ctx.getString(R.string.recipe_item_count, q));
                }
                menu.setOnMenuItemClickListener(m -> {
                    listener.onPickJumlah(row, m.getItemId());
                    return true;
                });
                menu.show();
            });

            b.keranjang.setImageResource(row.selected ? R.drawable.ic_delete : R.drawable.ic_add_shopping_cart);
            b.keranjang.setImageTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(ctx,
                    row.selected ? R.color.text_danger : R.color.green)));
            b.keranjang.setOnClickListener(v -> listener.onCart(row));
        }

        b.terpilih.setVisibility(row.selected ? View.VISIBLE : View.GONE);
        if (row.selected) {
            String nama = row.selectedSatuan == null ? "" : String.valueOf(row.selectedSatuan.nama);
            b.diorder.setText(ctx.getString(R.string.recipe_diorder, Currency.formatSatuan(row.quantity,
                    "x " + nama + " @ " + Currency.formatRupiah(row.hargaBersih(), 0))));
            b.subTotal.setText(ctx.getString(R.string.recipe_sub_total,
                    Currency.formatRupiah((long) row.hargaBersih() * row.quantity, 0)));
        }
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemRecipeBahanBinding b;

        Holder(ItemRecipeBahanBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }
    }
}
