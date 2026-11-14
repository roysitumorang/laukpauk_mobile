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

    public static class TotalDompet {
        @Json(name = "totalDompet")
        public long totalDompet;
    }

    public static class JumlahVoucher {
        @Json(name = "jumlahVoucher")
        public int jumlahVoucher;
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

    public static class RiwayatDompetList {
        @Json(name = "riwayatDompet")
        public List<RiwayatDompet> riwayatDompet;
    }

    public static class RiwayatPointList {
        @Json(name = "riwayatPoint")
        public List<RiwayatPoint> riwayatPoint;
    }

    public static class UpdateProfil {
        @Json(name = "statusUpdateProfil")
        public boolean statusUpdateProfil;
    }

    public static class UpdateFoto {
        @Json(name = "saveFotoProfil")
        public boolean saveFotoProfil;
    }

    public static class GantiNomor {
        @Json(name = "statusSimpanNomorBaru")
        public boolean statusSimpanNomorBaru;
        @Json(name = "idPenggantianNomor")
        public long idPenggantianNomor;
    }

    public static class VerifikasiNomor {
        @Json(name = "statusVerifikasiNomor")
        public boolean statusVerifikasiNomor;
    }

    public static class JumlahGantiNomor {
        @Json(name = "jumlahGantiNomorHPForToday")
        public int jumlahGantiNomorHPForToday;
    }

    public static class GantiPassword {
        @Json(name = "statusGantiPassword")
        public boolean statusGantiPassword;
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
