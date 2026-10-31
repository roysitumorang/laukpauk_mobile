package id.laukpauk.core;

import id.laukpauk.BuildConfig;

/**
 * Port of lib/libraries/ServerPath.dart (which is git-ignored in the Flutter repo).
 * Values come from laukpauk.properties through BuildConfig - see laukpauk.properties.example.
 *
 * Image URLs are built as: baseURL + "/" + uploadGambarX + "/" + fileName
 */
public final class ServerPath {
    private ServerPath() {}

    public static final String apiBaseURL = BuildConfig.API_BASE_URL;
    public static final String baseURL = BuildConfig.BASE_URL;

    public static final String uploadGambarArtikel = BuildConfig.UPLOAD_GAMBAR_ARTIKEL;
    public static final String uploadGambarBanner = BuildConfig.UPLOAD_GAMBAR_BANNER;
    public static final String uploadGambarBannerPenjual = BuildConfig.UPLOAD_GAMBAR_BANNER_PENJUAL;
    public static final String uploadGambarGift = BuildConfig.UPLOAD_GAMBAR_GIFT;
    public static final String uploadGambarKategoriProduk = BuildConfig.UPLOAD_GAMBAR_KATEGORI_PRODUK;
    public static final String uploadGambarKontenItem = BuildConfig.UPLOAD_GAMBAR_KONTEN_ITEM;
    public static final String uploadGambarPaket = BuildConfig.UPLOAD_GAMBAR_PAKET;
    public static final String uploadGambarPasar = BuildConfig.UPLOAD_GAMBAR_PASAR;
    public static final String uploadGambarProduk = BuildConfig.UPLOAD_GAMBAR_PRODUK;
    public static final String uploadGambarPushNotifikasi = BuildConfig.UPLOAD_GAMBAR_PUSH_NOTIFIKASI;
    public static final String uploadGambarResep = BuildConfig.UPLOAD_GAMBAR_RESEP;
    public static final String uploadGambarUser = BuildConfig.UPLOAD_GAMBAR_USER;
    public static final String uploadGambarVoucher = BuildConfig.UPLOAD_GAMBAR_VOUCHER;
}
