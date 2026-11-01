package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.R;
import id.laukpauk.core.AppState;
import id.laukpauk.core.AuthModel;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.model.ADJDModel;
import id.laukpauk.model.CheckoutModel;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.model.HomepageModel;
import id.laukpauk.model.KodePromoModel;
import id.laukpauk.model.PointModel;
import id.laukpauk.model.ProfilSayaModel;
import id.laukpauk.model.SettingModel;
import id.laukpauk.model.SplashScreenModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.CustomForm;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.SplashScreenActivity;
import id.laukpauk.view.pembeli.components.checkout.CheckoutGlobalState;
import id.laukpauk.view.pembeli.components.checkout.CheckoutUi;
import id.laukpauk.view.pembeli.components.checkout.JadwalPengiriman;
import id.laukpauk.view.pembeli.components.checkout.RingkasanPembayaran;
import id.laukpauk.view.pembeli.components.checkout.SubTotal;

/**
 * Port of lib/views/pembeli/Checkout.dart. No arguments.
 * The Dart State rebuilt its whole tree on setState; here the page is built once when the data is ready and
 * {@link #refreshAll()} recomputes the totals and updates SubTotal / RingkasanPembayaran.
 */
public class CheckoutActivity extends BaseActivity {
    private static final double FORM_BORDER_WIDTH = .5;
    private static final int FORM_BORDER_COLOR = CustomColors.green;

    private EditText namaPenerimaController, nomorHPPenerimaController, alamatPenerimaController,
            catatanTambahanController;

    private Boolean kodePromoStatusState;
    private String cartCode = "";
    private String kodePromoState = "";
    private String kodePromoMessageState;
    private long kodePromoDiskonState = 0;
    private int kodePromoRequestSeq = 0;
    private JsonObject usedVoucherState, jadwalDeliverySelected;

    private JsonObject walletConfigurationState;
    private long totalOngkirState = 0, totalBelanjaState = 0, totalDompetSayaState = 0;
    private long jumlahBarangState = 0;
    private boolean isReadyToShow = false;
    private String penjelasanOngkirState, statusVoucherState;
    private JsonObject jenisOngkirState;

    private boolean isPointUsedState = false;
    private long pointState = 0;
    private double totalDapatPointState = 0;
    private long minimalPointTukarPointState = 0;
    private JsonElement diskonKategoriState = null; // Dart: false = none

    private JsonObject detailUserState;

    private boolean pageBuilt = false;
    private CheckoutGlobalState cgs;
    private JsonObject dataRingkasanPembayaran;
    private SubTotal subTotal;
    private RingkasanPembayaran ringkasanPembayaran;
    private SwipeRefreshLayout refresh;

    /** Result of the background part of _dataInitialization(). */
    private static final class InitData {
        boolean enough;          // totalBelanja >= 1
        boolean complete;        // jenisOngkir != null (Dart only shows the page then)
        long totalBelanja, totalOngkir, jumlahBarang, totalDompetSaya, point, minimalPointTukarPoint;
        JsonObject walletConfiguration, jenisOngkir, detailUser;
        JsonElement diskonKategori;
        String penjelasanOngkir = "", cartCode = "";
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        namaPenerimaController = CustomForm.controller(this);
        nomorHPPenerimaController = CustomForm.controller(this);
        alamatPenerimaController = CustomForm.controller(this);
        catatanTambahanController = CustomForm.controller(this);

        setRoot(Loading.view(this));
        initializeState();
    }

    // ------------------------------------------------------------------ _initializeState

    private void initializeState() {
        async(() -> {
            if (!AuthModel.isLogin()) {
                return 0;
            }
            if (!LocalStorage.isUserVerified()) {
                return 1;
            }
            if (!SplashScreenModel.hasOTPConfirmed()) {
                return 2;
            }
            if (!CheckoutModel.isJadwalDeliveryOpened()) {
                return 3;
            }
            return 4;
        }, (Integer step) -> {
            JsonObject routeBack = Json.of("routeToClass", CheckoutActivity.class.getName());
            switch (step) {
                case 0:
                    Nav.pushAndRemoveUntil(this, LoginActivity.class, routeBack);
                    break;
                case 1:
                    Nav.pushAndRemoveUntil(this, OTPVerifikasiUserActivity.class, routeBack);
                    break;
                case 2:
                    Nav.pushAndRemoveUntil(this, OTPNotCompletedActivity.class, routeBack);
                    break;
                case 3:
                    jadwalDitutup();
                    break;
                default:
                    dataInitialization();
                    break;
            }
        });
    }

