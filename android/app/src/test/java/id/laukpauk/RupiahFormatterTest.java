package id.laukpauk;

import org.junit.Test;
import static org.junit.Assert.*;

public class RupiahFormatterTest {
    @Test public void rupiahHasIndonesianCurrencyPrefix() {
        assertTrue(RupiahFormatter.rupiah(12500, 0).startsWith("Rp. "));
        assertTrue(RupiahFormatter.rupiah(12500, 0).contains("12.500"));
    }

    @Test public void quantityAppendsUnitOnlyWhenProvided() {
        assertEquals("2 kg", RupiahFormatter.quantity(2, "kg"));
        assertEquals("2", RupiahFormatter.quantity(2, ""));
        assertEquals("2", RupiahFormatter.quantity(2, null));
    }
}
