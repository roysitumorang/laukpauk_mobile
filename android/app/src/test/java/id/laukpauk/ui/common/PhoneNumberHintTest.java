package id.laukpauk.ui.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** The Phone Number Hint API returns E.164 numbers; the app's fields use the local 08... format. */
public class PhoneNumberHintTest {

    @Test
    public void indonesianNumbersGetTheLeadingZero() {
        assertEquals("081234567890", PhoneNumberHint.toLocal("+6281234567890"));
        assertEquals("081234567890", PhoneNumberHint.toLocal("+62 812-3456-7890"));
        assertEquals("081234567890", PhoneNumberHint.toLocal("6281234567890"));
    }

    @Test
    public void localAndForeignNumbersKeepTheirDigits() {
        assertEquals("081234567890", PhoneNumberHint.toLocal("081234567890"));
        assertEquals("6591234567", PhoneNumberHint.toLocal("+65 9123 4567"));
    }

    @Test
    public void nothingPickedIsEmpty() {
        assertEquals("", PhoneNumberHint.toLocal(null));
        assertEquals("", PhoneNumberHint.toLocal(""));
    }
}
