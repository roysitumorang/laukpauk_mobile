class CheckoutGlobalState {
  bool isCheckoutFormCompleted = false;
  bool isWalletOn = false;
  bool isPointUsed = false;
  int totalKeseluruhan = 0;
  int point = 0;
  int subTotal = 0;
  int potonganDompet = 0;
  int potonganVoucher = 0;
  int ongkir = 0;
  int minimalPointTukarPoint = 0;
  double totalDapatPoint = 0;
  String catatanTambahan;
  String cartCode;
  String kodePromo;
  bool? kodePromoStatus;
  String? kodePromoMessage;
  int kodePromoDiskon = 0;
  Map<String, dynamic> detailUser;
  Map<String, dynamic>? usedVoucher;
  Map<String, dynamic>? jadwalDelivery;
  dynamic diskonKategori;
  int nilaiPointOut = 0;
  CheckoutGlobalState(
      {required this.cartCode,
      required this.isPointUsed,
      required this.isWalletOn,
      required this.isCheckoutFormCompleted,
      required this.totalKeseluruhan,
      required this.point,
      required this.subTotal,
      required this.potonganDompet,
      required this.potonganVoucher,
      required this.ongkir,
      required this.detailUser,
      required this.catatanTambahan,
      required this.usedVoucher,
      required this.jadwalDelivery,
      required this.totalDapatPoint,
      required this.minimalPointTukarPoint,
      required this.diskonKategori,
      required this.nilaiPointOut,
      required this.kodePromo,
      required this.kodePromoStatus,
      required this.kodePromoDiskon,
      this.kodePromoMessage
      });
}
