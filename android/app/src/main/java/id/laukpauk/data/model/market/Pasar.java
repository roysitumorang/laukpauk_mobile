package id.laukpauk.data.model.market;

/** A market (pasar) in the "Pasar Lain" lists. */
public class Pasar {
    public String id;
    public String nama;
    public String gambar;
    /** "0" = not a public market (shows the app logo). */
    public String pasarUmum;
    /** Distance in KM (only sent for markets around the delivery address). */
    public double distance;
}
