import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class NamaPromo extends StatelessWidget {
  final Map<String, dynamic> detailPromo, detailSatuan, detailPromoItem;
  NamaPromo(
      {required this.detailPromo,
      required this.detailSatuan,
      required this.detailPromoItem});

  Widget build(BuildContext context) {
    String namaPromo = detailPromo['nama'];
    String statusPromo = detailPromo['status'];
    String statusPromoLC = statusPromo.toLowerCase();
    String _deskripsi = detailPromo['deskripsi'];

    String namaSatuan = detailSatuan['nama'];
    String hargaSatuan = detailSatuan['harga'];
    String diskonSatuan = detailSatuan['diskon'];

    int _diskonPromo = int.parse(detailPromoItem['diskon']);

    int hargaInt = int.parse(hargaSatuan);
    int diskonInt = int.parse(diskonSatuan);

    int hargaBersih = hargaInt - (diskonInt + _diskonPromo);

    bool isPromoAktif = statusPromoLC == 'aktif';

    return Container(
        padding: EdgeInsets.symmetric(horizontal: 15.0, vertical: 10.0),
        width: SizeConfig.horizontalBlock * 100,
        decoration: BoxDecoration(
            color: (isPromoAktif) ? Color(0x66FFc107) : Color(0x66dc3545),
            borderRadius: BorderRadius.circular(10)),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Text(namaPromo,
                      style: TextStyle(
                          fontSize: SizeConfig.horizontalBlock * 3.25,
                          fontWeight: FontWeight.w600)),
                ),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      Text(Currency.formatRupiah(hargaBersih, 0),
                          style: TextStyle(
                              fontSize: SizeConfig.horizontalBlock * 3.75,
                              fontWeight: FontWeight.w600)),
                      SizedBox(height: 3.5),
                      Text('Khusus ' + namaSatuan,
                          style: TextStyle(
                              fontSize: SizeConfig.horizontalBlock * 2.75)),
                    ],
                  ),
                )
              ],
            ),
            Container(
                height: 0.45,
                color: Colors.black,
                margin: EdgeInsets.symmetric(vertical: 10.0)),
            Text('Keterangan Promo',
                style: TextStyle(fontWeight: FontWeight.bold)),
            SizedBox(height: 5.0),
            Text(_deskripsi,
                style: TextStyle(fontSize: SizeConfig.horizontalBlock * 2.75)),
            (!isPromoAktif)
                ? Padding(
                    padding: EdgeInsets.only(top: 15.0),
                    child: Text('Promo tidak tersedia untuk saat ini!',
                        style: TextStyle(
                            fontWeight: FontWeight.w500,
                            fontSize: SizeConfig.horizontalBlock * 3.5)),
                  )
                : SizedBox()
          ],
        ));
  }
}
