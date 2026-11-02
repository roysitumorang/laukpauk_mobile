package id.laukpauk.data.model.home;

/** An item of a konten block: a product (HomeProduk fields) or a banner ({@code item}, {@code pilihan}, {@code keterangan}). */
public class KontenItem extends HomeProduk {
    /** Banner: the id passed to the banner's route. */
    public String pilihan;
    /** Banner: the image file. */
    public String item;
    public String keterangan;
}
