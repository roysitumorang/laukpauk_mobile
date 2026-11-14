package id.laukpauk.ui.orders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import id.laukpauk.R;
import id.laukpauk.data.model.orders.ItemTransaksi;
import id.laukpauk.data.model.orders.RepeatOrderResponses;
import id.laukpauk.databinding.ItemOrdersRoItemBinding;
import id.laukpauk.databinding.ItemOrdersRoBinding;
import id.laukpauk.util.Currency;

/** The single card of Repeat Order: the old transaction's lines (ItemTransaksiRO.dart) or the "cannot re-order" note. */
public class RepeatOrderAdapter extends ListAdapter<RepeatOrderResponses.Items, RepeatOrderAdapter.Holder> {

    private static final DiffUtil.ItemCallback<RepeatOrderResponses.Items> DIFF =
            new DiffUtil.ItemCallback<RepeatOrderResponses.Items>() {
        @Override
        public boolean areItemsTheSame(@NonNull RepeatOrderResponses.Items a, @NonNull RepeatOrderResponses.Items b) {
            return true;
        }

        @Override
        public boolean areContentsTheSame(@NonNull RepeatOrderResponses.Items a,
                                          @NonNull RepeatOrderResponses.Items b) {
            return false;
        }
    };

    public RepeatOrderAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemOrdersRoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        h.bind(getItem(position));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        private final ItemOrdersRoBinding b;

        Holder(ItemOrdersRoBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(RepeatOrderResponses.Items p) {
            Context ctx = b.getRoot().getContext();
            b.card.setStrokeColor(ContextCompat.getColor(ctx,
                    p.isAvailable ? R.color.nearly_white : R.color.text_danger));

            b.availableGroup.setVisibility(p.isAvailable ? View.VISIBLE : View.GONE);
            b.unavailableGroup.setVisibility(p.isAvailable ? View.GONE : View.VISIBLE);
            b.itemsContainer.removeAllViews();
            if (p.isAvailable && p.itemTransaksi != null) {
                LayoutInflater inflater = LayoutInflater.from(ctx);
                for (ItemTransaksi item : p.itemTransaksi) {
                    ItemOrdersRoItemBinding row = ItemOrdersRoItemBinding.inflate(inflater, b.itemsContainer,
                            false);
                    row.nama.setText(ctx.getString(R.string.orders_ro_item_format, OrderUi.nz(item.namaProduk),
                            item.quantity, item.satuan()));
                    row.total.setText(Currency.formatRupiah(item.quantity * (item.harga - item.diskon), 0));
                    String catatan = OrderUi.nz(item.catatan);
                    row.catatan.setVisibility(catatan.isEmpty() ? View.GONE : View.VISIBLE);
                    row.catatan.setText(ctx.getString(R.string.orders_catatan_format, catatan));
                    b.itemsContainer.addView(row.getRoot());
                }
            }
        }
    }
}
