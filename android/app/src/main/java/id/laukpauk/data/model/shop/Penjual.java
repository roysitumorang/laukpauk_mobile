package id.laukpauk.data.model.shop;

import androidx.annotation.Nullable;

/** Public profile of a seller (Penjual/getDetailPenjual). */
public class Penjual {
    public String id;
    @Nullable public String namaToko;
    @Nullable public String namaPasar;
    @Nullable public String alamatToko;
    @Nullable public String banner;
    /** The market id. */
    @Nullable public String pasar;
}
