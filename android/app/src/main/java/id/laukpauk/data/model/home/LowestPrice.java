package id.laukpauk.data.model.home;

/** Cheapest unit price of a product (Produk/getLowestPriceOfThisProduct). */
public class LowestPrice {
    public long diskon;
    public long harga;
    public long hargaBersih;

    /** Discount in percent of the gross price (0 when none). */
    public double persentaseDiskon() {
        return harga <= 0 ? 0 : (double) diskon / harga * 100;
    }
}
