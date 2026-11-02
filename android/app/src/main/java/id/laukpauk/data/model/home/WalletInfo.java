package id.laukpauk.data.model.home;

/** Dompet / voucher / poin summary shown on the home page (signed-in users). */
public class WalletInfo {
    public final long totalDompet;
    public final int jumlahVoucher;
    public final long poin;

    public WalletInfo(long totalDompet, int jumlahVoucher, long poin) {
        this.totalDompet = totalDompet;
        this.jumlahVoucher = jumlahVoucher;
        this.poin = poin;
    }
}
