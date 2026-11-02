package id.laukpauk.data.model.seller.products;

import java.util.List;

/** Response envelopes of the seller product endpoints. */
public final class ProdukPenjualResponses {
    private ProdukPenjualResponses() {}

    /** Produk/getHargaProduk: the product detail and (with entitasHargaProduk) its units. */
    public static class HargaProduk {
        /** null when the server answers false. */
        public DetailProdukPenjual detailProduk;
        public List<SatuanProdukData> hargaProduk;
    }

    public static class DetailProduk {
        public DetailProdukPenjual detailProduk;
    }

    public static class DetailPaket {
        public DetailProdukPenjual detailPaket;
    }

    public static class Kategori {
        public List<KategoriPilihan> kategoriProduk;
    }

    public static class ProdukList {
        public List<ProdukPenjual> produkPenjual;
    }

    public static class PaketList {
        public List<ProdukPenjual> paketPenjual;
    }

    /** All the boolean status flags the write endpoints answer with (each endpoint sets one). */
    public static class Status {
        public boolean statusToggleProdukPalingLaris;
        public boolean statusTogglePaketPalingLaris;
        public boolean statusUbahKategoriProduk;
        public boolean statusUbahDeskripsiProduk;
        public boolean statusUbahDeskripsiPaket;
        public boolean statusHapusProduk;
        public boolean statusHapusPaket;
        public boolean statusHapusHargaProduk;
        public boolean statusSaveHargaProduk;
        public boolean saveProduk;
    }
}
