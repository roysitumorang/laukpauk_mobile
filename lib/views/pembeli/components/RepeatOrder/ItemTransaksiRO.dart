import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class ItemTransaksiRO extends StatelessWidget {
  final List itemTransaksi;
  ItemTransaksiRO(this.itemTransaksi);

  TextStyle titleSize(BuildContext context, {Color? color}) {
    SizeConfig().initSize(context);

    return TextStyle(
        fontSize: SizeConfig.horizontalBlock * 3.75,
        fontWeight: FontWeight.w700,
        color: (color == null) ? CustomColors.nearlyBlack : color);
  }

  TextStyle contentSize(BuildContext context, {Color? color}) {
    SizeConfig().initSize(context);

    return TextStyle(
        fontSize: SizeConfig.horizontalBlock * 3.25,
        color: (color == null) ? CustomColors.nearlyBlack : color);
  }

  TextStyle catatanSize(BuildContext context, {Color? color}) {
    SizeConfig().initSize(context);

    return TextStyle(
        fontSize: SizeConfig.horizontalBlock * 2.5,
        color: (color == null) ? CustomColors.nearlyBlack : color);
  }

  Widget build(BuildContext context) {
    List<Widget> itemTransaksiWidget = itemTransaksi.map((item) {
      int harga = int.parse(item['harga']);
      int diskon = int.parse(item['diskon']);
      int quantity = int.parse(item['quantity']);
      int totalHarga = quantity * (harga - diskon);

      String jenisProduk = item['jenisProduk'];
      String namaSatuan;
      if (jenisProduk.toLowerCase() == 'nonpaket') {
        namaSatuan = item['namaSatuan'];
      } else {
        namaSatuan = 'paket';
      }

      // String status = item['status'];
      // String statusReadable = 'Belum diperika';
      // Color statusReadbleColor = CustomColors.textMuted;
      // if (status.toLowerCase() == 'ok') {
      //   statusReadable = 'Produk Ada';
      //   statusReadbleColor = CustomColors.textSuccess;
      // }

      // if (status.toLowerCase() == 'rejected') {
      //   statusReadable = 'Produk Tidak Tersedia';
      //   statusReadbleColor = CustomColors.textDanger;
      // }

      return Padding(
        padding: EdgeInsets.only(bottom: 10.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: Text(
                        item['namaProduk'] +
                            ' ' +
                            quantity.toString() +
                            ' x ' +
                            namaSatuan,
                        style: contentSize(context,
                            color: CustomColors.nearlyBlack)),
                  ),
                  SizedBox(width: 5),
                  Text(Currency.formatRupiah(totalHarga, 0),
                      textAlign: TextAlign.right,
                      style: titleSize(context, color: CustomColors.green))
                ]),
            // Text(statusReadable,
            //     style: catatanSize(context, color: statusReadbleColor)),
            SizedBox(height: 3.5),
            (item['catatan'].length >= 1) ? SizedBox(height: 3) : SizedBox(),
            (item['catatan'].length >= 1)
                ? Container(
                    width: SizeConfig.horizontalBlock * 100,
                    padding: EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                    decoration: BoxDecoration(
                        color: Color(0x5566CC99),
                        borderRadius: BorderRadius.circular(10)),
                    child: Text('Catatan : ' + item['catatan'],
                        style: catatanSize(context)),
                  )
                : SizedBox()
          ],
        ),
      );
    }).toList();

    return Padding(
        padding: EdgeInsets.only(bottom: 5.0),
        child: (itemTransaksi.length >= 1)
            ? Padding(
                padding: EdgeInsets.only(bottom: 7),
                child: Column(children: itemTransaksiWidget),
              )
            : Container(
                width: double.infinity,
                child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Image.asset(ClientPath.assetsImg + '/empty.png',
                          width: SizeConfig.horizontalBlock * 20),
                      SizedBox(height: 5),
                      Text('Transaksi ini tidak memiliki item belanjaan',
                          style: TextStyle(
                              color: CustomColors.textMuted,
                              fontSize: SizeConfig.horizontalBlock * 3)),
                      SizedBox(height: 10)
                    ]),
              ));
  }
}