    private void jadwalDitutup() {
        Modal.Content<Boolean> actions = handle -> CustomAlert.actionsRow(this,
                CheckoutUi.textButton(this, "Pilih Jadwal Lain", CustomColors.textSuccess,
                        () -> handle.close(Boolean.TRUE)),
                CheckoutUi.textButton(this, "Lain Kali", CustomColors.textMuted,
                        () -> handle.close(Boolean.FALSE)));
        CustomAlert.<Boolean>textComponent(this, null, "Jadwal Pengantaran",
                "Yah, jadwal pengantarannya sudah ditutup mak! Pilih jadwal pengantaran lainnya yuk ...",
                actions, null, false, (Boolean konfirmasi) -> {
                    if (konfirmasi != null && konfirmasi) {
                        async(() -> {
                            ADJDModel.deleteAlamatDanJadwalDelivery();
                            return Boolean.TRUE;
                        }, ok -> HeaderModel.adjd(this, null, null, null, (JsonObject newADJD) -> {
                            String latitude = Json.strOrNull(newADJD, "latitude");
                            String longitude = Json.strOrNull(newADJD, "longitude");
                            String kodeJadwalDelivery = Json.strOrNull(newADJD, "kodeJadwalDelivery");
                            if (latitude != null && longitude != null && kodeJadwalDelivery != null) {
                                AppState.get().setADJDSet(true);
                                Nav.pushAndRemoveUntil(this, SplashScreenActivity.class, null);
                            }
                        }));
                    } else {
                        Nav.pushAndRemoveUntil(this, SplashScreenActivity.class, null);
                    }
                });
    }

    // ------------------------------------------------------------------ _dataInitialization

