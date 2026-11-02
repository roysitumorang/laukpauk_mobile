package id.laukpauk.data.model.delivery;

/** A Google Places text-search hit, as shown in the address search box (legacy CariGoogleMaps). */
public final class PlaceSuggestion {
    public final String name;
    public final String formattedAddress;
    public final double lat;
    public final double lng;

    public PlaceSuggestion(String name, String formattedAddress, double lat, double lng) {
        this.name = name;
        this.formattedAddress = formattedAddress;
        this.lat = lat;
        this.lng = lng;
    }
}
