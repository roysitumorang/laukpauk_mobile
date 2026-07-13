import 'package:flutter/material.dart';
import 'package:laukpauk/views/pembeli/components/RepeatOrder/ItemTransaksiRO.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class RepeatOrderTransaksiItemPerPenjual extends StatelessWidget {
  final bool isAvailable, isSelected;
  final String idTransaksi;
  final Map<String, dynamic> detailPenjual;
  final List itemTransaksi;
  final void Function(bool? state) toggleSelected;
  RepeatOrderTransaksiItemPerPenjual(
      this.isSelected,
      this.isAvailable,
      this.idTransaksi,
      this.detailPenjual,
      this.itemTransaksi,
      this.toggleSelected);

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String namaPenjual = detailPenjual['namaPenjual'];
    String alamatToko = detailPenjual['alamatToko'];
    if (alamatToko.length <= 0) {
      alamatToko = 'Alamat toko belum ditentukan';
    }

    String namaPasar = detailPenjual['namaPasar'];
    String bannerPenjual = detailPenjual['bannerPenjual'];

    String bannerPenjualURL = ServerPath.baseURL +
        '/' +
        ServerPath.uploadGambarBannerPenjual +
        '/compress/$bannerPenjual';

    double imgWidthHeight = SizeConfig.horizontalBlock * 10.5;

    return Card(
        elevation: 5.0,
        shape: RoundedRectangleBorder(
            side: BorderSide(
                color: (!isAvailable)
                    ? CustomColors.textDanger
                    : CustomColors.nearlyWhite),
            borderRadius: BorderRadius.circular(10.0)),
        child: Padding(
          padding: EdgeInsets.symmetric(horizontal: 15.0, vertical: 15.0),
          child:
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Row(children: [
              (isAvailable)
                  ? Checkbox(
                      value: isSelected,
                      onChanged: toggleSelected,
                      checkColor: CustomColors.nearlyWhite,
                      activeColor: CustomColors.green)
                  : SizedBox(),
              SizedBox(width: (isAvailable) ? 5.0 : 0.0),
              ClipRRect(
                  borderRadius: BorderRadius.circular(50.0),
                  child: Image.network(bannerPenjualURL,
                      fit: BoxFit.cover,
                      width: imgWidthHeight,
                      height: imgWidthHeight)),
              SizedBox(width: 10.0),
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text(namaPenjual,
                    style: TextStyle(
                        fontSize: SizeConfig.horizontalBlock * 3.0,
                        fontWeight: FontWeight.w500)),
                SizedBox(height: 1.5),
                Text(alamatToko,
                    style: TextStyle(
                        fontSize: SizeConfig.horizontalBlock * 2.50,
                        color: CustomColors.textMuted)),
                SizedBox(height: 5.0),
                Text('Berjualan di $namaPasar',
                    style: TextStyle(
                        fontSize: SizeConfig.horizontalBlock * 2.50,
                        color: CustomColors.textMuted))
              ])
            ]),
            (isAvailable)
                ? Container(
                    margin: EdgeInsets.symmetric(vertical: 10.0),
                    padding: EdgeInsets.symmetric(horizontal: 7.5),
                    width: SizeConfig.horizontalBlock * 100,
                    child: Text('Item Belanjaan',
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 3.25,
                            fontWeight: FontWeight.w500)))
                : SizedBox(height: 15.0),
            (isAvailable)
                ? Padding(
                    padding: EdgeInsets.symmetric(horizontal: 7.5),
                    child: ItemTransaksiRO(itemTransaksi),
                  )
                : Column(children: [
                    Text('Tidak Dapat Dibeli Ulang',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                            color: CustomColors.textDanger,
                            fontWeight: FontWeight.w500,
                            fontSize: SizeConfig.horizontalBlock * 3.25)),
                    SizedBox(height: 5.0),
                    Text(
                        'Mohon maaf, belanjaan tidak dapat direpeat order karena penjual tidak ready pada alamat dan jam pengantaran yang telah anda tentukan',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                            color: CustomColors.textMuted,
                            fontSize: SizeConfig.horizontalBlock * 2.75))
                  ])
          ]),
        ));
  }
}
