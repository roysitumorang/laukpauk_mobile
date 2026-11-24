package id.laukpauk.data.model.setting;

import androidx.annotation.Nullable;

/** Envelopes of the {@code buyer/setting} endpoint (the payload is in {@code data}). */
public final class SettingResponses {
    private SettingResponses() {}

    /** {@code GET buyer/setting}. */
    public static class Buyer {
        @Nullable public BuyerSetting data;
    }
}
