package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.AppState;
import id.laukpauk.core.AuthModel;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Http;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.model.CheckoutModel;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.keranjang.CartUi;
import id.laukpauk.view.pembeli.components.keranjang.ListKeranjang;

/** Port of lib/views/pembeli/Keranjang.dart (no constructor arguments). */
public class KeranjangActivity extends BaseActivity {
    private String removedItem = "";
    private boolean isAnyCart = false;
    private int updatedItemInt = 0;
    private boolean isUserLoggedInState = false;

    /** Identifies the latest build so that results of replaced FutureBuilders are ignored. */
    private int generation = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        render();
        initializationState();
    }

    /** Dart: getKeranjangPerPenjual(). Blocking. */
    private static JsonArray getKeranjangPerPenjual() {
        String cartCode = LocalStorage.getCartCode();
        String params = HTTPRequest.getQueryString("cartCode=" + cartCode);
        Http.Response httpResponse = Http.getApi("/User/getCartDividedBySeller?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "listCart");
        }
        return new JsonArray();
    }

    private void initializationState() {
        async(() -> {
            boolean loggedIn = AuthModel.isLogin();
            int jumlahKeranjang = HeaderModel.getJumlahCart();
            return new int[]{loggedIn ? 1 : 0, jumlahKeranjang};
        }, (int[] r) -> {
            isAnyCart = r[1] >= 1;
            isUserLoggedInState = r[0] == 1;
            render();
        });
    }

    private void render() {
        final int myGeneration = ++generation;
        float hb = Ui.hb(this);

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.lightGrey);
        root.addView(CartUi.appBar(this, "Keranjang", true), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout body = Ui.column(this);
        Ui.pad(body, 5, 5, 5, 5);
        root.addView(body, Ui.weightH(1f));

        if (isAnyCart) {
            LinearLayout card = CartUi.card(this, CustomColors.white, 10f, 8f, false);
            Ui.pad(card, 15, 20, 15, 20);
            body.addView(card);

            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.TOP);
            card.addView(top, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

            LinearLayout subtotal = Ui.column(this);
            subtotal.addView(Ui.text(this, "SUBTOTAL", hb * 5, CustomColors.black));
            final FrameLayout totalSlot = new FrameLayout(this);
            totalSlot.addView(CartUi.smallSpinner(this, 15), new FrameLayout.LayoutParams(Ui.dp(this, 15),
                    Ui.dp(this, 15), Gravity.CENTER));
            subtotal.addView(totalSlot, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            top.addView(subtotal, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            async(() -> Integer.valueOf(CheckoutModel.getTotalKeranjang()), (Integer totalKeranjang) -> {
                if (myGeneration != generation || totalKeranjang == null) {
                    return;
                }
                totalSlot.removeAllViews();
                totalSlot.addView(Ui.text(this, Currency.formatRupiah(totalKeranjang, 0), hb * 4,
                        CustomColors.green, true));
            });

            top.addView(Ui.spaceW(this, 30));
            top.addView(CustomButton.button(this, "Checkout", this::onCheckout,
                    new CustomButton.Opts().color(CustomButton.themeColor).outline().pill()), Ui.weightW(1f));

            final FrameLayout walletSlot = new FrameLayout(this);
            walletSlot.addView(CartUi.smallSpinner(this, 15), new FrameLayout.LayoutParams(Ui.dp(this, 15),
                    Ui.dp(this, 15), Gravity.CENTER));
            card.addView(walletSlot, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            async(() -> CheckoutModel.getWalletConfiguration(), (JsonElement walletConfiguration) -> {
                if (myGeneration != generation) {
                    return;
                }
                showWallet(walletSlot, walletConfiguration);
            });

            // Row(end): "Bersihkan Keranjang"
            LinearLayout clearRow = new LinearLayout(this);
            clearRow.setOrientation(LinearLayout.HORIZONTAL);
            clearRow.setGravity(Gravity.END);
            Ui.pad(clearRow, 5, 10, 5, 5);
            TextView clear = Ui.text(this, "Bersihkan Keranjang", hb * 3.5f, CustomColors.textDanger, true);
            Ui.onTap(clear, this::onBersihkan);
            clearRow.addView(clear);
            body.addView(clearRow, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        } else {
            LinearLayout clearRow = new LinearLayout(this);
            clearRow.setOrientation(LinearLayout.HORIZONTAL);
            Ui.pad(clearRow, 5, 10, 5, 5);
            body.addView(clearRow, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }

        final FrameLayout listSlot = new FrameLayout(this);
        listSlot.addView(Loading.view(this), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        body.addView(listSlot, Ui.weightH(1f));
        async(() -> getKeranjangPerPenjual(), (JsonArray keranjangPerPenjual) -> {
            if (myGeneration != generation || keranjangPerPenjual == null) {
                return;
            }
            listSlot.removeAllViews();
            listSlot.addView(new ListKeranjang(this, keranjangPerPenjual,
                    () -> initializationState(),
                    removedItemID -> async(() -> Integer.valueOf(HeaderModel.getJumlahCart()), (Integer jumlah) -> {
                        removedItem = removedItemID;
                        isAnyCart = jumlah >= 1;
                        render();
                    }),
                    () -> {
                        updatedItemInt++;
                        render();
                    }), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        });

        setRoot(root);
    }

    private void showWallet(FrameLayout slot, JsonElement walletConfiguration) {
        float hb = Ui.hb(this);
        if (walletConfiguration == null || walletConfiguration.isJsonNull()) {
            return; // snapshot.hasData is false: the indicator stays
        }
        slot.removeAllViews();
        if (Json.isFalse(walletConfiguration)) {
            TextView t = Ui.text(this, "Gagal mengambil data konfigurasi Dompet/Wallet", 14f, CustomColors.black);
            Ui.pad(t, 0, 15, 0, 0);
            slot.addView(t, new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            return;
        }
        JsonObject wallet = Json.obj(walletConfiguration);
        if (Json.bool(wallet, "isWalletOn")) {
            int potonganWallet = Json.parseInt(Json.str(wallet, "walletDiscount"), 0);
            int minimalTransaksiWallet = Json.parseInt(Json.str(wallet, "minGrandTotalTransactionForWallet"), 0);
            TextView t = Ui.text(this, "Potongan langsung dari dompet sebesar "
                    + Currency.formatRupiah(potonganWallet, 0) + " untuk setiap minimal belanja "
                    + Currency.formatRupiah(minimalTransaksiWallet, 0),
                    new id.laukpauk.ui.Txt.Style(hb * 3.25f).italic().color(CustomColors.black));
            t.setGravity(Gravity.CENTER);
            Ui.pad(t, 0, 15, 0, 0);
            slot.addView(t, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
    }

    private void onCheckout() {
        if (isAnyCart) {
            if (isUserLoggedInState) {
                Nav.push(this, CheckoutActivity.class, null);
            } else {
                Nav.push(this, LoginActivity.class, AuthRoute.carry(CheckoutActivity.class.getName(), null));
            }
        } else {
            Modal.Content<Object> actions = handle -> CustomAlert.actionsRow(this,
                    CartUi.textButton(this, "OK", () -> handle.close()));
            CustomAlert.<Object>textComponent(this, null, "Keranjang",
                    "Anda belum membeli item apapun, silahkan belanja untuk bisa checkout.", actions, null, false,
                    null);
        }
    }

    private void onBersihkan() {
        CheckoutModel.konfirmasi(this, (Boolean konfirmasiResult) -> {
            boolean konfirmasi = konfirmasiResult != null && konfirmasiResult;
            if (!konfirmasi) {
                return;
            }
            async(() -> Boolean.valueOf(CheckoutModel.bersihkanKeranjang()), (Boolean statusBersihkanKeranjang) -> {
                if (statusBersihkanKeranjang != null && statusBersihkanKeranjang) {
                    updatedItemInt++;
                    isAnyCart = false;
                    render();
                }
                async(() -> Integer.valueOf(HeaderModel.getJumlahCart()), (Integer jumlahKeranjang) ->
                        AppState.get().setJumlahCart(jumlahKeranjang));
            });
        });
    }
}
