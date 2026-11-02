package id.laukpauk.ui.seller;

/** What the seller has chosen so far in the "update lokasi berjualan" sheet. */
final class TokoLokasiState {
    static final String PASAR_TERSEDIA = "pasarTersedia";
    static final String LOKASI_TERSENDIRI = "lokasiTersendiri";

    String jenis = PASAR_TERSEDIA;
    /** Selected market id (jenis = pasarTersedia). */
    String idPasar;
    /** Own location (jenis = lokasiTersendiri). */
    String latitude;
    String longitude;
    String alamat;

    boolean hasSelection() {
        return PASAR_TERSEDIA.equals(jenis) ? idPasar != null : latitude != null;
    }

    void reset() {
        jenis = PASAR_TERSEDIA;
        idPasar = null;
        latitude = null;
        longitude = null;
        alamat = null;
    }
}
