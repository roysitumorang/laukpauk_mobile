package id.laukpauk.data.model.home;

import java.util.List;

/** Response envelopes of the home endpoints. */
public final class HomeResponses {
    private HomeResponses() {}

    public static class Banners {
        public List<Banner> listBanner;
    }

    public static class Artikels {
        public List<Artikel> listArtikel;
    }

    public static class KontenList {
        public List<Konten> dataKonten;
    }

    public static class KontenItems {
        public List<KontenItem> kontenItem;
    }

    public static class ProdukList {
        public List<HomeProduk> listProduk;
    }

    public static class JumlahVoucher {
        public int jumlahVoucher;
    }

    public static class TotalDompet {
        public long totalDompet;
    }

    public static class PointData {
        public long point;
    }

    public static class Point {
        /** false when the user has no point record. */
        public PointData response;
    }

    public static class LowestPriceResponse {
        /** false when the product has no price list. */
        public LowestPrice lowestPrice;
    }

    public static class KategoriList {
        public List<KategoriProduk> kategoriProduk;
    }

    public static class ProdukInKategori {
        public List<HomeProduk> productInThisCategory;
    }
}