    private void dataInitialization() {
        async(() -> {
            InitData d = new InitData();
            long totalBelanja = CheckoutModel.getTotalKeranjang();
            d.totalBelanja = totalBelanja;
            if (totalBelanja >= 1) {
                d.enough = true;
                JsonElement walletConfiguration = CheckoutModel.getWalletConfiguration();
                d.walletConfiguration = (walletConfiguration != null && walletConfiguration.isJsonObject())
                        ? walletConfiguration.getAsJsonObject() : null;
                d.totalOngkir = CheckoutModel.getTotalOngkir();
                d.jumlahBarang = CheckoutModel.getJumlahCart();
                JsonElement jenisOngkirElement = CheckoutModel.getJenisOngkir();
                JsonObject jenisOngkir = (jenisOngkirElement != null && jenisOngkirElement.isJsonObject())
                        ? jenisOngkirElement.getAsJsonObject() : null;
                d.diskonKategori = CheckoutModel.getDiskonKategori();
                String penjelasanOngkir = "";

                d.totalDompetSaya = HomepageModel.getTotalDompet();

                d.detailUser = Json.obj(ProfilSayaModel.getDetailActiveUser("nama, nomorHP, alamat"));

                d.cartCode = LocalStorage.getCartCode();

                if (jenisOngkir != null) {
                    if (Json.str(jenisOngkir, "jenisOngkir").equals("perpasar")) {
                        if (Json.intVal(jenisOngkir, "banyakPasar") >= 2) {
                            long tarifOngkirPerPasar = Json.asLong(SettingModel.getSettingValue("costDelivery"), 0);
                            long banyakPasar = Json.longVal(jenisOngkir, "banyakPasar");

                            penjelasanOngkir = "Anda dikenakan  " + Currency.formatSatuan(banyakPasar)
                                    + "x ongkos kirim karena belanja dari "
                                    + Currency.formatSatuan(banyakPasar, "Pasar")
                                    + ". Tarif ongkir perpasar adalah "
                                    + Currency.formatRupiah(tarifOngkirPerPasar, 0)
                                    + ". Disarankan belanja dari 1 pasar.";
                        }
                    } else {
                        String namaPasarTerjauh = Json.strOrNull(jenisOngkir, "namaPasarTerjauh");
                        if (namaPasarTerjauh == null) {
                            namaPasarTerjauh = "Pasar Tidak Dikenal";
                        }
                        long tarifOngkirPerKM = Json.asLong(SettingModel.getSettingValue("costPerKm"), 0);
                        penjelasanOngkir = "Anda dikenakan ongkir PERKM karena anda sudah berbelanja di luar "
                                + "Coverage Area anda. Jarak yang harus ditempuh kurir adalah "
                                + Currency.formatSatuan(Json.longVal(jenisOngkir, "jarakPasarTerjauh"), "KM")
                                + " (" + namaPasarTerjauh.toUpperCase() + ")"
                                + ". Tarif ongkir per km adalah " + Currency.formatRupiah(tarifOngkirPerKM, 0);
                    }

                    JsonElement getPoint = PointModel.getPoint("point=true");
                    d.point = (getPoint != null && getPoint.isJsonObject() && !Json.isFalse(getPoint))
                            ? Json.longVal(getPoint.getAsJsonObject(), "point") : 0;

                    JsonElement minimalPointTukarPoint = SettingModel.getSettingValue("minimalPointTukarPoint");
                    if (minimalPointTukarPoint != null && !minimalPointTukarPoint.isJsonNull()
                            && !Json.isFalse(minimalPointTukarPoint)) {
                        d.minimalPointTukarPoint = Json.asLong(minimalPointTukarPoint, 0);
                    }

                    d.jenisOngkir = jenisOngkir;
                    d.penjelasanOngkir = penjelasanOngkir;
                    d.complete = true;
                }
            }
            return d;
        }, (InitData d) -> {
            if (!d.enough) {
                Modal.Content<Object> actions = handle -> CustomAlert.actionsRow(this,
                        CustomButton.button(this, "OK", () -> {
                            handle.close();
                            Nav.pushAndRemoveUntil(this, HomePageActivity.class, null);
                        }));
                CustomAlert.<Object>textComponent(this, null, "Checkout",
                        "Anda tidak dapat chekout karena total belanjaan anda kurang dari pembelian minimal!",
                        actions, null, false, null);
                return;
            }
            if (!d.complete) {
                return; // Dart: nothing is set, the loading view stays
            }
            cartCode = d.cartCode;
            totalDompetSayaState = d.totalDompetSaya;
            walletConfigurationState = d.walletConfiguration;
            totalOngkirState = d.totalOngkir;
            totalBelanjaState = d.totalBelanja;
            jumlahBarangState = d.jumlahBarang;
            isReadyToShow = true;
            jenisOngkirState = d.jenisOngkir;
            penjelasanOngkirState = d.penjelasanOngkir;

            pointState = d.point;
            detailUserState = d.detailUser;

            namaPenerimaController.setText(Json.str(d.detailUser, "nama"));
            nomorHPPenerimaController.setText(Json.str(d.detailUser, "nomorHP"));
            alamatPenerimaController.setText(Json.str(d.detailUser, "alamat"));

            minimalPointTukarPointState = d.minimalPointTukarPoint;
            diskonKategoriState = d.diskonKategori;

            if (!pageBuilt) {
                buildPage();
            }
            refreshAll();
        });
    }

    // ------------------------------------------------------------------ build()

