package id.laukpauk.ui.account;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import id.laukpauk.data.repository.SettingRepository;

/** HubungiKami: whether the server has the WhatsApp contact configuration (null = still loading). */
public class HubungiKamiViewModel extends ViewModel {

    private final MutableLiveData<Boolean> available = new MutableLiveData<>();

    public HubungiKamiViewModel(SettingRepository settings) {
        settings.getContactUsWhatsappConfiguration(result ->
                available.setValue(result.isSuccess() && result.data != null));
    }

    public LiveData<Boolean> getAvailable() {
        return available;
    }
}
