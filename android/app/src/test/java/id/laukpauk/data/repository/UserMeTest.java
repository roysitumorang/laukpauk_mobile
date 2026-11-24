package id.laukpauk.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import id.laukpauk.data.model.account.AccountResponses;
import id.laukpauk.data.model.account.UserDetail;
import id.laukpauk.data.remote.ApiClient;

/** {@code GET buyer/users/me}: the user, including the wallet balance. */
public class UserMeTest {

    private static AccountResponses.Me parse(String json) throws Exception {
        return ApiClient.createMoshi().adapter(AccountResponses.Me.class).fromJson(json);
    }

    @Test
    public void readsTheBuyerAndWallet() throws Exception {
        UserDetail u = UserRepository.me(parse("{\"status_code\":200,\"data\":{\"id\":\"u1\",\"role\":\"buyer\","
                + "\"name\":\"Budi\",\"mobile_phone_confirmed_at\":\"2026-11-21T16:30:00+07:00\","
                + "\"mobile_phone\":\"+6281234567890\",\"username\":\"budi\",\"email\":null,"
                + "\"photo\":\"\",\"user_level\":\"basic\",\"referral_code\":\"BUDI01\","
                + "\"address\":\"Jl. Merdeka 1\",\"referral_points\":0,\"reward_points\":10,"
                + "\"wallet\":25000,\"created_at\":\"2026-11-19T10:00:00+07:00\"}}"));
        assertEquals("Budi", u.name);
        assertEquals("+6281234567890", u.phoneNumber);
        assertEquals("Jl. Merdeka 1", u.address);
        assertEquals("BUDI01", u.referralCode);
        assertEquals(25000L, u.walletOrZero());
    }

    @Test
    public void missingWalletIsZero() throws Exception {
        UserDetail u = UserRepository.me(parse("{\"data\":{\"id\":\"u1\",\"name\":\"Budi\",\"wallet\":null}}"));
        assertEquals(0L, u.walletOrZero());
        assertNull(UserRepository.me(parse("{\"status_code\":200}")));
        assertNull(UserRepository.me(null));
    }
}
