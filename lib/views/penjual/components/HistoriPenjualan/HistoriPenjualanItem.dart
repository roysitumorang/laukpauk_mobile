import 'package:flutter/material.dart';
import 'package:laukpauk/views/pembeli/components/DetailTransaksi/ItemTransaksi.dart';

import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/models/HistoriPenjualanModel.dart';

class HistoriPenjualanItem extends StatelessWidget {
  final Map<String, dynamic> dataPenjualan;
  HistoriPenjualanItem(this.dataPenjualan);

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String namaUser = dataPenjualan['namaUser'];
    String tanggalCheckout = dataPenjualan['waktuCheckout'];
    String nomorTransaksi = dataPenjualan['nomorTransaksi'];
    String status = dataPenjualan['status'];
    String idTransaksi = dataPenjualan['id'];

    TextStyle teks = TextStyle(
        fontSize: SizeConfig.horizontalBlock * 3, color: CustomColors.grey);

    return InkWell(
      onTap: () async {
        int idPenjual = await LocalStorage.getIdPenjualFromActiveUser();
        Navigator.push(
            context,
            MaterialPageRoute(
                builder: (_) => ItemTransaksi(idTransaksi, 
                    idPenjual: idPenjual.toString(), scaffold: true)));
      },
      child: Container(
          margin: EdgeInsets.only(bottom: 5),
          decoration: BoxDecoration(
              color: CustomColors.nearlyWhite,
              borderRadius: BorderRadius.circular(10),
              border: Border.all(width: 0.75, color: CustomColors.green)),
          child: Padding(
            padding: EdgeInsets.symmetric(horizontal: 15, vertical: 15),
            child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(status.toUpperCase(),
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 3.5,
                                fontWeight: FontWeight.w500)),
                        SizedBox(height: 3.5),
                        Text('Transaksi $namaUser', style: teks),
                        SizedBox(height: 10),
                        Text('Invoice No. $nomorTransaksi', style: teks),
                        SizedBox(height: 2),
                        Text('Pada. $tanggalCheckout', style: teks)
                      ]),
                  FutureBuilder<int>(
                      future:
                          HistoriPenjualanModel.getTotalTransaksiForThisPenjual(
                              idTransaksi),
                      builder: (context, snapshot) {
                        if (snapshot.hasData) {
                          if (snapshot.data != null) {
                            int totalTransaksiForThisPenjual = snapshot.data!;
                            return Text(
                                Currency.formatRupiah(
                                    totalTransaksiForThisPenjual, 0),
                                style: TextStyle(
                                    fontWeight: FontWeight.w700,
                                    color: CustomColors.green,
                                    fontSize:
                                        SizeConfig.horizontalBlock * 3.5));
                          }
                        }
                        return SizedBox(
                            width: 10,
                            height: 10,
                            child: CircularProgressIndicator(strokeWidth: 1));
                      })
                ]),
          )),
    );
  }
}
