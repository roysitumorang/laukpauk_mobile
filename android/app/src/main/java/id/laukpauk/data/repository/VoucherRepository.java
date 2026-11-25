package id.laukpauk.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.voucher.Voucher;
import id.laukpauk.data.model.voucher.VoucherResponses;
import id.laukpauk.data.model.cart.CartResponses;
import id.laukpauk.data.remote.CartApi;
import id.laukpauk.data.remote.VoucherApi;

/** The buyer's vouchers ({@code GET buyer/vouchers}) and the "voucher in use" choice stored in the session. */
public class VoucherRepository extends BaseRepository {

    private final VoucherApi api;
    private final CartApi cartApi;

    public VoucherRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(VoucherApi.class);
        this.cartApi = api(CartApi.class);
    }

    /** The buyer's active vouchers (empty list when none). */
    public LiveData<Resource<List<Voucher>>> getVouchers() {
        return load(api.buyerVouchers(null), VoucherRepository::rows);
    }

    /** One voucher ({@code GET buyer/vouchers/:id}); ERROR 404 when it is not there. */
    public LiveData<Resource<Voucher>> getVoucherDetail(String idVoucher) {
        return load(api.buyerVoucher(idVoucher), (VoucherResponses.BuyerVoucher body) -> body == null ? null : body.data);
    }

    /**
     * How many vouchers the buyer has for the cart grand total: the length of {@code data} of
     * {@code GET buyer/vouchers?purchase=<total of GET buyer/cart>}. ERROR when a request failed (a 404 is 0).
     */
    public void countForCart(ResultCallback<Long> callback) {
        enqueue(cartApi.buyerCart(),
                (CartResponses.BuyerCartEnvelope body) -> body == null || body.data == null ? 0L : body.data.total(),
                cart -> {
                    if (!cart.isSuccess()) {
                        callback.onResult(Resource.error(cart.message, cart.code));
                        return;
                    }
                    long total = cart.data == null ? 0L : cart.data;
                    enqueue(api.buyerVouchers(total), body -> (long) rows(body).size(), r -> {
                        if (!r.isSuccess() && r.code == 404) {
                            callback.onResult(Resource.success(0L));
                        } else {
                            callback.onResult(r);
                        }
                    });
                });
    }

    /** {@code data} of {@code GET buyer/vouchers}, never null. */
    public static List<Voucher> rows(@Nullable VoucherResponses.BuyerVouchers body) {
        return body == null || body.data == null ? new ArrayList<Voucher>() : body.data;
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
