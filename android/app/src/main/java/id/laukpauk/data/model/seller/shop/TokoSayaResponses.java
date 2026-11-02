package id.laukpauk.data.model.seller.shop;

import com.squareup.moshi.Json;

import java.util.List;

/** Response envelopes of the shop-management endpoints. */
public final class TokoSayaResponses {
    private TokoSayaResponses() {}

    public static class DetailUser {
        @Json(name = "detailUser")
        public IdOnly detailUser;
    }

    public static class IdOnly {
        @Json(name = "id")
        public int id;
    }

    public static class Detail {
        /** null when the server answers false. */
        @Json(name = "detailPenjual")
        public DetailPenjual detailPenjual;
    }

    public static class TotalPenjualan {
        @Json(name = "totalPenjualan")
        public int totalPenjualan;
    }

    public static class TotalSaldo {
        @Json(name = "totalSaldo")
        public int totalSaldo;
    }

    public static class Tagihan {
        @Json(name = "tagihan")
        public int tagihan;
    }

    public static class JumlahProduk {
        @Json(name = "jumlahProduk")
        public Jumlah jumlahProduk;
    }

    public static class Jumlah {
        @Json(name = "produk")
        public int produk;
        @Json(name = "paket")
        public int paket;
    }

    public static class JadwalList {
        @Json(name = "jadwalDeliveryPenjual")
        public List<Jadwal> jadwalDeliveryPenjual;
    }

    public static class Jadwal {
        @Json(name = "jadwalDeliveryCode")
        public int jadwalDeliveryCode;
    }

    public static class Toggle {
        @Json(name = "toggleStatus")
        public boolean toggleStatus;
    }

    public static class PasarList {
        @Json(name = "dataPasar")
        public List<Pasar> dataPasar;
    }

    public static class UbahLokasi {
        @Json(name = "statusUbahLokasiBerjualan")
        public boolean statusUbahLokasiBerjualan;
    }

    public static class UbahNama {
        @Json(name = "statusUbahNamaToko")
        public boolean statusUbahNamaToko;
    }

    public static class AjukanRpd {
        @Json(name = "statusRequest")
        public boolean statusRequest;
    }

    public static class Geocode {
        @Json(name = "results")
        public List<GeocodeResult> results;
    }

    public static class GeocodeResult {
        @Json(name = "formatted_address")
        public String formattedAddress;
    }

    public static class HistoriList {
        @Json(name = "historiPenjualan")
        public List<HistoriPenjualan> historiPenjualan;
    }

    public static class TotalTransaksi {
        @Json(name = "totalTransaksi")
        public int totalTransaksi;
    }
}
