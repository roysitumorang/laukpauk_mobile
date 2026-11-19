package id.laukpauk.data.model.home;

/**
 * A hero slider banner, one entry of {@code data} in {@code GET buyer/banners}.
 * {@code photo} is a full image URL; {@code name} and {@code description} may be empty.
 */
public class Banner {
    public String id;
    public String name;
    public String description;
    public String photo;
    public Boolean visible;
}
