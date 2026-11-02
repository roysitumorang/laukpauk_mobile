package id.laukpauk.data.model.account;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Response envelopes of the account endpoints. */
public final class AccountResponses {
    private AccountResponses() {}

    public static class DetailUser {
        /** null when the server answers false ("no active user"). */
        @SerializedName("detailUser")
        public UserDetail detailUser;
    }

    public static class TotalDompet {
        @SerializedName("totalDompet")
        public long totalDompet;
    }

    public static class JumlahVoucher {
        @SerializedName("jumlahVoucher")
        public int jumlahVoucher;
    }

    public static class Point {
        /** null when the server answers false. */
        @SerializedName("response")
        public PointData response;
    }

    public static class PointData {
        @SerializedName("point")
        public long point;
    }

    public static class RiwayatDompetList {
        @SerializedName("riwayatDompet")
        public List<RiwayatDompet> riwayatDompet;
    }

    public static class RiwayatPointList {
        @SerializedName("riwayatPoint")
        public List<RiwayatPoint> riwayatPoint;
    }

    public static class UpdateProfil {
        @SerializedName("statusUpdateProfil")
        public boolean statusUpdateProfil;
    }

    public static class UpdateFoto {
        @SerializedName("saveFotoProfil")
        public boolean saveFotoProfil;
    }

    public static class GantiNomor {
        @SerializedName("statusSimpanNomorBaru")
        public boolean statusSimpanNomorBaru;
        @SerializedName("idPenggantianNomor")
        public long idPenggantianNomor;
    }

    public static class VerifikasiNomor {
        @SerializedName("statusVerifikasiNomor")
        public boolean statusVerifikasiNomor;
    }

    public static class JumlahGantiNomor {
        @SerializedName("jumlahGantiNomorHPForToday")
        public int jumlahGantiNomorHPForToday;
    }

    public static class GantiPassword {
        @SerializedName("statusGantiPassword")
        public boolean statusGantiPassword;
    }

    public static class RemoveToken {
        @SerializedName("removeToken")
        public boolean removeToken;
    }

    public static class ReferralConfiguration {
        /** null when the server answers false (referral not configured). */
        @SerializedName("referralConfiguration")
        public ReferralConfig referralConfiguration;
    }

    public static class ReferralConfig {
        @SerializedName("referralWallet")
        public long referralWallet;
    }

    public static class PasarList {
        @SerializedName("dataPasar")
        public List<Pasar> dataPasar;
    }

    public static class RequestPenjual {
        @SerializedName("statusRequest")
        public boolean statusRequest;
    }
}
