package id.laukpauk;

class TokenizationArgument {
    int totalTagihan;
    String cardNumber;
    String expiryMonth;
    String expiryYear;
    String cardCVN;

    public TokenizationArgument(int totalTagihanData, String cardNumberData, String expiryMonthData, String expiryYearData, String cardCVNData){
        totalTagihan    =   totalTagihanData;
        cardNumber      =   cardNumberData;
        expiryMonth     =   expiryMonthData;
        expiryYear      =   expiryYearData;
        cardCVN         =   cardCVNData;
    }
}
