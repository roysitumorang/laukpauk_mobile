package id.laukpauk.ui.product;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.model.cart.AddToCartResult;
import id.laukpauk.data.model.product.HargaProduk;
import id.laukpauk.data.model.product.Produk;
import id.laukpauk.data.model.product.SatuanSheetData;
import id.laukpauk.databinding.ItemProductPilihanSatuanBinding;
import id.laukpauk.databinding.SheetProductSatuanBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.util.Currency;
import id.laukpauk.util.ServerPath;

/**
 * The "Pilihan Satuan" bottom sheet (legacy ShowSatuanProduk + PilihanSatuanProduk + PilihanSatuanProdukItem):
 * pick a unit (and quantity) of a product and add it to the cart.
 */
final class ShowSatuanProduk {

    private final Fragment fragment;
    private final AppContainer c;
    private final String idProduk;
    @Nullable private final String idPromoArg;
    private final BottomSheetDialog dialog;
    private final SheetProductSatuanBinding b;
    private final List<Row> rows = new ArrayList<>();
    @Nullable private Row selected;
    private boolean adding;

    private static final class Row {
        HargaProduk h;
        ItemProductPilihanSatuanBinding b;
        int quantity = 1;

        String key() {
            return h.promo() ? "promo_" + h.idPromo : h.id;
        }
    }

    static void show(Fragment fragment, String idProduk, SatuanSheetData data, @Nullable String idPromo) {
        new ShowSatuanProduk(fragment, idProduk, data, idPromo);
    }

    private ShowSatuanProduk(Fragment fragment, String idProduk, SatuanSheetData data, @Nullable String idPromo) {
        this.fragment = fragment;
        this.c = AppContainer.from(fragment.requireContext());
        this.idProduk = idProduk;
        this.idPromoArg = idPromo;
        Context ctx = fragment.requireContext();
        dialog = new BottomSheetDialog(ctx);
        b = SheetProductSatuanBinding.inflate(LayoutInflater.from(ctx));
        dialog.setContentView(b.getRoot());

        Produk p = data.produk;
        if (p != null) {
            b.nama.setText(p.nama);
            b.kategori.setText(p.namaKategoriProduk != null ? p.namaKategoriProduk
                    : ctx.getString(R.string.product_kategori_tidak_diketahui));
            Images.circle(b.foto, Images.server(ServerPath.uploadGambarProduk + "/compress", p.foto == null ? "" : p.foto));
        }
        b.noteLuarCa.setVisibility(data.inCoverageArea ? View.GONE : View.VISIBLE);
        b.noteLuarCaText.setText(ctx.getString(R.string.product_note_luar_ca, data.ongkirPerKm));
        b.beli.setOnClickListener(v -> onBeli());

        boolean stock = p == null || p.isStockAvailable();
        if (stock) {
            loadRows();
        } else {
            b.stokHabis.setVisibility(View.VISIBLE);
        }
        dialog.show();
    }

    private void loadRows() {
        b.progress.setVisibility(View.VISIBLE);
        c.produkRepository.getHargaProduk(idProduk, result -> {
            b.progress.setVisibility(View.GONE);
            List<HargaProduk> list = result.isSuccess() && result.data != null ? result.data : new ArrayList<HargaProduk>();
            if (list.isEmpty()) {
                Images.asset(b.emptyImage, "empty.png");
                b.emptyRows.setVisibility(View.VISIBLE);
                return;
            }
            LayoutInflater inflater = LayoutInflater.from(b.getRoot().getContext());
            for (HargaProduk h : list) {
                Row row = new Row();
                row.h = h;
                row.b = ItemProductPilihanSatuanBinding.inflate(inflater, b.rows, false);
                bindRow(row);
                b.rows.addView(row.b.getRoot());
                rows.add(row);
            }
        });
    }

    private void bindRow(Row row) {
        Context ctx = row.b.getRoot().getContext();
        HargaProduk h = row.h;
        if (!h.promo()) {
            row.b.title.setText(h.nama);
            SpannableStringBuilder sb = new SpannableStringBuilder(Currency.formatRupiah(h.harga - h.diskon, 0) + " ");
            if (h.diskon >= 1) {
                int start = sb.length();
                sb.append(" ").append(Currency.formatRupiah(h.harga, 0));
                sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.text_muted)), start, sb.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new StrikethroughSpan(), start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            row.b.subtitle.setText(sb);
            if (h.jenisHarga != null && h.jenisHarga.toLowerCase().equals("tidak pas")) {
                row.b.quantityBox.setVisibility(View.VISIBLE);
                row.b.quantity.setText(String.valueOf(row.quantity));
                row.b.quantityBox.setOnClickListener(v -> {
                    PopupMenu menu = new PopupMenu(ctx, row.b.quantityBox);
                    for (int q = 1; q <= 10; q++) {
                        menu.getMenu().add(0, q, q, String.valueOf(q));
                    }
                    menu.setOnMenuItemClickListener(item -> {
                        row.quantity = item.getItemId();
                        row.b.quantity.setText(String.valueOf(row.quantity));
                        select(row);
                        return true;
                    });
                    menu.show();
                });
            } else {
                row.b.hargaPas.setVisibility(View.VISIBLE);
            }
        } else {
            row.b.title.setText(h.namaPromo);
            int bersih = h.hargaSatuan - h.diskonSatuan - h.diskonPromo;
            SpannableStringBuilder sb = new SpannableStringBuilder(h.namaSatuan + "  ");
            int greenStart = sb.length();
            sb.append("  ").append(Currency.formatRupiah(bersih, 0));
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.green)), greenStart, sb.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), greenStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            int strike = sb.length();
            sb.append("  ").append(Currency.formatRupiah(h.hargaSatuan, 0)).append(" - ");
            sb.setSpan(new StrikethroughSpan(), strike, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            row.b.subtitle.setText(sb);
            row.b.subtitle.setTextColor(ContextCompat.getColor(ctx, R.color.text_muted));
            row.b.maxOrder.setVisibility(View.VISIBLE);
            row.b.maxOrder.setText(ctx.getString(R.string.product_max_order, h.maxOrder));
        }
        row.b.getRoot().setOnClickListener(v -> select(row));
    }

    private void select(Row row) {
        selected = row;
        for (Row r : rows) {
            r.b.radio.setChecked(r == row);
        }
        b.beli.setVisibility(View.VISIBLE);
    }

    private void onBeli() {
        Row row = selected;
        if (row == null || adding) {
            return;
        }
        adding = true;
        b.beli.setEnabled(false);
        String idSatuan = row.h.id;
        String quantity = String.valueOf(row.quantity);
        String idPromo = idPromoArg;
        if (row.h.promo()) {
            idSatuan = row.h.idSatuanProduk;
            idPromo = row.h.idPromo;
            quantity = "1";
        }
        c.cartRepository.addToCart(idProduk, idSatuan, quantity, idPromo, result -> {
            dialog.dismiss();
            if (!fragment.isAdded()) {
                return;
            }
            AddToCartResult r = result.isSuccess() ? result.data : null;
            if (r != null && r.statusAddCart) {
                Messages.cartSnack(fragment, fragment.getString(R.string.product_berhasil_keranjang));
                c.cartRepository.refreshCartCount();
            } else {
                String msg = fragment.getString(R.string.product_gagal_keranjang);
                if (r != null && r.message != null && !r.message.isEmpty()) {
                    msg = msg + "\n" + r.message;
                }
                Messages.info(fragment, fragment.getString(R.string.product_keranjang), msg, null);
            }
        });
    }
}
