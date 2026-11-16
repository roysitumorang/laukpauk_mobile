package id.laukpauk.ui.account;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.account.WalletRecords;
import id.laukpauk.data.repository.UserRepository;

/** HistoriDompet: selected tab (0 = semua, 1 = referral, 2 = potongan transaksi) and its history. */
public class WalletHistoryViewModel extends ViewModel {

    private final UserRepository repository;
    private final MutableLiveData<Integer> type = new MutableLiveData<>(0);
    private final MediatorLiveData<Resource<List<WalletRecords>>> history = new MediatorLiveData<>();
    private LiveData<Resource<List<WalletRecords>>> source;
    private boolean loaded;

    public WalletHistoryViewModel(UserRepository repository, int initialType) {
        this.repository = repository;
        type.setValue(initialType);
    }

    public LiveData<Integer> getType() {
        return type;
    }

    public LiveData<Resource<List<WalletRecords>>> getHistory() {
        return history;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void selectType(int value) {
        Integer current = type.getValue();
        if (current == null || current != value) {
            type.setValue(value);
            load();
        }
    }

    public void load() {
        loaded = true;
        if (source != null) {
            history.removeSource(source);
        }
        Integer j = type.getValue();
        String param = j == null ? null : j == 1 ? UserRepository.WALLET_REFERRAL
                : j == 2 ? UserRepository.WALLET_TRANSACTION_DEDUCTION : UserRepository.WALLET_ALL;
        source = repository.getWalletRecords(param);
        history.addSource(source, history::setValue);
    }
}
