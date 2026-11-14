package id.laukpauk.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.orders.GiftInfo;
import id.laukpauk.data.model.orders.GiftTransaksi;
import id.laukpauk.data.model.orders.ItemTransaksi;
import id.laukpauk.data.model.orders.Transaksi;
import id.laukpauk.data.model.orders.TransaksiResponses;
import id.laukpauk.data.remote.TransaksiApi;

/** The buyer's transactions: history, detail, items, gifts, "pesanan diterima" and "belanja ulang". */
public class TransaksiRepository extends BaseRepository {

    /** Status filter of each tab of Pesanan Saya (tab 0 = today, no status). */
    private static final String[] TAB_STATUS = {null, "pending", "processing", "otw", "received", "rejected"};

    private static final String ENTITAS_ITEM =
            "id,  namaProduk, namaSatuan, quantity, harga, diskon, catatan, jenisProduk, status, diskonPromo";

    private final TransaksiApi api;
    private final DeliveryRepository delivery;

    public TransaksiRepository(RepositoryDeps deps, DeliveryRepository delivery) {
        super(deps);
        this.api = api(TransaksiApi.class);
        this.delivery = delivery;
    }

    /**
     * Legacy PesananSaya.getRiwayatTransaksi. {@code tab} 0..5; {@code hariIni} (tab 0 only) is the status
     * dropdown value ("" = all); {@code awal}/{@code akhir} ("yyyy-MM-d", tabs 1..5 only) the date range.
     */
    public LiveData<Resource<List<Transaksi>>> getRiwayat(int tab, String hariIni, String awal, String akhir) {
        String status = (tab >= 0 && tab < TAB_STATUS.length) ? TAB_STATUS[tab] : null;
        String hariIniQ = (status == null && hariIni != null && !hariIni.isEmpty()) ? hariIni : null;
        String awalQ = (status != null && awal != null && !awal.isEmpty()) ? awal : null;
        String akhirQ = (status != null && akhir != null && !akhir.isEmpty()) ? akhir : null;
        return load(api.getRiwayatTransaksi(status, hariIniQ, awalQ, akhirQ),
                body -> body == null || body.riwayatTransaksi == null
                        ? Collections.<Transaksi>emptyList() : body.riwayatTransaksi);
    }

    /** Transaction detail; SUCCESS with null data when it does not exist. */
    public LiveData<Resource<Transaksi>> getDetail(String idTransaksi) {
        return getDetail(idTransaksi, null);
    }

    /** {@code entitas} limits the returned columns (e.g. "nomorTransaksi, tanggalDelivery"). */
    public LiveData<Resource<Transaksi>> getDetail(String idTransaksi, @Nullable String entitas) {
        return load(api.getTransaksi(idTransaksi, entitas),
                body -> body == null ? null : body.transaksi);
    }

    /** The lines of a transaction. */
    public LiveData<Resource<List<ItemTransaksi>>> getItemTransaksi(String idTransaksi) {
        return load(api.getItemTransaksi(idTransaksi, ENTITAS_ITEM),
                body -> body == null || body.itemTransaksi == null
                        ? Collections.<ItemTransaksi>emptyList() : body.itemTransaksi);
    }

    /** Number of items already delivered (a rejected transaction can be refunded/re-ordered when 0). */
    public LiveData<Resource<Integer>> getJumlahItemDiantar(String idTransaksi) {
        return load(api.getJumlahItemDiantar(idTransaksi),
                body -> body == null ? 0 : body.jumlahBarangDiantar);
    }

    /** Marks an "otw" transaction as received; SUCCESS(true) when the server accepted it. */
    public void pesananDiterima(String idTransaksi, ResultCallback<Boolean> callback) {
        enqueue(api.pesananDiterima(idTransaksi), body -> body != null && body.pesananDiterima,
                callback);
    }

    /** Legacy TransaksiModel.belanjaUlang: re-adds a rejected transaction; the saved schedule is sent when one is chosen. */
    public void belanjaUlang(String idTransaksi, ResultCallback<TransaksiResponses.BelanjaUlang> callback) {
        DeliverySelection selection = delivery.current();
        String code = selection.jadwalDeliveryCode;
        String day = code == null ? null : selection.dayDelivery != null ? selection.dayDelivery : "Hari Pengantaran";
        enqueue(api.belanjaUlang(idTransaksi, body("dayDelivery", day, "jadwalDeliveryCode", code, "cartCode",
                session.getCartCode())), body -> body, callback);
    }

    /** Gifts attached to a cart (empty when none). */
    public LiveData<Resource<List<GiftTransaksi>>> getGiftTransaksi(String cartCode) {
        return load(api.getGiftTransaksi(cartCode),
                body -> body == null || body.giftTransaksi == null
                        ? Collections.<GiftTransaksi>emptyList() : body.giftTransaksi);
    }

    /** One gift; SUCCESS with null data when it does not exist. */
    public void getGift(String idGift, ResultCallback<GiftInfo> callback) {
        enqueue(api.getGift(idGift), body -> body == null ? null : body.gift, callback);
    }
}
