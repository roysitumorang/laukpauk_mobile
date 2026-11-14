package id.laukpauk.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.voucher.Voucher;
import id.laukpauk.data.remote.VoucherApi;

/** The buyer's vouchers and the "voucher in use" choice stored in the session. */
public class VoucherRepository extends BaseRepository {

    private static final String LIST_FIELDS = "id, foto, minimumBelanja, berlakuMulai, berlakuSampai, maximumUsage";

    private final VoucherApi api;

    public VoucherRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(VoucherApi.class);
    }

    /** Active vouchers of the signed-in user (empty list when none). */
    public LiveData<Resource<List<Voucher>>> getVouchers() {
        return load(api.getVoucher(LIST_FIELDS),
                body -> body == null || body.listVoucher == null ? new ArrayList<Voucher>() : body.listVoucher);
    }

    /** One voucher; SUCCESS with null data when it does not exist. */
    public LiveData<Resource<Voucher>> getVoucherDetail(String idVoucher) {
        return load(api.getVoucherDetail(idVoucher), body -> body == null ? null : body.dataVoucher);
    }

    /** Id of the voucher the buyer chose to use, or null. */
    @Nullable
    public String usedVoucherId() {
        String used = session.getUsedVoucher();
        return used == null || used.isEmpty() ? null : used;
    }

    public void useVoucher(String idVoucher) {
        session.setUsedVoucher(idVoucher);
    }

    public void clearUsedVoucher() {
        session.clearUsedVoucher();
    }
}
