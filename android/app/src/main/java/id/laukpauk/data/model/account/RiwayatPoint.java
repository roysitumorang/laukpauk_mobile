package id.laukpauk.data.model.account;

/** One points history row (User/getRiwayatPoint). createdFrom is "admin" or "transaksi". */
public class RiwayatPoint {
    public static final String CREATED_FROM_TRANSAKSI = "transaksi";

    public String keterangan;
    public String createdFrom;
    public long nilai;
}
