package id.laukpauk.view.pembeli.components.keranjang;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;

import id.laukpauk.R;
import id.laukpauk.core.AppState;
import id.laukpauk.core.Async;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailPaketActivity;
import id.laukpauk.view.pembeli.DetailProdukActivity;
import id.laukpauk.view.pembeli.EditKeranjangItemActivity;
import id.laukpauk.view.penjual.PesananScreenUi;

/** Port of lib/views/pembeli/components/Keranjang/ProductPenjual.dart. */
public class ProductPenjual extends LinearLayout {
    private final JsonObject dataProduk;
    private final Async.Done<String> onRemoveItemSuccessed;
    private final Runnable onEditItemSuccessed;

    public ProductPenjual(Context ctx, JsonObject dataProduk, Async.Done<String> onRemoveItemSuccessed,
                          Runnable onEditItemSuccessed) {
        super(ctx);
        this.dataProduk = dataProduk;
        this.onRemoveItemSuccessed = onRemoveItemSuccessed;
        this.onEditItemSuccessed = onEditItemSuccessed;
        setOrientation(VERTICAL);
        Ui.pad(this, 0, 0, 0, 20);
        render();
    }

    private Activity activity() {
        return CartUi.activity(getContext());
    }

    private void openDetail() {
        String jenisProdukLC = Json.str(dataProduk, "jenisProduk").toLowerCase();
        if (jenisProdukLC.equals("nonpaket")) {
            Nav.push(getContext(), DetailProdukActivity.class, Json.of("id", Json.str(dataProduk, "idProduk")));
        }
        if (jenisProdukLC.equals("paket")) {
            Nav.push(getContext(), DetailPaketActivity.class, Json.of("idPaket", Json.str(dataProduk, "idProduk")));
        }
    }

    private void deleteProductPenjual(final String idCart, String namaProduk) {
        final Activity a = activity();
        if (a == null) {
            return;
        }
        String title = "Konfirmasi Hapus Keranjang!";
        String subTitle = "Apakah anda yakin akan menghapus produk " + namaProduk + " dari keranjang ?";
        Modal.Content<Object> actions = handle -> CustomAlert.actionsRow(a,
                CartUi.textButton(a, "Tidak", () -> handle.close()),
                CartUi.textButton(a, "Ya", () -> hapusCart(idCart, () -> handle.close())));
        CustomAlert.<Object>textComponent(a, null, title, subTitle, actions, null, false, null);
    }

    /** Dart: hapusCart(context, idCart); `after` runs once it is finished (Navigator.pop(context)). */
    private void hapusCart(final String idCart, final Runnable after) {
        PesananScreenUi.run(getContext(), () -> {
            Http.Response httpResponse = Http.postApi("/Keranjang/deleteCart/" + idCart,
                    new HashMap<String, String>());
            if (httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                boolean statusDeleteCart = Json.bool(dataObject, "statusDeleteCart");
                if (statusDeleteCart) {
                    return Integer.valueOf(HeaderModel.getJumlahCart());
                }
            }
            return null;
        }, (Integer jumlahKeranjang) -> {
            if (jumlahKeranjang != null) {
                AppState.get().setJumlahCart(jumlahKeranjang);
                if (onRemoveItemSuccessed != null) {
                    onRemoveItemSuccessed.run(idCart);
                }
            }
            after.run();
        });
    }

    /** Navigator.push(EditKeranjangItem(dataProduk)) and the follow-up alert when it reports a failure. */
    private void editItem(final String failSubTitle, final String failButton) {
        final Activity a = activity();
        if (!(a instanceof id.laukpauk.ui.BaseActivity)) {
            return;
        }
        Nav.pushForResult((id.laukpauk.ui.BaseActivity) a, EditKeranjangItemActivity.class,
                Json.of("dataKeranjangItem", dataProduk), (JsonElement result) -> {
                    if (result != null && !result.isJsonNull()) {
                        boolean statusEditKeranjangItem = Json.asBool(result, false);
                        if (statusEditKeranjangItem) {
                            if (onEditItemSuccessed != null) {
                                onEditItemSuccessed.run();
                            }
                        } else {
                            Modal.Content<Object> actions = handle -> CustomAlert.actionsRow(a,
                                    CartUi.textButton(a, failButton, () -> handle.close()));
                            CustomAlert.<Object>textComponent(a, null, "Keranjang", failSubTitle, actions, null,
                                    false, null);
                        }
                    }
                });
    }

