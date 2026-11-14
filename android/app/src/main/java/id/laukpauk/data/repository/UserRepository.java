package id.laukpauk.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.account.AccountResponses;
import id.laukpauk.data.model.account.RiwayatDompet;
import id.laukpauk.data.model.account.RiwayatPoint;
import id.laukpauk.data.model.account.UserDetail;
import id.laukpauk.data.remote.UserApi;

/** The signed-in user's profile, wallet, vouchers and points (legacy UserModel / ProfilSayaModel / PointModel). */
public class UserRepository extends BaseRepository {

    public static final String DOMPET_SEMUA = null;
    public static final String DOMPET_REFERRAL = "referral";
    public static final String DOMPET_POTONGAN_TRANSAKSI = "potongantransaksi";

    private final UserApi api;

    public UserRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(UserApi.class);
    }

    /** The active user; data is null when the server has no active user ("Data Akun Anda tidak Ditemukan"). */
    public LiveData<Resource<UserDetail>> getDetailUser() {
        return load(api.getDetailUser(null), body -> body == null ? null : body.detailUser);
    }

    public void getDetailUser(ResultCallback<UserDetail> callback) {
        enqueue(api.getDetailUser(null), body -> body == null ? null : body.detailUser, callback);
    }

    public LiveData<Resource<Long>> getTotalDompet() {
        return load(api.getTotalDompet(), body -> body == null ? 0L : body.totalDompet);
    }

    public LiveData<Resource<Integer>> getJumlahVoucher() {
        return load(api.getJumlahVoucher(), body -> body == null ? 0 : body.jumlahVoucher);
    }

    public LiveData<Resource<Long>> getPoint() {
        return load(api.getPoint("true"),
                (AccountResponses.Point body) -> body == null || body.response == null ? 0L : body.response.point);
    }

    /** {@code jenis}: DOMPET_SEMUA (null), DOMPET_REFERRAL or DOMPET_POTONGAN_TRANSAKSI. */
    public LiveData<Resource<List<RiwayatDompet>>> getRiwayatDompet(@Nullable String jenis) {
        return load(api.getRiwayatDompet(jenis),
                (AccountResponses.RiwayatDompetList body) ->
                        body == null || body.riwayatDompet == null ? new ArrayList<RiwayatDompet>() : body.riwayatDompet);
    }

    public LiveData<Resource<List<RiwayatPoint>>> getRiwayatPoint() {
        return load(api.getRiwayatPoint(),
                (AccountResponses.RiwayatPointList body) ->
                        body == null || body.riwayatPoint == null ? new ArrayList<RiwayatPoint>() : body.riwayatPoint);
    }

    /** POST User/updateProfil; true when the server saved the changes. */
    public void updateProfil(String namaLengkap, String alamat, String username, ResultCallback<Boolean> callback) {
        enqueue(api.updateProfil(body("namaLengkap", namaLengkap, "alamat", alamat, "username", username)),
                (AccountResponses.UpdateProfil body) -> body != null && body.statusUpdateProfil, callback);
    }

    /** POST User/updateFotoProfil with the image as base64 (encoded on the disk thread). */
    public void updateFotoProfil(java.io.File image, ResultCallback<Boolean> callback) {
        executors.diskIO().execute(() -> {
            String base64 = encode(image);
            executors.mainThread().execute(() -> enqueue(api.updateFotoProfil(body("fotoProfil", base64)),
                    (AccountResponses.UpdateFoto body) -> body != null && body.saveFotoProfil, callback));
        });
    }

    /** POST User/gantiPassword; true when changed. */
    public void gantiPassword(String lama, String baru, String konfirmasi, ResultCallback<Boolean> callback) {
        enqueue(api.gantiPassword(body("passwordLama", lama, "passwordBaru", baru, "konfirmasiPasswordBaru",
                konfirmasi)),
                (AccountResponses.GantiPassword body) -> body != null && body.statusGantiPassword, callback);
    }

    private static String encode(java.io.File file) {
        try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            return android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.NO_WRAP);
        } catch (java.io.IOException e) {
            return "";
        }
    }
}
