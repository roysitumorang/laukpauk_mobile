package id.laukpauk.ui.voucher;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.voucher.Voucher;
import id.laukpauk.data.repository.VoucherRepository;

/** The buyer's voucher list. */
public class VoucherViewModel extends ViewModel {

    private final VoucherRepository repository;
    private final MediatorLiveData<Resource<List<Voucher>>> vouchers = new MediatorLiveData<>();
    private LiveData<Resource<List<Voucher>>> source;

    public VoucherViewModel(VoucherRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<List<Voucher>>> getVouchers() {
        return vouchers;
    }

    public boolean isLoaded() {
        return source != null;
    }

    /** First open and pull-to-refresh. */
    public void load() {
        if (source != null) {
            vouchers.removeSource(source);
        }
        source = repository.getVouchers();
        vouchers.addSource(source, vouchers::setValue);
    }
}
