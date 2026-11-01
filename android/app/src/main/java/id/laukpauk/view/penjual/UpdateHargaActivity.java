package id.laukpauk.view.penjual;

import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.R;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.penjual.components.updateharga.UpdateHargaItem;

/** Port of lib/views/penjual/UpdateHarga.dart (no arguments). */
public class UpdateHargaActivity extends BaseActivity {
    private String cariProdukStringState = "";
    private EditText cariProdukController;
    private FrameLayout hasilHolder;

    /** Dart getProduk(): the matching products when a search term was entered, else []. BLOCKING. */
    private JsonArray getProduk(String term) {
        if (term.length() >= 1) {
            String getProductEntities = "id, foto, nama, namaKategoriProduk";
            String params = HTTPRequest.getQueryString(
                    "getProductEntities=" + getProductEntities + "&term=" + Http.enc(term), true);

            Http.Response httpResponse = Http.getApi("/Penjual/getProdukPenjual?" + params);

            if (httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                return Json.array(dataObject, "produkPenjual");
            }
        }
        return new JsonArray();
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        float hb = Ui.hb(this);

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.aliceBlue);
        root.addView(DetailUi.appBar(this, "Update Harga Produk"));

        LinearLayout page = Ui.column(this);
        Ui.padXY(page, 5, 10);

        LinearLayout card = PenjualUi.cardPadded(this, 15, 20);
        card.addView(PenjualUi.text(this, "Update Harga Produk Massal", 4f, 0xDD000000, true));
        card.addView(Ui.spaceH(this, 5));
        card.addView(PenjualUi.text(this,
                "Isi kotak pencarian produk dan isi harga satuan - satuan dari produk tersebut", 3.5f, 0xDD000000,
                false));
        card.addView(Ui.spaceH(this, 15));

        // Material(color: 0x5566CC99, pill) > Padding(h10) > TextField(suffixIcon: search)
        LinearLayout searchBox = Ui.row(this);
        searchBox.setBackground(Ui.shape(this, 0x5566CC99, 50f));
        Ui.pad(searchBox, 10, 0, 10, 0);
        cariProdukController = new EditText(this);
        PenjualUi.styleEdit(this, cariProdukController, "Cari Produk Anda dengan Nama Produk",
                InputType.TYPE_CLASS_TEXT, hb * 3.5f, CustomColors.darkText, 0x99000000);
        cariProdukController.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        cariProdukController.setPadding(Ui.dp(this, 15), Ui.dp(this, 5), 0, Ui.dp(this, 5));
        cariProdukController.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                hideKeyboard();
                cariProdukStringState = cariProdukController.getText().toString();
                loadHasil();
                return true;
            }
            return false;
        });
        searchBox.addView(cariProdukController, Ui.weightW(1f));
        ImageView search = Ui.icon(this, R.drawable.ic_search, 14f, CustomColors.darkGreen);
        Ui.padAll(search, 17);
        search.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 48), Ui.dp(this, 48)));
        Ui.onTap(search, () -> cariProduk());
        searchBox.addView(search);
        card.addView(searchBox, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        page.addView(card, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        hasilHolder = new FrameLayout(this);
        page.addView(hasilHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(page, new ScrollView.LayoutParams(Ui.MATCH, Ui.WRAP));
        root.addView(scroll, Ui.weightH(1f));
        setRoot(root);

        loadHasil();
    }

    /** Dart cariProduk(). */
    private void cariProduk() {
        hideKeyboard();
        cariProdukStringState = cariProdukController.getText().toString();
        loadHasil();
    }

    /** The FutureBuilder under the search card (getProduk() re-runs on every rebuild). */
    private void loadHasil() {
        final String term = cariProdukStringState;
        if (term.length() >= 1 || hasilHolder.getChildCount() == 0) {
            hasilHolder.removeAllViews();
            hasilHolder.addView(PenjualUi.paddedSpinner(this, 0, 15), new FrameLayout.LayoutParams(Ui.MATCH, Ui.dp(this, 15),
                    Gravity.CENTER));
        }
        async(() -> getProduk(term), dataProduk -> {
            if (!term.equals(cariProdukStringState)) {
                return; // a newer search is on its way
            }
            hasilHolder.removeAllViews();
            if (dataProduk.size() >= 1) {
                LinearLayout column = Ui.column(this);
                for (JsonElement e : dataProduk) {
                    column.addView(new UpdateHargaItem(this, Json.obj(e)), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                }
                hasilHolder.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            } else {
                LinearLayout empty = PenjualUi.emptyState(this, 35f, 5f,
                        "Tidak ada data produk dengan nama " + cariProdukController.getText() + " !", 4f,
                        CustomColors.textDanger);
                Ui.padXY(empty, 0, 15);
                hasilHolder.addView(empty, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
        });
    }
}
