package id.laukpauk.data.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** The sign-in password is sent base64-encoded. */
public class AuthRepositoryTest {

    @Test
    public void passwordIsStandardBase64OfItsUtf8Bytes() {
        assertEquals("cmFoYXNpYTEyMw==", AuthRepository.base64("rahasia123"));
        assertEquals("cMOkc3N3w7ZyZA==", AuthRepository.base64("pässwörd"));
        assertEquals("Pz4/Pg==", AuthRepository.base64("?>?>"));
    }

    @Test
    public void emptyOrMissingPasswordEncodesToEmptyText() {
        assertEquals("", AuthRepository.base64(""));
        assertEquals("", AuthRepository.base64(null));
    }

    @Test
    public void loginValue_phoneNumbersBecomeE164() {
        assertEquals("+6281234567890", AuthRepository.loginValue("081234567890"));
        assertEquals("+6281234567890", AuthRepository.loginValue("6281234567890"));
        assertEquals("+6281234567890", AuthRepository.loginValue("81234567890"));
        assertEquals("+6281234567890", AuthRepository.loginValue("+62 812-3456-7890"));
        assertEquals("+6281234567890", AuthRepository.loginValue(" 0812 3456 7890 "));
    }

    @Test
    public void loginValue_usernameAndEmailAreLowerCased() {
        assertEquals("budi", AuthRepository.loginValue(" Budi "));
        assertEquals("budi@example.com", AuthRepository.loginValue("Budi@Example.com"));
    }
}
