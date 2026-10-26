package id.laukpauk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.json.JSONObject;
import id.laukpauk.models.BannerModel;
import id.laukpauk.models.HomepageModel;
import id.laukpauk.models.KategoriProdukModel;
import id.laukpauk.models.NotifikasiModel;
import id.laukpauk.models.PromoModel;
import id.laukpauk.models.ResepModel;
import id.laukpauk.models.TransaksiModel;
import id.laukpauk.models.UserModel;
import id.laukpauk.models.VoucherModel;
import id.laukpauk.models.ProsesPesananModel;

public final class NativeFeatureActivity extends Activity {
    public static final String EXTRA_ROUTE = "native.route";
    public static final String EXTRA_QUERY = "native.query";
    private static final int GREEN = Color.rgb(36, 125, 76);
    private TextView title, subtitle, result;
    private ProgressBar progress;
    private LinearLayout actions;
    private EditText identifier;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        NativePreferences.migrateKnownLegacyValues(getApplicationContext());
        setContentView(R.layout.activity_native_feature);
        title = findViewById(R.id.native_feature_title);
        subtitle = findViewById(R.id.native_feature_subtitle);
        result = findViewById(R.id.native_feature_result);
        progress = findViewById(R.id.native_feature_progress);
        actions = findViewById(R.id.native_feature_actions);
        String route = getIntent().getStringExtra(EXTRA_ROUTE);
        if (route == null || route.isEmpty()) route = "buyer";
        render(route);
    }

    private void render(String route) {
        actions.removeAllViews();
        result.setText("");
        identifier = null;
        title.setText(titleFor(route));
        subtitle.setText("Native Android (Java) · data and actions available depend on the migrated API.");
        if ("buyer".equalsIgnoreCase(route) || "home".equalsIgnoreCase(route) || "/".equals(route)) {
            addAction("Beranda / banner", "banner");
            addAction("Kategori produk", "categories");
            addAction("Pasar", "market");
            addAction("Promo", "promos");
            addAction("Resep", "recipes");
            addAction("Keranjang", "cart");
            addAction("Checkout", "checkout");
            addAction("Pesanan saya", "orders");
            addAction("Voucher", "vouchers");
            addAction("Notifikasi", "notifications");
            addAction("Profil saya", "profile");
            addAction("Masuk / Login", "login");
            addAction("Kelola toko", "seller");
        } else if ("seller".equalsIgnoreCase(route)) {
            addAction("Pesanan masuk (butuh ID transaksi)", "seller-orders");
            addAction("Proses pesanan (butuh ID transaksi)", "seller-process");
            addAction("Produk toko", "seller-products");
            addAction("Informasi toko", "seller-store");
            addAction("Kembali ke pembeli", "buyer");
        } else {
            switch (route.toLowerCase()) {
                case "login":
                    startActivity(new Intent(this, LoginActivity.class));
                    break;
                case "banner":
                    request("Memuat banner…", cb -> BannerModel.getBanner(this, cb));
                    break;
                case "categories":
                    request("Memuat kategori…", cb -> KategoriProdukModel.getKategoriProduk(this, null, cb));
                    break;
                case "market":
                    askIdentifier("ID pasar (diperlukan untuk daftar produk)", "ID pasar");
                    addAction("Muat produk pasar", "market-load");
                    break;
                case "market-load":
                    String marketId = inputValue();
                    if (marketId.isEmpty()) { result.setText("Masukkan ID pasar terlebih dahulu."); break; }
                    request("Memuat produk pasar…", cb -> id.laukpauk.models.PasarModel.getProdukInThisPasar(this, marketId, "true", null, null, cb));
                    break;
                case "promos":
                    request("Memuat promo…", cb -> PromoModel.getProdukPromo(this, cb));
                    break;
                case "recipes":
                    request("Memuat resep…", cb -> ResepModel.getResep(this, cb));
                    break;
                case "notifications":
                    request("Memuat notifikasi…", cb -> NotifikasiModel.getUpdateNotifikasi(this, cb));
                    break;
                case "profile":
                    request("Memuat profil…", cb -> UserModel.getDetailActiveUser(this, cb));
                    break;
                case "orders":
                    askIdentifier("ID transaksi untuk detail pesanan", "ID transaksi");
                    addAction("Muat detail pesanan", "order-load");
                    break;
                case "order-load":
                    String orderId = inputValue();
                    if (orderId.isEmpty()) { result.setText("Masukkan ID transaksi terlebih dahulu."); break; }
                    request("Memuat detail transaksi…", cb -> TransaksiModel.getDetailTransaksi(this, orderId, cb));
                    break;
                case "vouchers":
                    askIdentifier("ID voucher untuk detail voucher", "ID voucher");
                    addAction("Muat detail voucher", "voucher-load");
                    break;
                case "voucher-load":
                    String voucherId = inputValue();
                    if (voucherId.isEmpty()) { result.setText("Masukkan ID voucher terlebih dahulu."); break; }
                    request("Memuat voucher…", cb -> VoucherModel.getVoucherDetail(this, voucherId, cb));
                    break;
                case "seller-orders":
                    askIdentifier("ID transaksi pesanan masuk", "ID transaksi");
                    addAction("Muat pesanan masuk", "seller-orders-load");
                    break;
                case "seller-orders-load":
                    String sellerOrderId = inputValue();
                    if (sellerOrderId.isEmpty()) { result.setText("Masukkan ID transaksi terlebih dahulu."); break; }
                    request("Memuat pesanan masuk…", cb -> ProsesPesananModel.getPesananMasukItem(this, sellerOrderId, cb));
                    break;
                case "seller-process":
                    askIdentifier("ID transaksi yang diproses", "ID transaksi");
                    addAction("Muat proses pesanan", "seller-process-load");
                    break;
                case "seller-process-load":
                    String processId = inputValue();
                    if (processId.isEmpty()) { result.setText("Masukkan ID transaksi terlebih dahulu."); break; }
                    request("Memuat proses pesanan…", cb -> ProsesPesananModel.getProsesPesanan(this, processId, cb));
                    break;
                case "cart":
                    result.setText("Keranjang dan mutasi item belum selesai dipindahkan. Belum ada operasi perubahan keranjang yang dipasang pada layar ini.");
                    break;
                case "checkout":
                    result.setText("Checkout, pemilihan metode pembayaran, jadwal, voucher, dan pembayaran belum selesai dipindahkan. Jangan gunakan layar ini untuk transaksi nyata.");
                    break;
                case "seller-products":
                    result.setText("Manajemen produk penjual belum selesai dipindahkan.");
                    break;
                case "seller-store":
                    result.setText("Pengaturan toko dan lokasi penjualan belum selesai dipindahkan.");
                    break;
                default:
                    result.setText("Layar native untuk rute ini belum selesai dimigrasikan. Kode Dart sumber tetap dipertahankan.");
                    break;
            }
        }
    }

    private String titleFor(String route) {
        switch (route.toLowerCase()) {
            case "buyer": case "home": return "Laukpauk · Pembeli";
            case "seller": return "Laukpauk · Penjual";
            case "banner": return "Beranda";
            case "categories": return "Kategori produk";
            case "market": case "market-load": return "Pasar";
            case "promos": return "Promo";
            case "recipes": return "Resep";
            case "notifications": return "Notifikasi";
            case "profile": return "Profil saya";
            case "orders": case "order-load": return "Pesanan saya";
            case "vouchers": case "voucher-load": return "Voucher";
            case "seller-orders": case "seller-orders-load": return "Pesanan masuk";
            case "seller-process": case "seller-process-load": return "Proses pesanan";
            case "cart": return "Keranjang";
            case "checkout": return "Checkout";
            default: return "Laukpauk";
        }
    }

    private void addAction(String label, String route) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(label);
        button.setTextColor(Color.WHITE);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(GREEN));
        button.setOnClickListener(v -> render(route));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        lp.topMargin = dp(6);
        actions.addView(button, lp);
    }

    private void askIdentifier(String hint, String label) {
        identifier = new EditText(this);
        identifier.setSingleLine(true);
        identifier.setHint(hint);
        actions.addView(identifier, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
    }

    private String inputValue() {
        return identifier == null || identifier.getText() == null ? "" : identifier.getText().toString().trim();
    }

    private interface Request { void run(NativeApiClient.Callback callback); }
    private void request(String loading, Request request) {
        progress.setVisibility(View.VISIBLE);
        result.setText(loading);
        try {
            request.run(new NativeApiClient.Callback() {
                @Override public void onSuccess(int statusCode, JSONObject body) {
                    progress.setVisibility(View.GONE);
                    result.setText("HTTP " + statusCode + "\n\n" + body.toString());
                }
                @Override public void onError(Exception error) {
                    progress.setVisibility(View.GONE);
                    result.setText("Permintaan gagal: " + error.getMessage()
                            + "\n\nPastikan LAUKPAUK_API_BASE_URL disetel dan dapat diakses.");
                }
            });
        } catch (Exception error) {
            progress.setVisibility(View.GONE);
            result.setText("Konfigurasi/permintaan belum siap: " + error.getMessage());
        }
    }
    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
