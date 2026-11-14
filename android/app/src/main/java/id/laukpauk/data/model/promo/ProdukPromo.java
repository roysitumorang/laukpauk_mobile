package id.laukpauk.data.model.promo;

import id.laukpauk.util.Json;

/** One promo product of {@code Promo/getPromoAroundOfThisLatLong} (a card of the Promo grid). */
public class ProdukPromo {
    public String idPromo;
    public String idPromoItem;
    public String idProduk;
    /** The price (unit) id, sent as idSatuan when adding to the cart. */
    public String idHargaProduk;
    public String namaProduk;
    public String keteranganSingkat;
    public String fotoProduk;
    public String maxOrder;
    /** Numeric strings from the server; use the int accessors. */
    public String hargaProduk;
    public String diskonProduk;
    public String diskonPromo;

    public int hargaKotor() {
        return Json.parseInt(hargaProduk, 0);
    }

    public int diskon() {
        return Json.parseInt(diskonProduk, 0);
    }

    public int diskonPromoInt() {
        return Json.parseInt(diskonPromo, 0);
    }

    /** Price after the product discount and the promo discount. */
    public int hargaBersihPromo() {
        return hargaKotor() - diskon() - diskonPromoInt();
    }
}
