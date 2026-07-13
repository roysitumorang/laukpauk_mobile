class GiftTransaksi{
  final String? id;
  final String? idGift;
  final String? cartCode;
  final String? createdAt;

  GiftTransaksi({
    this.id, this.idGift, this.cartCode, this.createdAt});

  GiftTransaksi.fromJson(Map<String, dynamic> json):
    id  =  json['id'],
    idGift  = json['idGift'],
    cartCode = json['cartCode'],
    createdAt = json['createdAt'];

  Map<String, dynamic> toJson() => {
    'id'  :  id,
    'idGift': idGift,
    'cartCode' : cartCode,
    'createdAt' : createdAt
  };
}