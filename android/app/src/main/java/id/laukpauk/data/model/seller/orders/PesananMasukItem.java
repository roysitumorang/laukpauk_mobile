package id.laukpauk.data.model.seller.orders;

/** An ordered item of an incoming order (Penjual/getPesananMasukItem). Also passed as JSON to PesananItemTidakTersedia. */
public class PesananMasukItem {
    public String id;
    public String quantity;
    public String namaProduk;
    public String namaSatuan;
    public String harga;
    public String diskon;
    public String fotoProduk;
    public String catatan;
    public String jenisProduk;
    /** null = not checked yet, "rejected" = marked as unavailable. */
    public String status;
}
