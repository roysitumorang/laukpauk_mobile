class Keranjang{
  final String? id;
  final String? cartCode;
  final String? dayDelivery;
  final String? jadwalDeliveryCode;
  final String? idUser;
  final String? idPromo;
  final String? diskonPromo;
  final String? idPasarPenjual;
  final String? idPenjual;
  final String? idProduk;
  final String? jenisProduk;
  final String? idSatuan;
  final String? harga;
  final String? diskon;
  final String? quantity;
  final String? catatan;

  Keranjang({
    this.id, this.cartCode, this.dayDelivery, this.jadwalDeliveryCode, this.idUser, 
   this.idPromo, this.diskonPromo, this.idPasarPenjual, this.idPenjual, 
    this.idProduk,  this.jenisProduk, this.idSatuan, this.harga, 
    this.diskon, this.quantity, this.catatan});

  Keranjang.fromJson(Map<String, dynamic> json):
    id  =  json['id'],
    cartCode = json['cartCode'],
    dayDelivery = json['dayDelivery'],
    jadwalDeliveryCode = json['jadwalDeliveryCode'],
    idUser = json['idUser'],
    idPromo = json['idPromo'],
    diskonPromo = json['diskonPromo'],
    idPasarPenjual = json['idPasarPenjual'],
    idPenjual = json['idPenjual'],
    idProduk = json['idProduk'],
    jenisProduk = json['jenisProduk'],
    idSatuan = json['idSatuan'],
    harga = json['harga'],
    diskon = json['diskon'],
    quantity = json['quantity'],
    catatan = json['catatan']
  ;

  Map<String, dynamic> toJson() => {
    'id'  :  id,
    'cartCode' : cartCode,
    'dayDelivery' : dayDelivery,
    'jadwalDeliveryCode' : jadwalDeliveryCode,
    'idUser' : idUser,
    'idPromo' : idPromo,
    'diskonPromo' : diskonPromo,
    'idPasarPenjual' : idPasarPenjual,
    'idPenjual' : idPenjual,
    'idProduk' : idProduk,
    'jenisProduk' : jenisProduk,
    'idSatuan' : idSatuan,
    'harga' : harga,
    'diskon' : diskon,
    'quantity' : quantity,
    'catatan' : catatan
  };
}