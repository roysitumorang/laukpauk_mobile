package id.laukpauk;

import android.text.TextUtils;
import java.net.URI;

/** Build-time API endpoint configuration. Supply LAUKPAUK_API_BASE_URL via
 * Gradle property or environment; never embed production credentials here. */
public final class ApiConfiguration {
    private ApiConfiguration() { }

    public static String baseUrl() {
        String value = BuildConfig.API_BASE_URL == null ? "" : BuildConfig.API_BASE_URL.trim();
        if (TextUtils.isEmpty(value)) {
            throw new IllegalStateException(
                    "Missing LAUKPAUK_API_BASE_URL. Set it in your local Gradle properties or build environment.");
        }
        URI uri = URI.create(value);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
            throw new IllegalStateException("LAUKPAUK_API_BASE_URL must be an absolute HTTPS URL.");
        }
        return value.replaceAll("/+$", "");
    }
}
