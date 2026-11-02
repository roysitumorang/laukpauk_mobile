package id.laukpauk.data.model.search;

/** One row of Konten/pencarian: a product ({@code jenisItem} "produk") or a seller ("penjual"). */
public class SearchSuggestion {
    public String id;
    public String jenisItem;
    public String fotoProduk;
    public String namaProduk;
    public String namaPenjual;
    public String bannerToko;
    public String namaToko;
    public String alamatToko;

    public boolean isPenjual() {
        return jenisItem != null && jenisItem.toLowerCase().equals("penjual");
    }
}
