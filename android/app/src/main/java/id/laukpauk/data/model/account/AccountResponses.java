package id.laukpauk.data.model.account;

import com.squareup.moshi.Json;

import java.util.List;

/** Response envelopes of the account endpoints. */
public final class AccountResponses {
    private AccountResponses() {}

    /** {@code GET buyer/users/me}: the backend envelope; the user is in {@code data}. */
    public static class Me {
        @androidx.annotation.Nullable public UserDetail data;
    }

    public static class WalletRecordList {
        @Json(name = "riwayatDompet")
        public List<WalletRecords> walletRecords;
    }

    public static class PointRecordList {
        @Json(name = "riwayatPoint")
        public List<PointRecords> pointRecords;
    }

    public static class UpdateProfile {
        @Json(name = "statusUpdateProfil")
        public boolean updateProfileStatus;
    }

    public static class UpdatePhoto {
        @Json(name = "saveFotoProfil")
        public boolean saveProfilePhoto;
    }

    public static class ChangeNumber {
        @Json(name = "statusSimpanNomorBaru")
        public boolean saveNewNumberStatus;
        @Json(name = "idPenggantianNomor")
        public long numberChangeId;
    }

    public static class NumberVerification {
        @Json(name = "statusVerifikasiNomor")
        public boolean numberVerificationStatus;
    }

    public static class NumberChangeCount {
        @Json(name = "jumlahGantiNomorHPForToday")
        public int phoneNumberChangeCountForToday;
    }

    public static class ChangePassword {
        @Json(name = "statusGantiPassword")
        public boolean changePasswordStatus;
    }

    public static class RemoveToken {
        @Json(name = "removeToken")
        public boolean removeToken;
    }

    public static class ReferralConfiguration {
        /** null when the server answers false (referral not configured). */
        @Json(name = "referralConfiguration")
        public ReferralConfig referralConfiguration;
    }

    public static class ReferralConfig {
        @Json(name = "referralWallet")
        public long referralWallet;
    }
}
