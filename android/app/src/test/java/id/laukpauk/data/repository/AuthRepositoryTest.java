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
}