    /** The computation block at the top of Dart's build(): fills `cgs` and `dataRingkasanPembayaran`. */
    private void recompute() {
        String namaPenerima = namaPenerimaController.getText().toString().trim();
        String nomorHPPenerima = nomorHPPenerimaController.getText().toString().trim();
        String alamatDetailPenerima = alamatPenerimaController.getText().toString().trim();

        boolean isFormCheckoutCompleted = namaPenerima.length() >= 1 && nomorHPPenerima.length() >= 1
                && alamatDetailPenerima.length() >= 1;

        long totalBelanja = totalBelanjaState;
        long totalOngkir = totalOngkirState;
        long totalOngkirNormal = totalOngkirState;

        boolean isSelectedVoucherNull = (usedVoucherState == null);
        boolean isWalletConfigurationNull = (walletConfigurationState == null);

        long nilaiVoucher = 0;
        if (!isSelectedVoucherNull) {
            if (statusVoucherState != null) {
                if (statusVoucherState.toLowerCase().equals("aktif")) {
                    nilaiVoucher = Json.longVal(usedVoucherState, "nilai");
                }
            }
        }

        boolean hasDiskon = diskonKategoriState != null && diskonKategoriState.isJsonObject();
        long nilaiDiskonKategori = hasDiskon
                ? Json.longVal(diskonKategoriState.getAsJsonObject(), "diskon") : 0;

        long potonganWallet = 0;
        long minGrandTotalTransactionForWallet = 0;
        boolean isWalletOn = false;
        boolean walletDibatalkan = false;
        if (!isWalletConfigurationNull) {
            isWalletOn = Json.bool(walletConfigurationState, "isWalletOn");

            if (isWalletOn) {
                minGrandTotalTransactionForWallet = Json.longVal(walletConfigurationState,
                        "minGrandTotalTransactionForWallet");
                if (totalBelanjaState >= minGrandTotalTransactionForWallet) {
                    long potonganDompetConfig = Json.longVal(walletConfigurationState, "walletDiscount");
                    if (totalDompetSayaState >= potonganDompetConfig) {
                        potonganWallet = potonganDompetConfig;
                    } else {
                        walletDibatalkan = true;
                    }
                }
            }
        }

        long subTotalValue = totalBelanjaState; // belum termasuk potongan wallet, ongkir dan potongan voucher
        long subTotalBersih = subTotalValue - potonganWallet - nilaiVoucher - nilaiDiskonKategori;

        long nilaiPointOut = isPointUsedState ? pointState : 0;
        if (nilaiPointOut >= subTotalBersih) {
            nilaiPointOut = subTotalBersih;
        }

        long totalKeseluruhan = (totalBelanja + totalOngkir)
                - (nilaiVoucher + potonganWallet + nilaiPointOut + nilaiDiskonKategori + kodePromoDiskonState);

        JsonObject data = new JsonObject();
        data.addProperty("isReadyToShow", isReadyToShow);
        if (usedVoucherState != null) {
            data.add("usedVoucher", usedVoucherState);
        }
        if (walletConfigurationState != null) {
            data.add("walletConfigurationState", walletConfigurationState);
        }
        data.addProperty("jumlahBarang", jumlahBarangState);
        data.addProperty("totalBelanja", subTotalValue);
        data.addProperty("totalOngkir", totalOngkir);
        data.addProperty("totalOngkirNormal", totalOngkirNormal);
        if (jenisOngkirState != null) {
            data.add("jenisOngkir", jenisOngkirState);
        }
        data.addProperty("penjelasanOngkir", penjelasanOngkirState);
        if (statusVoucherState != null) {
            data.addProperty("statusVoucher", statusVoucherState);
        }
        data.addProperty("isWalletOn", isWalletOn);
        data.addProperty("potonganWallet", potonganWallet);
        data.addProperty("walletDibatalkan", walletDibatalkan);
        data.addProperty("totalKeseluruhan", totalKeseluruhan);
        dataRingkasanPembayaran = data;

        cgs = new CheckoutGlobalState(cartCode, isPointUsedState, isWalletOn, isFormCheckoutCompleted,
                totalKeseluruhan, pointState, subTotalValue, potonganWallet, nilaiVoucher, totalOngkir,
                detailUserState, catatanTambahanController.getText().toString().trim(), usedVoucherState,
                jadwalDeliverySelected, totalDapatPointState, minimalPointTukarPointState,
                diskonKategoriState, nilaiPointOut, kodePromoState, kodePromoStatusState, kodePromoDiskonState,
                kodePromoMessageState);
    }

