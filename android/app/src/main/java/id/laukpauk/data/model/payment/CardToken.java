package id.laukpauk.data.model.payment;

/** Xendit single-use token; its JSON text is the moreDataMetodePembayaran of a credit card order. */
public class CardToken {
    public String tokenID;
    public String authenticationID;
    public String cardCVN;
}
