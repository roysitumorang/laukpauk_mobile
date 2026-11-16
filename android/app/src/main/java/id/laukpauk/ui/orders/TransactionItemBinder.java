package id.laukpauk.ui.orders;

import android.view.LayoutInflater;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import id.laukpauk.R;
import id.laukpauk.data.model.orders.TransactionItem;
import id.laukpauk.databinding.ItemOrdersGiftRowBinding;
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
                for (TransactionItem item : res.data) {
                    ItemOrdersItemRowBinding row = ItemOrdersItemRowBinding.inflate(inflater, b.itemsContainer,
                            false);
                    bindRow(f, row, item);
                    b.itemsContainer.addView(row.getRoot());
                }
            }
        });
        vm.getCartCodeMissing().observe(f.getViewLifecycleOwner(), missing ->
                b.giftMessage.setVisibility(Boolean.TRUE.equals(missing) ? View.VISIBLE : View.GONE));
        vm.getGifts().observe(f.getViewLifecycleOwner(), rows -> {
            b.giftContainer.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(f.requireContext());
            for (TransactionItemViewModel.GiftRow g : rows) {
                ItemOrdersGiftRowBinding row = ItemOrdersGiftRowBinding.inflate(inflater, b.giftContainer, false);
                if (g.info != null) {
                    row.progress.setVisibility(View.GONE);
                    row.content.setVisibility(View.VISIBLE);
                    row.name.setText(f.getString(R.string.orders_gift_format, OrderUi.nz(g.info.name)));
                    row.description.setText(OrderUi.nz(g.info.description));
                }
                b.giftContainer.addView(row.getRoot());
            }
        });
    }

    private static void bindRow(Fragment f, ItemOrdersItemRowBinding row, TransactionItem item) {
        int discount = item.discount + item.promoDiscount;
        int total = item.quantity * (item.price - discount);
        String status = OrderUi.nz(item.status).toLowerCase();
        boolean rejected = status.equals("rejected");
        int statusText = R.string.orders_product_processed;
        int statusColor = R.color.text_muted;
        if (status.equals("ok")) {
            statusText = R.string.orders_product_exists;
            statusColor = R.color.text_success;
        }
        if (rejected) {
            statusText = R.string.orders_product_unavailable;
            statusColor = R.color.text_danger;
        }
        row.name.setText(item.productName != null ? item.productName
                : f.getString(R.string.orders_unknown_product_format, item.quantity, item.unit()));
        row.total.setText(Currency.formatRupiah(total, 0));
        row.total.setTextColor(ContextCompat.getColor(f.requireContext(),
                rejected ? R.color.text_muted : R.color.nearly_black));
        row.total.getPaint().setStrikeThruText(rejected);
        row.status.setText(statusText);
        row.status.setTextColor(ContextCompat.getColor(f.requireContext(), statusColor));
        String note = OrderUi.nz(item.note);
        row.note.setVisibility(note.isEmpty() ? View.GONE : View.VISIBLE);
        row.note.setText(f.getString(R.string.orders_note_format, note));
    }
}
