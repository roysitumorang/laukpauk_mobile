package id.laukpauk.ui.checkout;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** The wallet is always on and has no minimum total: it pays what is left, up to the balance. */
public class WalletDeductionTest {

    @Test
    public void paysEverythingWhenTheBalanceIsEnough() {
        assertEquals(14000, CheckoutViewModel.walletDeduction(25000, 14000));
    }

    @Test
    public void paysUpToTheBalance() {
        assertEquals(10000, CheckoutViewModel.walletDeduction(10000, 14000));
    }

    @Test
    public void paysNothingWithoutBalanceOrDue() {
        assertEquals(0, CheckoutViewModel.walletDeduction(0, 14000));
        assertEquals(0, CheckoutViewModel.walletDeduction(10000, 0));
        assertEquals(0, CheckoutViewModel.walletDeduction(10000, -500));
    }
}
