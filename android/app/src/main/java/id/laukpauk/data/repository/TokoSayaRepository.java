package id.laukpauk.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.shop.DetailPenjual;
import id.laukpauk.data.model.seller.shop.Pasar;
import id.laukpauk.data.model.seller.shop.TokoSayaResponses;
import id.laukpauk.data.remote.TokoSayaApi;

/**
 * The seller's shop: dashboard figures, delivery schedule, selling location, shop name and fund withdrawal
 * requests (legacy TokoSayaModel + the calls made inside the TokoSaya components).
 */
public class TokoSayaRepository extends BaseRepository {

    private final TokoSayaApi api;

    public TokoSayaRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(TokoSayaApi.class);
    }

    /** Legacy LocalStorage.getIdPenjualFromActiveUser(): the signed-in user's id as text, "0" when unknown. */
    public void getIdPenjual(ResultCallback<String> callback) {
        enqueue(api.getDetailUserId("id", authToken()),
                body -> String.valueOf(body == null || body.detailUser == null ? 0 : body.detailUser.id),
                result -> callback.onResult(Resource.success(result.isSuccess() ? result.data : "0")));
    }

    /** The shop detail; SUCCESS with null data when the server has no such seller. */
    public LiveData<Resource<DetailPenjual>> getDetailToko() {
        MutableLiveData<Resource<DetailPenjual>> live = new MutableLiveData<>(Resource.loading());
        getIdPenjual(idResult -> enqueue(api.getDetailPenjual(idResult.data, authToken()),
                body -> body == null ? null : body.detailPenjual, live::setValue));
        return live;
    }

    /** Account number and name of the signed-in seller (RequestPencairanDana prefill). */
    public LiveData<Resource<DetailPenjual>> getRekeningPenjual() {
        return load(api.getDetailPenjualEntities(session.getCurrentUserID(), "nomorRekening, namaUser"),
                body -> body == null ? null : body.detailPenjual);
    }

    public LiveData<Resource<Integer>> getTotalPenjualan() {
        return load(api.getTotalPenjualan(authToken()), body -> body == null ? 0 : body.totalPenjualan);
    }

    public LiveData<Resource<Integer>> getTotalSaldo() {
        return load(api.getTotalSaldo(authToken()), body -> body == null ? 0 : body.totalSaldo);
    }

    public LiveData<Resource<Integer>> getTagihan() {
        return load(api.getTagihan(authToken()), body -> body == null ? 0 : body.tagihan);
    }

    /** {produk, paket} product counts; SUCCESS with null data when the server answers false. */
    public LiveData<Resource<TokoSayaResponses.Jumlah>> getJumlahProduk() {
        return load(api.getJumlahProdukPenjual(authToken()), body -> body == null ? null : body.jumlahProduk);
    }

    /** Active delivery schedules: [pagi (code 1), siang (code 2), sore (code 3)]. */
    public LiveData<Resource<boolean[]>> getJadwalDelivery() {
        return load(api.getJadwalDeliveryPenjual(authToken()), body -> {
            boolean[] active = new boolean[3];
            if (body != null && body.jadwalDeliveryPenjual != null) {
                for (TokoSayaResponses.Jadwal jadwal : body.jadwalDeliveryPenjual) {
                    if (jadwal != null && jadwal.jadwalDeliveryCode >= 1 && jadwal.jadwalDeliveryCode <= 3) {
                        active[jadwal.jadwalDeliveryCode - 1] = true;
                    }
                }
            }
            return active;
        });
    }

    /** Switches one delivery schedule (code "1".."3") on/off; the result is the server's toggleStatus. */
    public void toggleJadwalDelivery(String jadwalDeliveryCode, ResultCallback<Boolean> callback) {
        enqueue(api.toggleJadwalDelivery(authToken(), jadwalDeliveryCode),
                body -> body != null && body.toggleStatus, callback);
    }

    /** Markets a seller can choose from. */
    public LiveData<Resource<List<Pasar>>> getPasarTersedia() {
        return load(api.getPasar("pasarTersedia"),
                body -> body == null || body.dataPasar == null ? Collections.<Pasar>emptyList() : body.dataPasar);
    }

    /** Legacy ADJDModel.reverseGeocoding: the formatted address, or a fallback text. Always SUCCESS. */
    public void reverseGeocoding(String latitude, String longitude, ResultCallback<String> callback) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude
                + "&key=" + session.getGoogleMapsApiKey();
        enqueue(api.reverseGeocode(url), body -> {
            if (body != null && body.results != null && !body.results.isEmpty()
                    && body.results.get(0) != null && body.results.get(0).formattedAddress != null) {
                return body.results.get(0).formattedAddress;
            }
            return "Alamat Tidak diketahui";
        }, result -> callback.onResult(Resource.success(result.isSuccess() ? result.data : "Alamat Tidak Diketahui")));
    }

    /**
     * Sets the new selling location: either a listed market ({@code idPasar}) or an own location
     * ({@code latitude}/{@code longitude}). The result is the server's statusUbahLokasiBerjualan.
     */
    public void ubahLokasiBerjualan(String idPenjual, String jenisPasarBaru, String idPasar, String latitude,
                                    String longitude, ResultCallback<Boolean> callback) {
        enqueue(api.ubahLokasiBerjualan(idPenjual, authToken(), jenisPasarBaru, idPasar, latitude, longitude),
                body -> body != null && body.statusUbahLokasiBerjualan,
                result -> callback.onResult(Resource.success(result.isSuccess() && Boolean.TRUE.equals(result.data))));
    }

    /** The result is statusUbahNamaToko (false for any failure, like the legacy). */
    public void ubahNamaToko(String namaToko, ResultCallback<Boolean> callback) {
        enqueue(api.ubahNamaToko(authToken(), namaToko), body -> body != null && body.statusUbahNamaToko,
                result -> callback.onResult(Resource.success(result.isSuccess() && Boolean.TRUE.equals(result.data))));
    }

    /** Files a fund withdrawal request; ERROR when the request itself failed, else the server's statusRequest. */
    public void ajukanRequestPencairan(String nomorRekening, String namaPenerima, String nominal,
                                       String keterangan, ResultCallback<Boolean> callback) {
        enqueue(api.ajukanRpd(authToken(), nomorRekening, namaPenerima, nominal, keterangan),
                body -> body != null && body.statusRequest, callback);
    }
}
