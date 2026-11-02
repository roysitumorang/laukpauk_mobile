package id.laukpauk.data.model.home;

/** A konten block of the app ("homepage" / "headersearch"): {@code jenisItem} is "produk" or a banner type. */
public class Konten {
    public String id;
    public String namaDivisi;
    public String jenisItem;
    /** Banner blocks: the named route a banner opens (with its {@code pilihan} as argument). */
    public String route;

    public boolean isProduk() {
        return jenisItem != null && jenisItem.toLowerCase().equals("produk");
    }
}
