package id.laukpauk.view.pembeli.components.detailproduk;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.core.Async;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;

/**
 * Port of lib/views/pembeli/components/DetailProduk/PilihanSatuanProdukItem.dart: one selectable
 * price/unit row (a ListTile with a Radio) or one promo row.
 *
 * Constructor = Dart's positional (detailHargaProduk, satuanTerpilih, onSatuanSelected).
 * The parent calls {@link #setSatuanTerpilih(JsonObject)} when the selection changes (Dart: the parent
 * rebuilt the item with the new satuanTerpilih while the item kept its State = quantitySelected).
 */
public class PilihanSatuanProdukItem extends LinearLayout {
    private final JsonObject detailHargaProduk;
    private JsonObject satuanTerpilih;
    private final Async.Done<JsonObject> onSatuanSelected;

    // State of _PilihanSatuanProdukItemState
    private int quantitySelected = 1;

    public PilihanSatuanProdukItem(Context ctx, JsonObject detailHargaProduk,
                                   JsonObject satuanTerpilih, Async.Done<JsonObject> onSatuanSelected) {
        super(ctx);
        setOrientation(VERTICAL);
        this.detailHargaProduk = detailHargaProduk != null ? detailHargaProduk : new JsonObject();
        this.satuanTerpilih = satuanTerpilih;
        this.onSatuanSelected = onSatuanSelected;
        render();
    }

    /** The parent's new selection (null = nothing selected). */
    public void setSatuanTerpilih(JsonObject satuanTerpilih) {
        this.satuanTerpilih = satuanTerpilih;
        render();
    }

    private void selected(JsonObject hargaDanQuantity) {
        if (onSatuanSelected != null) {
            onSatuanSelected.run(hargaDanQuantity);
        }
    }

