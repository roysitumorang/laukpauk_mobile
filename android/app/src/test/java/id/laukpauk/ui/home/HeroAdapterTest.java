package id.laukpauk.ui.home;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import id.laukpauk.util.ServerPath;

public class HeroAdapterTest {

    @Test
    public void photoUrl_upgradesHttpToHttps() {
        assertEquals("https://sadia.id/images/a.webp", HeroAdapter.photoUrl("http://sadia.id/images/a.webp"));
    }

    @Test
    public void photoUrl_keepsHttps() {
        assertEquals("https://sadia.id/images/a.webp", HeroAdapter.photoUrl("https://sadia.id/images/a.webp"));
    }

    @Test
    public void photoUrl_resolvesBarePathAgainstBaseUrl() {
        assertEquals(ServerPath.baseURL + "/images/a.webp", HeroAdapter.photoUrl("/images/a.webp"));
        assertEquals(ServerPath.baseURL + "/images/a.webp", HeroAdapter.photoUrl("images/a.webp"));
    }

    @Test
    public void photoUrl_emptyIsNull() {
        assertNull(HeroAdapter.photoUrl(null));
        assertNull(HeroAdapter.photoUrl(" "));
    }
}
