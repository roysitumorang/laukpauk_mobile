package id.laukpauk.data.model.product;

/** Produk/getLowestPriceOfThisProduct: the cheapest unit of a product. */
public class LowestPrice {
    /** Marker for "this product has no price list". */
    public static final LowestPrice NONE = new LowestPrice();

    public int harga;
    public int diskon;
    public int hargaBersih;

    /** Discount percentage of the gross price (0 when none). */
    public double persentaseDiskon() {
        return harga == 0 ? 0 : (double) diskon / harga * 100;
    }
}
