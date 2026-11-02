package id.laukpauk.data.model.voucher;

import androidx.annotation.Nullable;

/** A voucher (list rows carry only some fields; the detail has them all). */
public final class Voucher {
    public String id;
    public String foto;
    public String nama;
    /** "ongkir" or "belanja". */
    public String jenis;
    public String nilai;
    public String kode;
    public String minimumBelanja;
    public String maximumUsage;
    public String usageCount;
    public String berlakuMulai;
    public String berlakuSampai;
    public String deskripsi;

    public static int toInt(@Nullable String value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            try {
                return (int) Double.parseDouble(value.trim());
            } catch (NumberFormatException e2) {
                return 0;
            }
        }
    }
}
