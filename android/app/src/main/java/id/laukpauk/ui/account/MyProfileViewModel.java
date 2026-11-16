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
public class MyProfileViewModel extends ViewModel {

    private final UserRepository users;
    private final AccountRepository account;

    private final MediatorLiveData<Resource<UserDetail>> profile = new MediatorLiveData<>();
    private LiveData<Resource<UserDetail>> profileSource;
    private final MutableLiveData<File> photoNew = new MutableLiveData<>();
    private final MutableLiveData<Boolean> savingPhoto = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> savingProfile = new MutableLiveData<>(false);
    /** 0 = no pending number change, otherwise the id of the pending change. */
    private final MutableLiveData<Long> changeId = new MutableLiveData<>();
    /** null = checking, otherwise today's number of changes. */
    private final MutableLiveData<Integer> changeCount = new MutableLiveData<>();
    private boolean refillFields = true;
    private boolean loaded;

    public MyProfileViewModel(UserRepository users, AccountRepository account) {
        this.users = users;
        this.account = account;
        changeId.setValue(account.getNumberChangeId());
    }

    public LiveData<Resource<UserDetail>> getProfile() {
        return profile;
    }

    public LiveData<File> getPhotoNew() {
        return photoNew;
    }

    public LiveData<Boolean> getSavingPhoto() {
        return savingPhoto;
    }

    public LiveData<Boolean> getSavingProfile() {
        return savingProfile;
    }

    public LiveData<Long> getChangeId() {
        return changeId;
    }

    public LiveData<Integer> getChangeCount() {
        return changeCount;
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
        if (profileSource != null) {
            profile.removeSource(profileSource);
        }
        profileSource = users.getDetailUser();
        profile.addSource(profileSource, profile::setValue);
        if (changeId.getValue() == null || changeId.getValue() == 0) {
            checkChangeCount();
        }
    }

    public void setPhotoNew(File file) {
        photoNew.setValue(file);
    }

    public void savePhoto(BaseRepository.ResultCallback<Boolean> callback) {
        File file = photoNew.getValue();
        if (file == null) {
            return;
        }
        savingPhoto.setValue(true);
        users.updateProfilePhoto(file, result -> {
            savingPhoto.setValue(false);
            if (result.isSuccess() && Boolean.TRUE.equals(result.data)) {
                photoNew.setValue(null);
                load();
            }
            callback.onResult(result);
        });
    }

    public void saveProfile(String name, String address, String username) {
        savingProfile.setValue(true);
        users.updateProfile(name, address, username, result -> {
            savingProfile.setValue(false);
            if (result.isSuccess() && Boolean.TRUE.equals(result.data)) {
                refillFields = true;
                load();
            }
        });
    }

    public void logout(BaseRepository.ResultCallback<Boolean> callback) {
        account.logout(callback);
    }

    public void changePassword(String oldPassword, String newPassword, String confirm,
                              BaseRepository.ResultCallback<Boolean> callback) {
        users.changePassword(oldPassword, newPassword, confirm, callback);
    }

    // ------------------------------------------------------------------ phone number

    public void checkChangeCount() {
        changeCount.setValue(null);
        account.phoneNumberChangeCountForToday(result -> changeCount.setValue(result.isSuccess() && result.data != null
                ? result.data : 0));
    }

    public void newNumberSave(String number) {
        account.newNumberSave(number, result -> {
            if (result.isSuccess() && result.data != null && result.data != 0) {
                changeId.setValue(result.data);
            }
        });
    }

    public void cancelNumber() {
        account.clearNumberChangeId();
        changeId.setValue(0L);
        checkChangeCount();
    }

    public void numberVerification(String code, BaseRepository.ResultCallback<Boolean> callback) {
        account.numberVerification(code, result -> {
            if (result.isSuccess() && Boolean.TRUE.equals(result.data)) {
                changeId.setValue(0L);
            }
            callback.onResult(result);
        });
    }
}
