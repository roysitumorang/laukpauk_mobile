package id.laukpauk.data.model.product;

import androidx.annotation.Nullable;

/** What the "Pilihan Satuan" sheet needs before it is shown (legacy ProdukModel.showSatuanProduk). */
public class SatuanSheetData {
    /** The product (name, photo, category, stock); null when it could not be loaded. */
    @Nullable public Produk produk;
    public boolean inCoverageArea;
    public boolean sellerAvailable;
    /** "Rp 1.000" or "" (costPerKm setting). */
    public String ongkirPerKm = "";
}
