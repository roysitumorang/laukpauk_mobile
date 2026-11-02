package id.laukpauk.ui.orders;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import id.laukpauk.R;
import id.laukpauk.data.model.orders.ItemTransaksi;
import id.laukpauk.databinding.ItemOrdersGiftRowBinding;
import id.laukpauk.databinding.ItemOrdersItemRowBinding;
import id.laukpauk.databinding.ViewOrdersItemTransaksiBinding;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.util.Currency;
import id.laukpauk.util.ServerPath;

/** Fills the "Daftar Belanjaan" card from an {@link ItemTransaksiViewModel} (shared by two screens). */
final class ItemTransaksiBinder {
    private ItemTransaksiBinder() {}

    static void bind(Fragment f, ViewOrdersItemTransaksiBinding b, ItemTransaksiViewModel vm, String idTransaksi,
                     @Nullable String idPenjual) {
        if (idPenjual != null) {
            b.invoice.setVisibility(View.VISIBLE);
            b.invoice.setOnClickListener(v -> {
                String url = ServerPath.apiBaseURL + "/Transaksi/invoice/" + idTransaksi + "?idPenjual=" + idPenjual;
                if (!OrderUi.openUrl(f, url)) {
                    Messages.snack(f, f.getString(R.string.orders_url_tidak_valid));
                }
            });
        }
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
                for (ItemTransaksi item : res.data) {
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
            for (ItemTransaksiViewModel.GiftRow g : rows) {
                ItemOrdersGiftRowBinding row = ItemOrdersGiftRowBinding.inflate(inflater, b.giftContainer, false);
                if (g.info != null) {
                    row.progress.setVisibility(View.GONE);
                    row.content.setVisibility(View.VISIBLE);
                    row.nama.setText(f.getString(R.string.orders_gift_format, OrderUi.nz(g.info.nama)));
                    row.deskripsi.setText(OrderUi.nz(g.info.deskripsi));
                }
                b.giftContainer.addView(row.getRoot());
            }
        });
    }

    private static void bindRow(Fragment f, ItemOrdersItemRowBinding row, ItemTransaksi item) {
        int diskon = item.diskon + item.diskonPromo;
        int total = item.quantity * (item.harga - diskon);
        String status = OrderUi.nz(item.status).toLowerCase();
        boolean rejected = status.equals("rejected");
        int statusText = R.string.orders_produk_diproses;
        int statusColor = R.color.text_muted;
        if (status.equals("ok")) {
            statusText = R.string.orders_produk_ada;
            statusColor = R.color.text_success;
        }
        if (rejected) {
            statusText = R.string.orders_produk_tidak_tersedia;
            statusColor = R.color.text_danger;
        }
        row.nama.setText(item.namaProduk != null ? item.namaProduk
                : f.getString(R.string.orders_produk_tidak_dikenal_format, item.quantity, item.satuan()));
        row.total.setText(Currency.formatRupiah(total, 0));
        row.total.setTextColor(ContextCompat.getColor(f.requireContext(),
                rejected ? R.color.text_muted : R.color.nearly_black));
        row.total.getPaint().setStrikeThruText(rejected);
        row.status.setText(statusText);
        row.status.setTextColor(ContextCompat.getColor(f.requireContext(), statusColor));
        String catatan = OrderUi.nz(item.catatan);
        row.catatan.setVisibility(catatan.isEmpty() ? View.GONE : View.VISIBLE);
        row.catatan.setText(f.getString(R.string.orders_catatan_format, catatan));
    }
}
