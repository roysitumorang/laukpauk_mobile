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
import id.laukpauk.data.model.product.ProductPrice;
import id.laukpauk.data.model.product.Product;
import id.laukpauk.data.model.product.UnitSheetData;
import id.laukpauk.databinding.ItemProductUnitOptionsBinding;
import id.laukpauk.databinding.SheetProductUnitBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.util.Currency;
import id.laukpauk.util.ServerPath;

/**
 * The "Pilihan Satuan" bottom sheet (legacy ShowProductUnit + PilihanSatuanProduk + PilihanSatuanProdukItem):
 * pick a unit (and quantity) of a product and add it to the cart.
 */
final class ShowProductUnit {

    private final Fragment fragment;
    private final AppContainer c;
    private final String productId;
    @Nullable private final String idPromoArg;
    private final BottomSheetDialog dialog;
    private final SheetProductUnitBinding b;
    private final List<Row> rows = new ArrayList<>();
    @Nullable private Row selected;
    private boolean adding;

    private static final class Row {
        ProductPrice h;
        ItemProductUnitOptionsBinding b;
        int quantity = 1;

        String key() {
            return h.promo() ? "promo_" + h.idPromo : h.id;
        }
    }

    static void show(Fragment fragment, String productId, UnitSheetData data, @Nullable String idPromo) {
        new ShowProductUnit(fragment, productId, data, idPromo);
    }

    private ShowProductUnit(Fragment fragment, String productId, UnitSheetData data, @Nullable String idPromo) {
        this.fragment = fragment;
        this.c = AppContainer.from(fragment.requireContext());
        this.productId = productId;
        this.idPromoArg = idPromo;
        Context ctx = fragment.requireContext();
        dialog = new BottomSheetDialog(ctx);
        b = SheetProductUnitBinding.inflate(LayoutInflater.from(ctx));
        dialog.setContentView(b.getRoot());

        Product p = data.product;
        if (p != null) {
            b.name.setText(p.name);
            b.category.setText(p.productCategoryName != null ? p.productCategoryName
                    : ctx.getString(R.string.product_category_unknown));
            Images.circle(b.photo, Images.server(ServerPath.uploadImageProduct + "/compress", p.photo == null ? "" : p.photo));
        }
        b.noteOutsideCa.setVisibility(data.inCoverageArea ? View.GONE : View.VISIBLE);
        b.noteOutsideCaText.setText(ctx.getString(R.string.product_note_outside_ca, data.shippingFeePerKm));
        b.buy.setOnClickListener(v -> onBuy());

        boolean stock = p == null || p.isStockAvailable();
        if (stock) {
            loadRows();
        } else {
            b.outOfStock.setVisibility(View.VISIBLE);
        }
        dialog.show();
    }

    private void loadRows() {
        b.progress.setVisibility(View.VISIBLE);
        c.productRepository.getProductPrice(productId, result -> {
            b.progress.setVisibility(View.GONE);
            List<ProductPrice> list = result.isSuccess() && result.data != null ? result.data : new ArrayList<ProductPrice>();
            if (list.isEmpty()) {
                Images.asset(b.emptyImage, "empty.png");
                b.emptyRows.setVisibility(View.VISIBLE);
                return;
            }
            LayoutInflater inflater = LayoutInflater.from(b.getRoot().getContext());
            for (ProductPrice h : list) {
                Row row = new Row();
                row.h = h;
                row.b = ItemProductUnitOptionsBinding.inflate(inflater, b.rows, false);
                bindRow(row);
                b.rows.addView(row.b.getRoot());
                rows.add(row);
            }
        });
    }

    private void bindRow(Row row) {
        Context ctx = row.b.getRoot().getContext();
        ProductPrice h = row.h;
        if (!h.promo()) {
            row.b.title.setText(h.name);
            SpannableStringBuilder sb = new SpannableStringBuilder(Currency.formatRupiah(h.price - h.discount, 0) + " ");
            if (h.discount >= 1) {
                int start = sb.length();
                sb.append(" ").append(Currency.formatRupiah(h.price, 0));
                sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.text_muted)), start, sb.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new StrikethroughSpan(), start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            row.b.subtitle.setText(sb);
            if (h.priceType != null && h.priceType.toLowerCase().equals("tidak pas")) {
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
                row.b.exactPrice.setVisibility(View.VISIBLE);
            }
        } else {
            row.b.title.setText(h.promoName);
            int net = h.unitPrice - h.unitDiscount - h.promoDiscount;
            SpannableStringBuilder sb = new SpannableStringBuilder(h.unitName + "  ");
            int greenStart = sb.length();
            sb.append("  ").append(Currency.formatRupiah(net, 0));
            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.green)), greenStart, sb.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), greenStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            int strike = sb.length();
            sb.append("  ").append(Currency.formatRupiah(h.unitPrice, 0)).append(" - ");
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
        b.buy.setVisibility(View.VISIBLE);
    }

    private void onBuy() {
        Row row = selected;
        if (row == null || adding) {
            return;
        }
        adding = true;
        b.buy.setEnabled(false);
        String unitId = row.h.id;
        String quantity = String.valueOf(row.quantity);
        String idPromo = idPromoArg;
        if (row.h.promo()) {
            unitId = row.h.productUnitId;
            idPromo = row.h.idPromo;
            quantity = "1";
        }
        c.cartRepository.addToCart(productId, unitId, quantity, idPromo, result -> {
            dialog.dismiss();
            if (!fragment.isAdded()) {
                return;
            }
            AddToCartResult r = result.isSuccess() ? result.data : null;
            if (r != null && r.statusAddCart) {
                Messages.cartSnack(fragment, fragment.getString(R.string.product_cart_success));
                c.cartRepository.refreshCartCount();
            } else {
                String msg = fragment.getString(R.string.product_cart_failed);
                if (r != null && r.message != null && !r.message.isEmpty()) {
                    msg = msg + "\n" + r.message;
                }
                Messages.info(fragment, fragment.getString(R.string.product_cart), msg, null);
            }
        });
    }
}
