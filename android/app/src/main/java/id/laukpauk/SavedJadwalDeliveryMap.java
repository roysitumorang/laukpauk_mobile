package id.laukpauk;

class SavedJadwalDeliveryMap {
    String currentDate;
    String currentTime;
    String id;
    String kode;
    String day;
    String rentangWaktu;
    String sessi;
    String minTime;
    String maxTime;
    boolean isFull;
    int quota;
    int jumlahTransaksi;

    public SavedJadwalDeliveryMap(
            String currentDateData,
            String currentTimeData, String idData, String kodeData,
            String dayData, String rentangWaktuData, String sessiData,
            String minTimeData, String maxTimeData,
            boolean isFullData, int quotaData, int jumlahTransaksiData){
        currentDate     =   currentDateData;
        currentTime     =   currentTimeData;
        id      =   idData;
        kode    =   kodeData;
        day     =   dayData;
        rentangWaktu    =   rentangWaktuData;
        sessi   =   sessiData;
        minTime =   minTimeData;
        maxTime =   maxTimeData;
        isFull  =   isFullData;
        quota   =   quotaData;
        jumlahTransaksi =   jumlahTransaksiData;
    }
}
