package id.laukpauk.ui.orders;

import android.view.LayoutInflater;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import id.laukpauk.R;
import id.laukpauk.data.model.orders.BuyerTransactionItem;
import id.laukpauk.databinding.ItemOrdersItemRowBinding;
import id.laukpauk.databinding.ViewOrdersTransactionItemBinding;
import id.laukpauk.util.Currency;

/** Fills the "Daftar Belanjaan" card from an {@link TransactionItemViewModel} (shared by two screens). */
final class TransactionItemBinder {
    private TransactionItemBinder() {}

    static void bind(Fragment f, ViewOrdersTransactionItemBinding b, TransactionItemViewModel vm) {
        vm.getItems().observe(f.getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            if (res.isLoading()) {
                b.progress.setVisibility(View.VISIBLE);
                b.itemsGroup.setVisibility(View.GONE);
                b.emptyGroup.setVisibility(View.GONE);
                return;
            }
            b.progress.setVisibility(View.GONE);
            boolean has = res.isSuccess() && res.data != null && !res.data.isEmpty();
            b.itemsGroup.setVisibility(has ? View.VISIBLE : View.GONE);
            b.emptyGroup.setVisibility(has ? View.GONE : View.VISIBLE);
            b.itemsContainer.removeAllViews();
            if (has) {
                LayoutInflater inflater = LayoutInflater.from(f.requireContext());
                for (BuyerTransactionItem item : res.data) {
                    ItemOrdersItemRowBinding row = ItemOrdersItemRowBinding.inflate(inflater, b.itemsContainer,
                            false);
                    bindRow(f, row, item);
                    b.itemsContainer.addView(row.getRoot());
                }
            }
        });
    }

    /**
     * Name, line total (after revisions), whether it is ready (processed or later), out of stock, or revised, and the
     * buyer's and the seller's notes.
     */
    private static void bindRow(Fragment f, ItemOrdersItemRowBinding row, BuyerTransactionItem item) {
        boolean unavailable = item.outOfStock;
        boolean revised = !unavailable && item.revisedQuantity != item.quantity;
        int statusText = R.string.orders_product_processed;
        int statusColor = R.color.text_muted;
        if (unavailable) {
            statusText = R.string.orders_product_unavailable;
            statusColor = R.color.text_danger;
        }
        String unit = OrderUi.nz(item.stockUnit);
        String name = OrderUi.nz(item.productName);
        row.name.setText(f.getString(R.string.orders_item_name_format, name, item.quantity, unit));
        row.total.setText(Currency.formatRupiah(unavailable ? item.subtotal : item.revisedSubtotal, 0));
        row.total.setTextColor(ContextCompat.getColor(f.requireContext(),
                unavailable ? R.color.text_muted : R.color.nearly_black));
        row.total.getPaint().setStrikeThruText(unavailable);
        if (revised) {
            row.status.setText(f.getString(R.string.orders_revised_quantity_format, item.revisedQuantity, unit));
            row.status.setTextColor(ContextCompat.getColor(f.requireContext(), R.color.text_danger));
        } else {
            row.status.setText(statusText);
            row.status.setTextColor(ContextCompat.getColor(f.requireContext(), statusColor));
        }
        StringBuilder note = new StringBuilder(OrderUi.nz(item.buyerNote).trim());
        String revision = OrderUi.nz(item.revisionNote).trim();
        if (!revision.isEmpty()) {
            if (note.length() > 0) {
                note.append(" · ");
            }
            note.append(revision);
        }
        row.note.setVisibility(note.length() == 0 ? View.GONE : View.VISIBLE);
        row.note.setText(f.getString(R.string.orders_note_format, note.toString()));
    }
}
