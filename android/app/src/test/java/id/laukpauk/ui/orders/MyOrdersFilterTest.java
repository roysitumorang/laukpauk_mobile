package id.laukpauk.ui.orders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.TimeZone;

import id.laukpauk.data.model.orders.BuyerTransaction;

/** The list's filters: tab statuses and the created_at bounds; the status steps. */
public class MyOrdersFilterTest {

    private TimeZone saved;

    @Before
    public void jakarta() {
        saved = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Jakarta"));
    }

    @After
    public void restore() {
        TimeZone.setDefault(saved);
    }

    @Test
    public void tabsFilterByStatus() {
        assertEquals(0, MyOrdersViewModel.TAB_TODAY);
        assertNull(MyOrdersViewModel.TAB_STATUS[0]); // "Hari Ini": the dropdown's status
        assertNull(MyOrdersViewModel.TAB_STATUS[1]);
        assertEquals(Integer.valueOf(0), MyOrdersViewModel.TAB_STATUS[2]);
        assertEquals(Integer.valueOf(2), MyOrdersViewModel.TAB_STATUS[3]);
        assertEquals(Integer.valueOf(5), MyOrdersViewModel.TAB_STATUS[4]);
        assertEquals(Integer.valueOf(6), MyOrdersViewModel.TAB_STATUS[5]);
        assertEquals(Integer.valueOf(8), MyOrdersViewModel.TAB_STATUS[6]);
        assertEquals(Integer.valueOf(1), MyOrdersViewModel.TAB_STATUS[7]);
    }

    @Test
    public void datesAreTheWholeLocalDaysInUtc() {
        assertEquals("2026-11-24T17:00:00Z", MyOrdersViewModel.dayStart("2026-11-25"));
        assertEquals("2026-11-26T16:59:59Z", MyOrdersViewModel.dayEnd("2026-11-26"));
        assertNull(MyOrdersViewModel.dayStart(""));
        assertNull(MyOrdersViewModel.dayEnd("not a day"));
    }

    @Test
    public void steps() {
        assertEquals("", OrderUi.step(BuyerTransaction.STATUS_PENDING));
        assertEquals("approved", OrderUi.step(BuyerTransaction.STATUS_PAID));
        assertEquals("processing", OrderUi.step(BuyerTransaction.STATUS_PROCESSED));
        assertEquals("otw", OrderUi.step(BuyerTransaction.STATUS_SHIPPED));
        assertEquals("received", OrderUi.step(BuyerTransaction.STATUS_COMPLETED));
        assertEquals("", OrderUi.step(BuyerTransaction.STATUS_CANCELLED));
    }

    @Test
    public void readsRfc3339WithFractionsAndOffset() {
        assertEquals("26 November 2026 07:00", OrderUi.format("2026-11-26T07:00:00.123+07:00", "d MMMM yyyy HH:mm"));
        assertEquals("26 November 2026 07:00", OrderUi.format("2026-11-26T00:00:00Z", "d MMMM yyyy HH:mm"));
        assertEquals("", OrderUi.format(null, "HH:mm"));
    }
}
