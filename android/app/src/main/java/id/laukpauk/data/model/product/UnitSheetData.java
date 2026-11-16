package id.laukpauk.data.model.product;

import com.squareup.moshi.Json;

import androidx.annotation.Nullable;

/** What the "Pilihan Satuan" sheet needs before it is shown (legacy ProdukModel.showSatuanProduk). */
public class UnitSheetData {
    /** The product (name, photo, category, stock); null when it could not be loaded. */
    @Json(name = "produk")
    @Nullable public Product product;
    public boolean inCoverageArea;
    /** "Rp 1.000" or "" (costPerKm setting). */
    @Json(name = "ongkirPerKm")
    public String shippingFeePerKm = "";
}
