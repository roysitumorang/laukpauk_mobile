package id.laukpauk.ui.account;

import java.io.File;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.account.UserDetail;
import id.laukpauk.data.repository.AccountRepository;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.UserRepository;

/** State of the Profil Saya screen. */
public class ProfilSayaViewModel extends ViewModel {

    private final UserRepository users;
    private final AccountRepository account;

    private final MediatorLiveData<Resource<UserDetail>> profil = new MediatorLiveData<>();
    private LiveData<Resource<UserDetail>> profilSource;
    private final MutableLiveData<File> fotoBaru = new MutableLiveData<>();
    private final MutableLiveData<Boolean> savingFoto = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> savingProfil = new MutableLiveData<>(false);
    /** 0 = no pending number change, otherwise the id of the pending change. */
    private final MutableLiveData<Long> idPenggantian = new MutableLiveData<>();
    /** null = checking, otherwise today's number of changes. */
    private final MutableLiveData<Integer> jumlahGanti = new MutableLiveData<>();
    private boolean refillFields = true;
    private boolean loaded;

    public ProfilSayaViewModel(UserRepository users, AccountRepository account) {
        this.users = users;
        this.account = account;
        idPenggantian.setValue(account.getIdPenggantianNomor());
    }

    public LiveData<Resource<UserDetail>> getProfil() {
        return profil;
    }

    public LiveData<File> getFotoBaru() {
        return fotoBaru;
    }

    public LiveData<Boolean> getSavingFoto() {
        return savingFoto;
    }

    public LiveData<Boolean> getSavingProfil() {
        return savingProfil;
    }

    public LiveData<Long> getIdPenggantian() {
        return idPenggantian;
    }

    public LiveData<Integer> getJumlahGanti() {
        return jumlahGanti;
    }

    public boolean isLoaded() {
        return loaded;
    }

    /** True once after a save (the text fields must be refilled from the server); also true at first load. */
    public boolean consumeRefillFields() {
        boolean r = refillFields;
        refillFields = false;
        return r;
    }

    public void load() {
        loaded = true;
        if (profilSource != null) {
            profil.removeSource(profilSource);
        }
        profilSource = users.getDetailUser();
        profil.addSource(profilSource, profil::setValue);
        if (idPenggantian.getValue() == null || idPenggantian.getValue() == 0) {
            cekJumlahGanti();
        }
    }

    public void setFotoBaru(File file) {
        fotoBaru.setValue(file);
    }

    public void saveFoto(BaseRepository.ResultCallback<Boolean> callback) {
        File file = fotoBaru.getValue();
        if (file == null) {
            return;
        }
        savingFoto.setValue(true);
        users.updateFotoProfil(file, result -> {
            savingFoto.setValue(false);
            if (result.isSuccess() && Boolean.TRUE.equals(result.data)) {
                fotoBaru.setValue(null);
                load();
            }
            callback.onResult(result);
        });
    }

    public void saveProfil(String nama, String alamat, String username) {
        savingProfil.setValue(true);
        users.updateProfil(nama, alamat, username, result -> {
            savingProfil.setValue(false);
            if (result.isSuccess() && Boolean.TRUE.equals(result.data)) {
                refillFields = true;
                load();
            }
        });
    }

    public void logout(BaseRepository.ResultCallback<Boolean> callback) {
        account.logout(callback);
    }

    public void gantiPassword(String lama, String baru, String konfirmasi,
                              BaseRepository.ResultCallback<Boolean> callback) {
        users.gantiPassword(lama, baru, konfirmasi, callback);
    }

    // ------------------------------------------------------------------ phone number

    public void cekJumlahGanti() {
        jumlahGanti.setValue(null);
        account.jumlahGantiNomorHPForToday(result -> jumlahGanti.setValue(result.isSuccess() && result.data != null
                ? result.data : 0));
    }

    public void simpanNomorBaru(String nomor) {
        account.simpanNomorBaru(nomor, result -> {
            if (result.isSuccess() && result.data != null && result.data != 0) {
                idPenggantian.setValue(result.data);
            }
        });
    }

    public void batalNomor() {
        account.clearIdPenggantianNomor();
        idPenggantian.setValue(0L);
        cekJumlahGanti();
    }

    public void verifikasiNomor(String kode, BaseRepository.ResultCallback<Boolean> callback) {
        account.verifikasiNomor(kode, result -> {
            if (result.isSuccess() && Boolean.TRUE.equals(result.data)) {
                idPenggantian.setValue(0L);
            }
            callback.onResult(result);
        });
    }
}