    private void render() {
        removeAllViews();
        final Context ctx = getContext();
        float hb = Ui.hb(ctx);

        int harga = Json.parseInt(Json.str(dataProduk, "harga"), 0);
        int diskon = Json.parseInt(Json.str(dataProduk, "diskon"), 0);
        int quantity = Json.parseInt(Json.str(dataProduk, "quantity"), 0);
        long totalHarga = (long) (harga - diskon) * quantity;

        String jenisProduk = Json.str(dataProduk, "jenisProduk");
        String jenisProdukLC = jenisProduk.toLowerCase();
        String namaSatuan = jenisProdukLC.equals("paket") ? "Paket" : Json.str(dataProduk, "namaSatuan");
        String fotoProduk = Json.str(dataProduk, "fotoProduk");

        String imageURL;
        if (jenisProdukLC.equals("paket")) {
            imageURL = ServerPath.baseURL + "/" + ServerPath.uploadGambarPaket + "/compress/" + fotoProduk;
        } else {
            imageURL = ServerPath.baseURL + "/" + ServerPath.uploadGambarProduk + "/compress/" + fotoProduk;
        }

        String catatanTambahan = Json.str(dataProduk, "catatan");
        boolean adaCatatan = catatanTambahan.length() >= 1;

        LinearLayout top = new LinearLayout(ctx);
        top.setOrientation(HORIZONTAL);
        top.setGravity(Gravity.TOP);
        addView(top, new LayoutParams(Ui.MATCH, Ui.WRAP));

        ImageView image = Ui.network(ctx, imageURL, hb * 15, hb * 15);
        Ui.clipRounded(image, hb * 15 / 2f);
        Ui.onTap(image, this::openDetail);
        top.addView(image);
        top.addView(Ui.spaceW(ctx, 15));

        LinearLayout info = Ui.column(ctx);
        top.addView(info, Ui.weightW(1f));

        TextView nama = Ui.text(ctx, Json.str(dataProduk, "namaProduk"), hb * 4, CustomColors.textSuccess, true);
        Ui.onTap(nama, this::openDetail);
        info.addView(nama, new LayoutParams(Ui.WRAP, Ui.WRAP));
        info.addView(Ui.spaceH(ctx, 5));
        info.addView(Ui.text(ctx, Currency.formatRupiah(totalHarga, 0), hb * 3.5f, CustomColors.green, true));
        info.addView(Ui.spaceH(ctx, 5));

        LinearLayout qtyRow = new LinearLayout(ctx);
        qtyRow.setOrientation(HORIZONTAL);
        qtyRow.setGravity(Gravity.CENTER_VERTICAL);
        qtyRow.addView(Ui.text(ctx, Json.str(dataProduk, "quantity") + " x " + namaSatuan, hb * 3.25f,
                CustomColors.black));
        qtyRow.addView(Ui.spaceW(ctx, 5));
        ImageView pencil = Ui.icon(ctx, R.drawable.ic_edit, hb * 3.0f, CustomColors.textWarning);
        Ui.onTap(pencil, () -> editItem("Gagal mengubah item keranjang ! Silahkan coba lagi.", "Coba Lagi"));
        qtyRow.addView(pencil);
        info.addView(qtyRow);

        info.addView(Ui.spaceH(ctx, 2));
        info.addView(Ui.text(ctx, "@" + namaSatuan + " " + Currency.formatRupiah(harga - diskon, 0), hb * 3,
                CustomColors.black));
        if (!adaCatatan) {
            info.addView(Ui.spaceH(ctx, 10));
            TextView tambah = Ui.text(ctx, "Tambah Catatan", hb * 3.25f, CustomColors.textPrimary);
            Ui.onTap(tambah, () -> editItem("Gagal mengubah item keranjang! Coba lagi!", "OK"));
            info.addView(tambah, new LayoutParams(Ui.WRAP, Ui.WRAP));
        }

        top.addView(Ui.spaceW(ctx, 13));
        final String idCart = Json.str(dataProduk, "idCart");
        final String namaProduk = Json.str(dataProduk, "namaProduk");
        ImageView trash = Ui.icon(ctx, R.drawable.ic_delete, hb * 4, CustomColors.red);
        Ui.onTap(trash, () -> deleteProductPenjual(idCart, namaProduk));
        top.addView(trash);

        if (adaCatatan) {
            addView(Ui.spaceH(ctx, 10));
            LinearLayout box = Ui.column(ctx);
            box.setBackground(Ui.shape(ctx, 0x4466CC99, 15f));
            Ui.pad(box, 15, 10, 15, 10);
            addView(box, new LayoutParams(Ui.MATCH, Ui.WRAP));

            LinearLayout head = new LinearLayout(ctx);
            head.setOrientation(HORIZONTAL);
            head.setGravity(Gravity.CENTER_VERTICAL);
            TextView label = Ui.text(ctx, "Catatan Tambahan : ", hb * 3.5f, CustomColors.black, true);
            head.addView(label, Ui.weightW(1f));
            ImageView pen = Ui.icon(ctx, R.drawable.ic_edit, hb * 3.5f, CustomColors.textWarning);
            Ui.onTap(pen, () -> editItem("Gagal mengubah item keranjang! SIlahkan coba lagi!", "OK"));
            head.addView(pen);
            box.addView(head, new LayoutParams(Ui.MATCH, Ui.WRAP));
            box.addView(Ui.spaceH(ctx, 5));
            box.addView(Ui.text(ctx, catatanTambahan, hb * 3.25f, CustomColors.black));
        }
    }

    /** Kept for symmetry with the other cart components. */
    public void refresh() {
        render();
    }
}