    /** setState(() {}): recompute and update the parts that depend on the totals. */
    private void refreshAll() {
        if (!pageBuilt) {
            return;
        }
        recompute();
        subTotal.update(cgs);
        ringkasanPembayaran.update(dataRingkasanPembayaran, cgs);
    }

    /** Only the form changed (name / phone / address / note): the Bayar bar is the only consumer. */
    private void refreshForm() {
        if (!pageBuilt) {
            return;
        }
        recompute();
        subTotal.update(cgs);
    }

    private View appBar() {
        final BaseActivity a = this;
        LinearLayout bar = Ui.column(a);
        bar.setBackgroundColor(CustomColors.green);
        bar.setElevation(Ui.dp(a, 4));
        Layouts.tintStatusBar(a, CustomColors.green);

        FrameLayout toolbar = new FrameLayout(a);
        bar.addView(toolbar, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(a, 56)));

        TextView titleView = Ui.text(a, "Checkout", 20f, CustomColors.white);
        titleView.setSingleLine(true);
        titleView.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams titleLp = new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP,
                Gravity.CENTER_VERTICAL);
        titleLp.leftMargin = Ui.dp(a, 56);
        titleLp.rightMargin = Ui.dp(a, 56);
        toolbar.addView(titleView, titleLp);

        FrameLayout back = new FrameLayout(a);
        ImageView icon = Ui.icon(a, R.drawable.ic_arrow_back, 24, CustomColors.white);
        back.addView(icon, new FrameLayout.LayoutParams(Ui.dp(a, 24), Ui.dp(a, 24), Gravity.CENTER));
        Layouts.ripple(back, 0x33FFFFFF, 28f);
        back.setOnClickListener(v -> pop());
        toolbar.addView(back, new FrameLayout.LayoutParams(Ui.dp(a, 56), Ui.dp(a, 56),
                Gravity.LEFT | Gravity.CENTER_VERTICAL));
        return bar;
    }

    private View formField(String hint, EditText controller) {
        CustomForm.TextFieldOpts o = new CustomForm.TextFieldOpts().backgroundColor(CheckoutUi.FIELD_FILL)
                .useLabel(true).borderWidth(FORM_BORDER_WIDTH).borderColor(FORM_BORDER_COLOR);
        return CustomForm.textField(this, hint, controller, o);
    }

    private void buildPage() {
        pageBuilt = true;
        recompute();
        float hb = Ui.hb(this);

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);
        root.addView(appBar(), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        subTotal = new SubTotal(this, this, cgs);
        root.addView(subTotal, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        final LinearLayout inner = Ui.column(this);
        Ui.pad(inner, 20, 20, 20, 20);

        inner.addView(Ui.text(this, "Checkout", hb * 4, 0xDD000000, true));
        inner.addView(Ui.spaceH(this, 5));
        inner.addView(Ui.text(this, "Silahkan lengkapi data - data berikut ini untuk memproses orderan",
                Txt.textXS(this)));
        View sep = Ui.divider(this, CustomColors.green, .5f);
        sep.setLayoutParams(CheckoutUi.lp(Ui.MATCH, Math.max(1, Ui.dp(this, .5f)), 0, Ui.dp(this, 15), 0,
                Ui.dp(this, 15)));
        inner.addView(sep);

        float space = 12.5f;
        inner.addView(formField("Nama Lengkap Penerima", namaPenerimaController),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        inner.addView(Ui.spaceH(this, space));
        inner.addView(formField("Nomor HP Penerima", nomorHPPenerimaController),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        inner.addView(Ui.spaceH(this, space));
        inner.addView(formField("Alamat Lengkap Penerima", alamatPenerimaController),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        inner.addView(Ui.spaceH(this, space));
        inner.addView(formField("Catatan Tambahan", catatanTambahanController),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        for (EditText e : new EditText[]{namaPenerimaController, nomorHPPenerimaController,
                alamatPenerimaController, catatanTambahanController}) {
            CheckoutUi.onTextChanged(e, this::refreshForm);
        }

        inner.addView(Ui.spaceH(this, 20));
        inner.addView(Ui.divider(this, 0xFFE0E0E0, 1), new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(this, 1)));
        inner.addView(Ui.spaceH(this, 20));
        inner.addView(new JadwalPengiriman(this, (JsonObject jadwalPengiriman) -> {
            jadwalDeliverySelected = jadwalPengiriman;
            refreshAll();
        }), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        inner.addView(Ui.spaceH(this, 20));

        ringkasanPembayaran = new RingkasanPembayaran(this, dataRingkasanPembayaran, cgs,
                this::onVoucherSelected, this::onVoucherRemoved,
                (Boolean checked) -> {
                    isPointUsedState = checked;
                    refreshAll();
                }, this::onKodePromoFilled);
        inner.addView(ringkasanPembayaran, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        inner.addView(Ui.spaceH(this, 5));

        // Card(elevation: 5, radius 10) inside Padding(horizontal 1.75 blocks, bottom 5 blocks)
        LinearLayout card = Layouts.card(this, CustomColors.nearlyWhite, 10, 5);
        card.addView(inner, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        LinearLayout wrap = Ui.column(this);
        Ui.pad(wrap, hb * 1.75f, 0, hb * 1.75f, hb * 5.0f);
        wrap.addView(card, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        refresh = id.laukpauk.view.pembeli.DetailUi.refreshable(this, wrap, () -> {
            initializeState();
            refresh.postDelayed(() -> refresh.setRefreshing(false), 400);
        }, false);
        refresh.setClipChildren(false);
        // Container(constraints: BoxConstraints(minHeight: constraints.maxHeight))
        refresh.addOnLayoutChangeListener((v, l, t, r, bt, ol, ot, or, ob) -> {
            int h = bt - t;
            if (h > 0 && inner.getMinimumHeight() != h) {
                inner.setMinimumHeight(h);
            }
        });
        root.addView(refresh, Ui.weightH(1f));

        setRoot(root);
    }

    // ------------------------------------------------------------------ callbacks of RingkasanPembayaran

    private void onKodePromoFilled(final String kodePromo) {
        final int seq = ++kodePromoRequestSeq;
        async(() -> KodePromoModel.cekKodePromo(kodePromo), (JsonObject checkKodePromo) -> {
            if (seq != kodePromoRequestSeq) {
                return; // a newer code was typed meanwhile
            }
            if (checkKodePromo != null) {
                boolean status = Json.bool(checkKodePromo, "status");
                String message = Json.strOrNull(checkKodePromo, "message");
                JsonObject data = Json.objectOrNull(checkKodePromo, "data");

                long kodePromoDiskon = 0;
                if (data != null) {
                    JsonObject promoDetail = Json.object(data, "promo");
                    kodePromoDiskon = Json.longVal(promoDetail, "diskon");
                }

                kodePromoState = kodePromo;
                kodePromoStatusState = status;
                kodePromoMessageState = message;
                kodePromoDiskonState = kodePromoDiskon;
            } else {
                kodePromoState = kodePromo;
            }
            refreshAll();
        });
    }

    private void onVoucherSelected(JsonObject voucherSelected, String statusVoucher) {
        long nilaiVoucher = Json.longVal(voucherSelected, "nilai");
        if (nilaiVoucher <= cgs.totalKeseluruhan) {
            usedVoucherState = voucherSelected;
            statusVoucherState = statusVoucher;
            refreshAll();
        } else {
            Modal.Content<Object> actions = handle -> CustomAlert.actionsRow(this,
                    CustomButton.button(this, "OK", handle::close,
                            new CustomButton.Opts().color(CustomColors.green)));
            CustomAlert.<Object>textComponent(this, null, "Voucher Tidak Dapat Digunakan",
                    "Voucher tidak dapat digunakan karena potongan voucher lebih besar dari total belanjaan anda!",
                    actions, null, false, null);
        }
    }

    private void onVoucherRemoved() {
        LocalStorage.clearUsedVoucher();
        usedVoucherState = null;
        statusVoucherState = null;
        refreshAll();
    }
}
