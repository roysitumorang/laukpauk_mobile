package id.laukpauk.data.model.payment;

import com.squareup.moshi.Json;

/** paymentResponse of a tf_bca order. */
public class TransferBankPayment {
    @Json(name = "totalBayar")
    public long totalPayment;
    @Json(name = "kodeUnik")
    public long uniqueCode;
    @Json(name = "nomorRekening")
    public String bankAccountNumber;
    @Json(name = "pemilikNomorRekening")
    public String bankAccountOwner;
}
