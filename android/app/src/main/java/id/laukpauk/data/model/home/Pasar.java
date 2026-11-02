package id.laukpauk.data.model.home;

/** A market (Pasar/getPasar, Pasar/getPasarAroundOfThisLatLong). {@code distance} is only sent for the latter. */
public class Pasar {
    public String id;
    public String nama;
    public String gambar;
    public String distance;

    /** The distance in km, or null when the server sent none / something unparsable. */
    public Double distanceKm() {
        if (distance == null) {
            return null;
        }
        try {
            return Double.valueOf(Double.parseDouble(distance.trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
