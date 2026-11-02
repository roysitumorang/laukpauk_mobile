package id.laukpauk.data.model.orders;

import java.util.List;

/** One seller of a transaction that can be repeat-ordered (RO/getItemTransaksi). */
public class RoPenjual {
    public boolean isAvailable;
    public String idTransaksi;
    public String idPenjual;
    public String namaPenjual;
    public String namaPasar;
    public String bannerPenjual;
    public String alamatToko;
    public List<ItemTransaksi> itemTransaksi;
}
