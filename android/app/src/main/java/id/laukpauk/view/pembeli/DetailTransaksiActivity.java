package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.ADJDModel;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.model.PesananSayaModel;
import id.laukpauk.model.SettingModel;
import id.laukpauk.model.TransaksiModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.detailtransaksi.ItemTransaksi;
import id.laukpauk.view.pembeli.components.detailtransaksi.StatusTransaksi;
import id.laukpauk.view.pembeli.components.detailtransaksi.TransaksiUi;

/** Port of lib/views/pembeli/DetailTransaksi.dart. Args: idTransaksi (String), idPenjual (String, optional). */
public class DetailTransaksiActivity extends BaseActivity {
    private String idTransaksi;
    private String idPenjual;

    private int reload = 0;
    private JsonObject rekeningData; // null until SettingModel answered
    private JsonObject detailTransaksi; // null = not loaded or Dart `false`
    private boolean detailLoaded = false;
    private boolean isButtonBelanjaUlangBusy = false;
    private CustomButton.Btn btnBelanjaUlang;

    private FrameLayout bodyHost;
    private FrameLayout contentFrame;
    private int requestSeq = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        idTransaksi = Json.str(args(), "idTransaksi");
        idPenjual = Json.strOrNull(args(), "idPenjual");

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);
        root.addView(DetailUi.appBar(this, "Detail Transaksi"), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        bodyHost = new FrameLayout(this);
        root.addView(bodyHost, Ui.weightH(1f));
        contentFrame = new FrameLayout(this);
        bodyHost.addView(contentFrame, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        setRoot(root);

        initialization();
        load(true);
    }

    // ------------------------------------------------------------------ data

    private void initialization() {
        async(() -> {
            String nomorRekeningLP = Json.asStr(SettingModel.getSettingValue("nomorRekeningLP"), "");
            String pemilikNomorRekeningLP = Json.asStr(SettingModel.getSettingValue("pemilikNomorRekeningLP"), "");
            return Json.of("nomorRekening", nomorRekeningLP, "pemilikNomorRekening", pemilikNomorRekeningLP);
        }, data -> {
            rekeningData = data;
            if (detailLoaded && detailTransaksi != null) {
                renderDetail();
            }
        });
    }

    private void load(boolean showLoading) {
        if (showLoading) {
            contentFrame.removeAllViews();
            contentFrame.addView(Loading.view(this), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        }
        final int seq = ++requestSeq;
        async(() -> TransaksiModel.getDetailTransaksi(idTransaksi), (JsonElement result) -> {
            if (seq != requestSeq) {
                return;
            }
            detailLoaded = true;
            detailTransaksi = (result != null && Json.isObject(result)) ? result.getAsJsonObject() : null;
            renderDetail();
        });
    }

    private void doReload() {
        reload++;
        load(false);
    }

    // ------------------------------------------------------------------ rendering

    private void renderDetail() {
        contentFrame.removeAllViews();
        if (detailTransaksi == null) {
            LinearLayout col = Ui.column(this);
            col.setGravity(Gravity.CENTER);
            col.addView(Ui.asset(this, "empty.png", Ui.hb(this) * 35));
            col.addView(Ui.spaceH(this, 15));
            col.addView(TransaksiUi.hbText(this, "Detail transaksi tidak ditemukan", 3.5f, CustomColors.textMuted,
                    false));
            contentFrame.addView(DetailUi.refreshable(this, col, () -> load(false), true),
                    new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            return;
        }

        LinearLayout list = Ui.column(this);
        Ui.padXY(list, 5, 5);
        list.addView(buildStatusCard(), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        list.addView(new ItemTransaksi(this, idTransaksi, idPenjual, false, Json.str(detailTransaksi, "cartCode")),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        list.addView(buildRincianCard(), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        contentFrame.addView(DetailUi.refreshable(this, list, () -> load(false), false),
                new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }

    private static int num(JsonObject o, String key) {
        return Json.parseInt(Json.str(o, key), 0);
    }

    private TextView ct(String s) {
        return TransaksiUi.content(this, s);
    }

    /** Row(spaceBetween)[label, value] with Padding(bottom: 7). */
    private View rincianRow(String label, String value, boolean bold) {
        LinearLayout wrap = Ui.column(this);
        Ui.pad(wrap, 0, 0, 0, 7);
        TextView l = TransaksiUi.content(this, label, CustomColors.nearlyBlack, bold, false, false);
        TextView v = TransaksiUi.content(this, value, CustomColors.nearlyBlack, bold, false, false);
        v.setGravity(Gravity.END);
        wrap.addView(TransaksiUi.spaceBetween(this, l, v), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        return wrap;
    }

    private View buildStatusCard() {
        final JsonObject d = detailTransaksi;
        final float hb = Ui.hb(this);

        int totalBelanja = num(d, "totalBelanja");
        int ongkir = num(d, "ongkir");
        int nilaiVoucher = num(d, "nilaiVoucher");
        int nilaiPotonganWallet = num(d, "potonganWallet");
        int nilaiPointOut = num(d, "pointOut");
        int nilaiDiskonKategori = num(d, "diskonKategori");
        int nilaiDiskonKodePromo = num(d, "diskonPromo");

        String status = Json.str(d, "status");
        String metodePembayaran = Json.str(d, "metodePembayaran");
        String namaMetodePembayaran = Json.str(d, "namaMetodePembayaran");
        final String nomorTransaksi = Json.str(d, "nomorTransaksi");
        String catatan = Json.str(d, "catatan");
        String statusReason = Json.str(d, "statusReason");

        final String statusLC = status.toLowerCase();
        boolean isRejected = statusLC.equals("rejected");
        String statusReadable = PesananSayaModel.readAbleStatusForUser(status).toUpperCase();

        final int refundedOngkir = num(d, "refundedOngkir");
        final int refundedUangBelanja = num(d, "refundedUangBelanja");

        int totalKeseluruhan = (totalBelanja + ongkir)
                - (nilaiVoucher + nilaiPotonganWallet + nilaiPointOut + nilaiDiskonKategori + nilaiDiskonKodePromo);

        boolean isBCA = false;
        int moreDataMetodePembayaran = 0;
        JsonObject transferBankData = null;
        if (metodePembayaran.toLowerCase().equals("tf_bca")) {
            moreDataMetodePembayaran = num(d, "moreDataMetodePembayaran");
            isBCA = true;

            JsonObject paymentResponse = Json.of(
                    "totalBayar", totalKeseluruhan,
                    "kodeUnik", moreDataMetodePembayaran,
                    "nomorRekening", rekeningData != null ? Json.str(rekeningData, "nomorRekening")
                            : "-- Nomor Rekenig --",
                    "pemilikNomorRekening", rekeningData != null ? Json.str(rekeningData, "pemilikNomorRekening")
                            : " -- Nama Pemilik --",
                    "idTransaksi", idTransaksi,
                    "nomorTransaksi", nomorTransaksi);
            JsonObject responseCheckout = Json.of("idTransaksi", idTransaksi, "paymentResponse", paymentResponse);
            transferBankData = Json.of("responseCheckout", responseCheckout);
        }

        LinearLayout card = TransaksiUi.card(this, 15, 10);

        // Row('Detail Transaksi', 'Lihat Invoice')
        TextView lihat = TransaksiUi.content(this, "Lihat Invoice", CustomColors.textPrimary);
        Ui.onTap(lihat, () -> {
            String url = ServerPath.baseURL + "/transaksi/invoice/" + idTransaksi;
            if (!TransaksiUi.launch(this, url)) {
                DetailUi.snackbar(bodyHost, "URL Tidak Valid !", 14f, 1000, null, 0, null);
            }
        });
        card.addView(TransaksiUi.spaceBetween(this,
                TransaksiUi.hbText(this, "Detail Transaksi", 4.25f, TransaksiUi.BLACK87, true), lihat),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(Ui.spaceH(this, 15.0f));

        if (!isRejected) {
            // Row(spaceBetween)[StatusTransaksi, received-button, otw-button]
            LinearLayout row = Ui.row(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(new StatusTransaksi(this, status, hb * 10.5, hb * 4, false, 10),
                    new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            row.addView(new View(this), new LinearLayout.LayoutParams(0, 0, 1f));
            if (statusLC.equals("received")) {
                LinearLayout pad = Ui.column(this);
                Ui.pad(pad, 5, 0, 0, 0);
                pad.addView(CustomButton.button(this, "Pesan Lagi",
                        () -> Nav.push(this, RepeatOrderActivity.class, Json.of("idTransaksi", idTransaksi)),
                        new CustomButton.Opts().color(CustomButton.themeColor).pill()));
                row.addView(pad, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            } else {
                row.addView(new View(this), new LinearLayout.LayoutParams(0, 0));
            }
            row.addView(new View(this), new LinearLayout.LayoutParams(0, 0, 1f));
            if (statusLC.equals("otw")) {
                LinearLayout pad = Ui.column(this);
                Ui.pad(pad, 5, 0, 0, 0);
                pad.addView(CustomButton.button(this, "Diterima", this::onDiterima,
                        new CustomButton.Opts().color(CustomButton.themeColor).outline().pill()));
                row.addView(pad, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            } else {
                row.addView(new View(this), new LinearLayout.LayoutParams(0, 0));
            }
            card.addView(row, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

            card.addView(Ui.spaceH(this, 10));
            card.addView(TransaksiUi.hbText(this, "Status : " + statusReadable, 3.25f, TransaksiUi.BLACK87, false));
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(Ui.MATCH, Math.max(1, Ui.dp(this, 0.5f)));
            dlp.topMargin = Ui.dp(this, 10);
            dlp.bottomMargin = Ui.dp(this, 10);
            View divider = new View(this);
            divider.setBackgroundColor(CustomColors.textMuted);
            card.addView(divider, dlp);
        }

        if (statusLC.equals("otw")) {
            LinearLayout note = Ui.column(this);
            note.setBackground(Ui.shape(this, 0x3366CC99, 10f));
            Ui.padXY(note, 10, 10);
            note.addView(TransaksiUi.hbText(this, "Note!", 3.5f, TransaksiUi.BLACK87, true));
            note.addView(Ui.spaceH(this, 2.5f));
            note.addView(Ui.text(this,
                    "Mak, jangan lupa tekan tombol DITERIMA untuk bisa pesan ulang produk yang sama ya ...",
                    new id.laukpauk.ui.Txt.Style(hb * 3.0f).color(TransaksiUi.BLACK87).italic()));
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
            nlp.topMargin = Ui.dp(this, 5);
            nlp.bottomMargin = Ui.dp(this, 5);
            card.addView(note, nlp);
        }
        card.addView(Ui.spaceH(this, 5.0f));

        // Nomor Order
        card.addView(TransaksiUi.title(this, "Nomor Order"));
        card.addView(Ui.spaceH(this, 5.0f));
        card.addView(ct(nomorTransaksi));
        card.addView(Ui.spaceH(this, 10));

        // Metode Pembayaran
        View caraBayar = new View(this);
        if (isBCA) {
            TextView cara = TransaksiUi.content(this, "Cara Pembayaran", CustomColors.textDanger);
            final JsonObject tbd = transferBankData;
            Ui.onTap(cara, () -> Nav.push(this, TransferBankActivity.class,
                    Json.of("returnedDataFromCheckout", tbd)));
            caraBayar = cara;
        }
        card.addView(TransaksiUi.spaceBetween(this, TransaksiUi.title(this, "Metode Pembayaran"), caraBayar),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(Ui.spaceH(this, 5.0f));
        card.addView(ct(namaMetodePembayaran));

        if (catatan.length() >= 1) {
            card.addView(Ui.spaceH(this, 10));
            card.addView(TransaksiUi.title(this, "Catatan Tambahan"));
            card.addView(ct(catatan));
        }
        card.addView(Ui.spaceH(this, isRejected ? 10 : 0));

        if (isRejected) {
            LinearLayout box = Ui.column(this);
            box.setBackground(Ui.shape(this, 0x25DC3545, 10f));
            Ui.padXY(box, 15, 10);
            box.addView(TransaksiUi.content(this, "Alasan Pembatalan", CustomColors.textDanger));
            box.addView(Ui.spaceH(this, 5));
            if (statusReason.length() >= 1) {
                box.addView(ct(statusReason));
            } else {
                box.addView(TransaksiUi.content(this, "Tanpa Keterangan", CustomColors.textMuted, false, true, false));
            }
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
            blp.bottomMargin = Ui.dp(this, 5);
            card.addView(box, blp);
        }
        card.addView(Ui.spaceH(this, 5.0f));

        if (statusLC.equals("rejected")) {
            final FrameLayout slot = new FrameLayout(this);
            slot.addView(Loading.view(this), new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            card.addView(slot, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            async(() -> TransaksiModel.jumlahItemDiantar(idTransaksi), jumlahItemDiantar -> {
                // FutureBuilder<int?>: null never has data -> the spinner stays.
                if (jumlahItemDiantar == null) {
                    return;
                }
                slot.removeAllViews();
                if (jumlahItemDiantar <= 0) {
                    slot.addView(buildRefundBox(refundedOngkir, refundedUangBelanja),
                            new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                }
            });
        }
        return card;
    }

    private View buildRefundBox(int refundedOngkir, int refundedUangBelanja) {
        LinearLayout box = Ui.column(this);
        box.setBackground(Ui.shape(this, 0x25DC3545, 10f));
        Ui.padXY(box, 15, 10);
        box.addView(TransaksiUi.content(this, "Pengembalian Dana ("
                + Currency.formatRupiah(refundedOngkir + refundedUangBelanja, 0) + ")", CustomColors.textDanger));
        box.addView(Ui.spaceH(this, 5));
        box.addView(TransaksiUi.content(this, "Ongkos Kirim " + Currency.formatRupiah(refundedOngkir, 0),
                CustomColors.textMuted, false, true, false));
        box.addView(TransaksiUi.content(this, "Uang Belanja " + Currency.formatRupiah(refundedUangBelanja, 0),
                CustomColors.textMuted, false, true, false));
        box.addView(Ui.spaceH(this, 15.0f));
        btnBelanjaUlang = CustomButton.button(this, "Belanja Ulang", this::onBelanjaUlang,
                new CustomButton.Opts().color(CustomColors.green).pill().block().busy(isButtonBelanjaUlangBusy));
        box.addView(btnBelanjaUlang, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
        lp.bottomMargin = Ui.dp(this, 5);
        box.setLayoutParams(lp);
        return box;
    }

    private View buildRincianCard() {
        JsonObject d = detailTransaksi;
        int totalBelanja = num(d, "totalBelanja");
        int ongkir = num(d, "ongkir");
        int nilaiVoucher = num(d, "nilaiVoucher");
        String jenisVoucherLC = Json.str(d, "jenisVoucher").toLowerCase();
        int nilaiPotonganWallet = num(d, "potonganWallet");
        int nilaiPointOut = num(d, "pointOut");
        int nilaiDiskonKategori = num(d, "diskonKategori");
        int nilaiDiskonKodePromo = num(d, "diskonPromo");

        String formattedTanggalDelivery = Json.str(d, "formattedTanggalDelivery");
        String jamDelivery = Json.str(d, "jadwalDelivery");
        String alamatDelivery = Json.str(d, "alamatDelivery");
        String alamatDetailLengkap = Json.strOrNull(d, "alamatDetailLengkap");

        int totalKeseluruhan = (totalBelanja + ongkir)
                - (nilaiVoucher + nilaiPotonganWallet + nilaiPointOut + nilaiDiskonKategori + nilaiDiskonKodePromo);

        boolean isBCA = false;
        int moreDataMetodePembayaran = 0;
        if (Json.str(d, "metodePembayaran").toLowerCase().equals("tf_bca")) {
            moreDataMetodePembayaran = num(d, "moreDataMetodePembayaran");
            isBCA = true;
            totalKeseluruhan += moreDataMetodePembayaran;
        }

        LinearLayout card = TransaksiUi.card(this, 15, 10);
        card.addView(TransaksiUi.title(this, "Alamat Pengantaran"));
        card.addView(Ui.spaceH(this, 5));
        card.addView(ct(alamatDelivery));
        card.addView(Ui.spaceH(this, 15));
        if (alamatDetailLengkap != null) {
            LinearLayout col = Ui.column(this);
            col.setGravity(Gravity.CENTER_HORIZONTAL);
            col.addView(TransaksiUi.title(this, "Alamat Detail Pengantaran"));
            col.addView(Ui.spaceH(this, 5));
            col.addView(ct(alamatDetailLengkap));
            col.addView(Ui.spaceH(this, 15));
            card.addView(col, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        }
        card.addView(TransaksiUi.title(this, "Jadwal Delivery"));
        card.addView(Ui.spaceH(this, 5));
        card.addView(ct(formattedTanggalDelivery + ", ETA Jam " + jamDelivery));
        card.addView(Ui.spaceH(this, 15));
        View line = new View(this);
        line.setBackgroundColor(CustomColors.green);
        card.addView(line, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(this, 1)));
        card.addView(Ui.spaceH(this, 15));

        card.addView(rincianRow("Potongan Dompet", "(" + Currency.formatRupiah(nilaiPotonganWallet, 0) + ")", false));
        card.addView(rincianRow("Potongan Kode Promo",
                "(" + Currency.formatRupiah(nilaiDiskonKodePromo, 0) + ")", false));
        card.addView(rincianRow("Potongan Poin", "(" + Currency.formatRupiah(nilaiPointOut, 0) + ")", false));
        card.addView(rincianRow("Ongkos Kirim", Currency.formatRupiah(ongkir, 0), false));
        if (nilaiVoucher >= 1 && jenisVoucherLC.equals("ongkir")) {
            card.addView(rincianRow("Potongan Voucher Ongkir",
                    "(" + Currency.formatRupiah(nilaiVoucher, 0) + ")", false));
        }
        card.addView(rincianRow("Total Belanja", Currency.formatRupiah(totalBelanja, 0), false));
        if (jenisVoucherLC.equals("belanja") && nilaiVoucher >= 1) {
            card.addView(rincianRow("Potongan Voucher Belanja",
                    "(" + Currency.formatRupiah(nilaiVoucher, 0) + ")", false));
        }
        if (nilaiDiskonKategori >= 1) {
            card.addView(rincianRow("Diskon", "(" + Currency.formatRupiah(nilaiDiskonKategori, 0) + ")", false));
        }
        if (isBCA) {
            card.addView(rincianRow("Kode Unik", Currency.formatRupiah(moreDataMetodePembayaran, 0), false));
        }
        card.addView(rincianRow("Total Yang Harus Dibayar", Currency.formatRupiah(totalKeseluruhan, 0), true));
        return card;
    }

    // ------------------------------------------------------------------ actions

    private void onDiterima() {
        String title = "Pesanan Diterima";
        String subTitle = "Lakukan konfirmasi jika pesanan sudah diterima ya mak ...";
        CustomAlert.konfirmasi(this, title, subTitle, null, konfirmasi -> {
            if (konfirmasi != null && konfirmasi) {
                async(() -> PesananSayaModel.pesananDiterima(idTransaksi), statusPesananDiterima -> {
                    String message = "Gagal menandai pesanan sebagai SUDAH DITERIMA. Silahkan coba lagi!";
                    if (statusPesananDiterima) {
                        doReload();
                        message = "Berhasil menanda pesanan sebagai SUDAH DITERIMA !";
                    }
                    DetailUi.snackbar(bodyHost, message, 14f, 3000, null, 0, null);
                });
            }
        });
    }

    private void setBelanjaUlangBusy(boolean busy) {
        isButtonBelanjaUlangBusy = busy;
        if (btnBelanjaUlang != null) {
            btnBelanjaUlang.setBusy(busy);
        }
    }

    private void onBelanjaUlang() {
        if (LocalStorage.isAlamatDeliveryDanJadwalDeliverySet()) {
            setBelanjaUlangBusy(true);

            String adjdTitle = "Jadwal dan Alamat Antar";
            String adjdSubTitle = "Gunakan alamat dan jadwal pengantaran yang sudah di atur sebelumnya?";
            final float hb = Ui.hb(this);
            Modal.Content<Boolean> adjdActions = handle -> {
                LinearLayout col = Ui.column(this);
                Ui.padXY(col, hb * 15.0f, 0);
                LinearLayout row = Ui.row(this);
                row.setGravity(Gravity.CENTER);
                row.addView(CustomButton.button(this, "Ya", () -> handle.close(Boolean.TRUE),
                        new CustomButton.Opts().color(CustomColors.green)), new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
                row.addView(Ui.spaceW(this, 5.0f));
                row.addView(CustomButton.button(this, "Tidak", () -> handle.close(Boolean.FALSE),
                        new CustomButton.Opts().color(CustomColors.textDanger)), new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
                col.addView(row, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                col.addView(Ui.spaceH(this, 5.0f));
                col.addView(CustomButton.button(this, "Tutup", () -> handle.close(),
                        new CustomButton.Opts().block().color(CustomColors.textMuted).outline()),
                        new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                return col;
            };
            CustomAlert.<Boolean>textComponent(this, null, adjdTitle, adjdSubTitle, adjdActions, null, false,
                    useSameADJD -> {
                        if (useSameADJD != null) {
                            if (!useSameADJD) {
                                async(() -> {
                                    ADJDModel.deleteAlamatDanJadwalDelivery();
                                    return Boolean.TRUE;
                                }, ok -> HeaderModel.adjd(this, null, null, null, r -> doBelanjaUlang()));
                            } else {
                                doBelanjaUlang();
                            }
                        } else {
                            setBelanjaUlangBusy(false);
                        }
                    });
        } else {
            Modal.Content<Object> actions = handle -> CustomAlert.actionsRow(this,
                    CustomButton.button(this, "OK", () -> {
                        handle.close();
                        Nav.push(this, HomePageActivity.class, null);
                    }, new CustomButton.Opts().color(CustomColors.green)));
            CustomAlert.<Object>textComponent(this, null, "Alamat dan Jadwal",
                    "Alamat dan jadwal pengantaran belum ditentukan, tentukan dulu ya mak...", actions, null, false,
                    null);
        }
    }

    private void doBelanjaUlang() {
        async(() -> TransaksiModel.belanjaUlang(idTransaksi), responseBelanjaUlang -> {
            if (responseBelanjaUlang != null) {
                TransaksiModel.responseBelanjaUlang(this, responseBelanjaUlang,
                        v -> setBelanjaUlangBusy(false));
            } else {
                setBelanjaUlangBusy(false);
            }
        });
    }
}
