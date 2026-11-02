package id.laukpauk.data.model.seller.products;

/** One unit/price (satuan harga) of a product: HargaProduk rows. Also passed as JSON to EditSatuanProduk. */
public class SatuanProdukData {
    public String id;
    public String idProduk;
    public String nama;
    public String harga;
    public String diskon;
    /** "pas" or "tidak pas". */
    public String jenisHarga;
    public String keterangan;
}
