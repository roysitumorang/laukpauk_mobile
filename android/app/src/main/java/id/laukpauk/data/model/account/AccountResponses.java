package id.laukpauk.data.model.account;

import com.squareup.moshi.Json;

import java.util.List;

/** Response envelopes of the account endpoints. */
public final class AccountResponses {
    private AccountResponses() {}

    public static class DetailUser {
        /** null when the server answers false ("no active user"). */
        @Json(name = "detailUser")
        public UserDetail detailUser;
    }

    public static class WalletTotal {
        @Json(name = "totalDompet")
        @Json(name = "totalDompet")
        public long walletTotal;
    }

    public static class VoucherCount {
        @Json(name = "jumlahVoucher")
        @Json(name = "jumlahVoucher")
        public int voucherCount;
    }

    public static class Point {
        /** null when the server answers false. */
        @Json(name = "response")
        public PointData response;
    }

    public static class PointData {
        @Json(name = "point")
        public long point;
    }

    public static class WalletRecordList {
        @Json(name = "riwayatDompet")
        @Json(name = "riwayatDompet")
        public List<WalletRecords> walletRecords;
    }

    public static class PointRecordList {
        @Json(name = "riwayatPoint")
        @Json(name = "riwayatPoint")
        public List<PointRecords> pointRecords;
    }

    public static class UpdateProfile {
        @Json(name = "statusUpdateProfil")
        @Json(name = "statusUpdateProfil")
        public boolean updateProfileStatus;
    }

    public static class UpdatePhoto {
        @Json(name = "saveFotoProfil")
        @Json(name = "saveFotoProfil")
        public boolean saveProfilePhoto;
    }

    public static class ChangeNumber {
        @Json(name = "statusSimpanNomorBaru")
        @Json(name = "statusSimpanNomorBaru")
        public boolean saveNewNumberStatus;
        @Json(name = "idPenggantianNomor")
        @Json(name = "idPenggantianNomor")
        public long numberChangeId;
    }

    public static class NumberVerification {
        @Json(name = "statusVerifikasiNomor")
        @Json(name = "statusVerifikasiNomor")
        public boolean numberVerificationStatus;
    }

    public static class NumberChangeCount {
        @Json(name = "jumlahGantiNomorHPForToday")
        @Json(name = "jumlahGantiNomorHPForToday")
        public int phoneNumberChangeCountForToday;
    }

    public static class ChangePassword {
        @Json(name = "statusGantiPassword")
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
