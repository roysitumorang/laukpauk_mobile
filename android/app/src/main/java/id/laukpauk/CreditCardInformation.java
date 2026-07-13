package id.laukpauk;

class CreditCardInformation{
    String cardNumber = "";
    String expiryMonth = "";
    String expiryYear = "";
    String cardCVN = "";

    public CreditCardInformation(String cardNumberData, String expiryMonthData, String expiryYearData, String cardCVNData){
        cardNumber      =   cardNumberData;
        expiryMonth     =   expiryMonthData;
        expiryYear      =   expiryYearData;
        cardCVN         =   cardCVNData;
    }
}