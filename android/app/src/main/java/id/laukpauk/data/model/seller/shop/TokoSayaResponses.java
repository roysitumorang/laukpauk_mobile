package id.laukpauk.data.model.seller.shop;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Response envelopes of the shop-management endpoints. */
public final class TokoSayaResponses {
    private TokoSayaResponses() {}

    public static class DetailUser {
        @SerializedName("detailUser")
        public IdOnly detailUser;
    }

    public static class IdOnly {
        @SerializedName("id")
        public int id;
    }

    public static class Detail {
        /** null when the server answers false. */
        @SerializedName("detailPenjual")
        public DetailPenjual detailPenjual;
    }

    public static class TotalPenjualan {
        @SerializedName("totalPenjualan")
        public int totalPenjualan;
    }

    public static class TotalSaldo {
        @SerializedName("totalSaldo")
        public int totalSaldo;
    }

    public static class Tagihan {
        @SerializedName("tagihan")
        public int tagihan;
    }

    public static class JumlahProduk {
        @SerializedName("jumlahProduk")
        public Jumlah jumlahProduk;
    }

    public static class Jumlah {
        @SerializedName("produk")
        public int produk;
        @SerializedName("paket")
        public int paket;
    }

    public static class JadwalList {
        @SerializedName("jadwalDeliveryPenjual")
        public List<Jadwal> jadwalDeliveryPenjual;
    }

    public static class Jadwal {
        @SerializedName("jadwalDeliveryCode")
        public int jadwalDeliveryCode;
    }

    public static class Toggle {
        @SerializedName("toggleStatus")
        public boolean toggleStatus;
    }

    public static class PasarList {
        @SerializedName("dataPasar")
        public List<Pasar> dataPasar;
    }

    public static class UbahLokasi {
        @SerializedName("statusUbahLokasiBerjualan")
        public boolean statusUbahLokasiBerjualan;
    }

    public static class UbahNama {
        @SerializedName("statusUbahNamaToko")
        public boolean statusUbahNamaToko;
    }

    public static class AjukanRpd {
        @SerializedName("statusRequest")
        public boolean statusRequest;
    }

    public static class Geocode {
        @SerializedName("results")
        public List<GeocodeResult> results;
    }

    public static class GeocodeResult {
        @SerializedName("formatted_address")
        public String formattedAddress;
    }

    public static class HistoriList {
        @SerializedName("historiPenjualan")
        public List<HistoriPenjualan> historiPenjualan;
    }

    public static class TotalTransaksi {
        @SerializedName("totalTransaksi")
        public int totalTransaksi;
    }
}
