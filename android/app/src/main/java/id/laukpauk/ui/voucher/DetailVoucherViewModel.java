package id.laukpauk.ui.voucher;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.voucher.Voucher;
import id.laukpauk.data.repository.VoucherRepository;

/** One voucher and whether it is the voucher currently chosen for checkout. */
public class DetailVoucherViewModel extends ViewModel {

    private final VoucherRepository repository;
    private final String idVoucher;
    private final MediatorLiveData<Resource<Voucher>> detail = new MediatorLiveData<>();
    private final MutableLiveData<String> usedVoucherId = new MutableLiveData<>();
    private LiveData<Resource<Voucher>> source;

    public DetailVoucherViewModel(VoucherRepository repository, String idVoucher) {
        this.repository = repository;
        this.idVoucher = idVoucher;
    }

    public LiveData<Resource<Voucher>> getDetail() {
        return detail;
    }

    /** Id of the voucher in use (null when none). */
    public LiveData<String> getUsedVoucherId() {
        return usedVoucherId;
    }

    public boolean isLoaded() {
        return source != null;
    }

    public boolean isThisVoucherUsed() {
        String used = usedVoucherId.getValue();
        return used != null && used.equals(idVoucher);
    }

    public void load() {
        usedVoucherId.setValue(repository.usedVoucherId());
        if (source != null) {
            detail.removeSource(source);
        }
        source = repository.getVoucherDetail(idVoucher);
        detail.addSource(source, detail::setValue);
    }

    /** "Gunakan Voucher" / "Batal Gunakan Voucher". Returns true when the voucher is now in use. */
    public boolean toggle() {
        boolean nowUsed = !isThisVoucherUsed();
        if (nowUsed) {
            repository.useVoucher(idVoucher);
        } else {
            repository.clearUsedVoucher();
        }
        load();
        return nowUsed;
    }

    @Nullable
    public String getIdVoucher() {
        return idVoucher;
    }
}
