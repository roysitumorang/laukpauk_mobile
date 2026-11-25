package id.laukpauk.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import id.laukpauk.data.model.voucher.Voucher;
import id.laukpauk.data.model.voucher.VoucherResponses;

/** The voucher screens read {@code GET buyer/vouchers}: the detail is found by id, the count is the length. */
public class VoucherRepositoryTest {

    @Test
    public void detailIsTheVoucherWithThatId() {
        Voucher a = voucher("a");
        Voucher b = voucher("b");
        List<Voucher> all = Arrays.asList(a, b);
        assertSame(b, VoucherRepository.find(all, "b"));
        assertNull(VoucherRepository.find(all, "c"));
        assertNull(VoucherRepository.find(all, null));
    }

    @Test
    public void rowsAreNeverNull() {
        assertEquals(0, VoucherRepository.rows(null).size());
        assertEquals(0, VoucherRepository.rows(new VoucherResponses.BuyerVouchers()).size());
        VoucherResponses.BuyerVouchers body = new VoucherResponses.BuyerVouchers();
        body.data = Arrays.asList(voucher("a"), voucher("b"), voucher("c"));
        assertEquals(3, VoucherRepository.rows(body).size());
    }

    private static Voucher voucher(String id) {
        Voucher v = new Voucher();
        v.id = id;
        return v;
    }
}
