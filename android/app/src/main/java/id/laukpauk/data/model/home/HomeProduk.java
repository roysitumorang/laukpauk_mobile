package id.laukpauk.data.model.home;


/**
 * A product tile (market products, konten items, category products, ListProduk). Only the keys the tiles use
 * are declared; the screens pass lists of these around as JSON text ({@code listProdukFragment.dataProduk}).
 */
public class HomeProduk {
    public String id;
    public String nama;
    public String foto;
    public String namaKategoriProduk;
    public String keteranganSingkat;
    /** Missing = available. */
    public Boolean stokAvailable;
    /** Active promos; only "is there any" matters (dynamic: array, or false/"" when none). */
    public Object promo;

    public boolean isStokAvailable() {
        return stokAvailable == null || stokAvailable;
    }

    public boolean isInActivePromo() {
        return promo instanceof java.util.List && !((java.util.List<?>) promo).isEmpty();
    }
}