    private RadioButton radio(boolean checked) {
        RadioButton rb = new RadioButton(getContext());
        rb.setButtonTintList(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{0xFF2196F3, 0x8A000000}));
        rb.setMinWidth(0);
        rb.setMinHeight(0);
        rb.setPadding(0, 0, 0, 0);
        rb.setChecked(checked);
        rb.setClickable(false);
        rb.setFocusable(false);
        return rb;
    }

    /** ListTile(dense: true, leading, title, subtitle, trailing). */
    private LinearLayout tile(RadioButton leading, View title, View subtitle, View trailing,
                              Runnable onTap) {
        Context ctx = getContext();
        LinearLayout tile = new LinearLayout(ctx);
        tile.setOrientation(HORIZONTAL);
        tile.setGravity(Gravity.CENTER_VERTICAL);
        tile.setMinimumHeight(Ui.dp(ctx, 64));
        Ui.pad(tile, 16, 0, 16, 0);

        FrameLayout leadingBox = new FrameLayout(ctx);
        leadingBox.addView(leading, new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.CENTER));
        tile.addView(leadingBox, new LinearLayout.LayoutParams(Ui.dp(ctx, 40), Ui.WRAP));
        tile.addView(Ui.spaceW(ctx, 16));

        LinearLayout middle = Ui.column(ctx);
        middle.addView(title, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        middle.addView(subtitle, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        tile.addView(middle, Ui.weightW(1f));

        if (trailing != null) {
            tile.addView(Ui.spaceW(ctx, 16));
            tile.addView(trailing, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        }
        tile.setOnClickListener(v -> onTap.run());
        id.laukpauk.ui.Layouts.ripple(tile);
        return tile;
    }

    /** DropdownButton<int>(isDense: true, items 1..10) without the underline. */
    private View quantityDropdown(final JsonObject hargaProduk) {
        final Context ctx = getContext();
        final float hb = Ui.hb(ctx);
        final LinearLayout box = Ui.row(ctx);
        box.addView(Ui.text(ctx, String.valueOf(quantitySelected), hb * 3f, 0xDD000000));
        box.addView(Ui.icon(ctx, R.drawable.ic_arrow_drop_down, 24, 0xFF616161));
        Ui.padXY(box, 4, 4);
        box.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(ctx, box);
            for (int quantity = 1; quantity <= 10; quantity++) {
                menu.getMenu().add(0, quantity, quantity, String.valueOf(quantity));
            }
            menu.setOnMenuItemClickListener(item -> {
                int quantity = item.getItemId();
                JsonObject hargaDanQuantity = Json.of(
                        "idSatuan", Json.str(hargaProduk, "id"),
                        "quantity", String.valueOf(quantity));
                selected(hargaDanQuantity);

                quantitySelected = quantity;
                render();
                return true;
            });
            menu.show();
        });
        return box;
    }

    private void render() {
        removeAllViews();
        final Context ctx = getContext();
        final float hb = Ui.hb(ctx);
        final JsonObject hargaProduk = detailHargaProduk;

        boolean isPromo = hargaProduk.has("isPromo");

        String hargaTerpilih = (satuanTerpilih != null) ? Json.strOrNull(satuanTerpilih, "idSatuan") : null;

        if (!isPromo) {
            int harga = Json.intVal(hargaProduk, "harga");
            int diskon = Json.intVal(hargaProduk, "diskon");

            int hargaKotor = harga;
            harga = harga - diskon;

            final String id = Json.str(hargaProduk, "id");

            TextView title = Ui.text(ctx, Json.str(hargaProduk, "nama"),
                    new Txt.Style(hb * 3.75f).color(CustomColors.darkText));

            // RichText(text: TextSpan(text: harga + ' ', children: diskon >= 1 ? [struck-through gross price] : null))
            SpannableStringBuilder sb = new SpannableStringBuilder(Currency.formatRupiah(harga, 0) + " ");
            if (diskon >= 1) {
                int start = sb.length();
                sb.append(" ").append(Currency.formatRupiah(hargaKotor, 0));
                sb.setSpan(new ForegroundColorSpan(CustomColors.textMuted), start, sb.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new StrikethroughSpan(), start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            TextView subtitle = Ui.text(ctx, sb, Txt.textXS(ctx).color(CustomColors.darkGreen));
            Ui.pad(subtitle, 0, 7.5f, 0, 0);

            View trailing;
            if (Json.str(hargaProduk, "jenisHarga").toLowerCase(Locale.ROOT).equals("tidak pas")) {
                trailing = quantityDropdown(hargaProduk);
            } else {
                trailing = Ui.text(ctx, "1 Item (Harga Pas)", hb * 3f, 0xDD000000);
            }

            RadioButton leading = radio(hargaTerpilih != null && hargaTerpilih.equals(id));
            addView(tile(leading, title, subtitle, trailing, () -> {
                JsonObject hargaDanQuantity = Json.of(
                        "idSatuan", id,
                        "quantity", String.valueOf(quantitySelected));
                selected(hargaDanQuantity);
            }), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        } else {
            Txt.Style textStyle = new Txt.Style(hb * 3.0f).color(CustomColors.textMuted);

            String idSatuanProduk = Json.str(hargaProduk, "idSatuanProduk");
            String namaPromo = Json.str(hargaProduk, "namaPromo");
            String namaSatuan = Json.str(hargaProduk, "namaSatuan");
            String maxOrder = Json.str(hargaProduk, "maxOrder");
            int hargaSatuan = Json.intVal(hargaProduk, "hargaSatuan");
            int diskonSatuan = Json.intVal(hargaProduk, "diskonSatuan");
            int diskonPromo = Json.intVal(hargaProduk, "diskonPromo");
            String idPromo = Json.str(hargaProduk, "idPromo");

            int hargaKotor = hargaSatuan;
            int hargaBersihSatuan = hargaSatuan - diskonSatuan - diskonPromo;

            final String idSatuan = "promo_" + idPromo;

            final JsonObject hargaDanQuantity = Json.of(
                    "idSatuan", idSatuan,
                    "quantity", "1",
                    "isPromo", true,
                    "id", idSatuanProduk,
                    "idPromo", idPromo);

            TextView title = Ui.text(ctx, namaPromo,
                    new Txt.Style(hb * 3.75f).color(CustomColors.darkText));

            LinearLayout subtitle = Ui.column(ctx);
            subtitle.addView(Ui.spaceH(ctx, 5f));

            SpannableStringBuilder sb = new SpannableStringBuilder(namaSatuan + "  ");
            int greenStart = sb.length();
            sb.append("  ").append(Currency.formatRupiah(hargaBersihSatuan, 0));
            sb.setSpan(new ForegroundColorSpan(CustomColors.green), greenStart, sb.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), greenStart,
                    sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            int strikeStart = sb.length();
            sb.append("  ").append(Currency.formatRupiah(hargaKotor, 0)).append(" - ");
            sb.setSpan(new StrikethroughSpan(), strikeStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            subtitle.addView(Ui.text(ctx, sb, textStyle), new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));

            subtitle.addView(Ui.spaceH(ctx, 2.5f));
            // ListTile's default (dense) subtitle text: 12, black54
            subtitle.addView(Ui.text(ctx, "Max. Order " + maxOrder + "x", 12f, 0x8A000000),
                    new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));

            RadioButton leading = radio(hargaTerpilih != null && hargaTerpilih.equals(idSatuan));
            addView(tile(leading, title, subtitle, null, () -> selected(hargaDanQuantity)),
                    new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
    }
}
