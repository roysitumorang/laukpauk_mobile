package id.laukpauk.data.model.orders;

/** One line of a transaction (Transaksi/getItemTransaksiFromTransaksi, RO/getItemTransaksi). */
public class ItemTransaksi {
    public String id;
    public String namaProduk;
    public String namaSatuan;
    public String jenisProduk;
    public String catatan;
    public String status;
    public int quantity;
    public int harga;
    public int diskon;
    public int diskonPromo;

    /** "nonpaket" lines show their unit, packages show "paket". */
    public String satuan() {
        if (jenisProduk != null && jenisProduk.toLowerCase().equals("nonpaket")) {
            return namaSatuan == null ? "" : namaSatuan;
        }
        return "paket";
    }
}
