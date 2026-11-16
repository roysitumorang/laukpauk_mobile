package id.laukpauk.data.model.home;

import com.squareup.moshi.Json;

/** Dompet / voucher / poin summary shown on the home page (signed-in users). */
public class WalletInfo {
    @Json(name = "totalDompet")
    public final long walletTotal;
    @Json(name = "jumlahVoucher")
    public final int voucherCount;
    @Json(name = "poin")
    public final long points;

    public WalletInfo(long walletTotal, int voucherCount, long points) {
        this.walletTotal = walletTotal;
        this.voucherCount = voucherCount;
        this.points = points;
    }
}
