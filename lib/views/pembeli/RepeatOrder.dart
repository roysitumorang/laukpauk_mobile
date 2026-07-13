import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/global-components/Header.dart';
import 'package:laukpauk/global-components/Footer.dart';
import 'package:laukpauk/models/HeaderModel.dart';
import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';
import 'package:laukpauk/models/TransaksiModel.dart';
import 'package:laukpauk/views/pembeli/components/RepeatOrder/RepeatOrderItemTransaksi.dart';

class RepeatOrder extends StatefulWidget {
  final String idTransaksi;
  RepeatOrder(this.idTransaksi);
  _RepeatOrderState createState() => _RepeatOrderState();
}

class _RepeatOrderState extends State<RepeatOrder> {
  bool isReadyToShow = false;
  dynamic detailTransaksiState = false;

  void initialization() async {
    String idTransaksi = widget.idTransaksi;

    dynamic detailTransaksi = await TransaksiModel.getDetailTransaksi(
        idTransaksi,
        select: 'nomorTransaksi, tanggalDelivery');

    setState(() {
      isReadyToShow = true;
      detailTransaksiState = detailTransaksi;
    });
  }

  void initState() {
    super.initState();
    initialization();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String idTransaksi = widget.idTransaksi;
    String nomorTransaksi = '#';
    if (detailTransaksiState != false) {
      nomorTransaksi = '#' + detailTransaksiState['nomorTransaksi'];
    }

    return Scaffold(
        backgroundColor: CustomColors.nearlyWhite,
        body: (isReadyToShow)
            ? Container(
                child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                    Header(),
                    SizedBox(height: 5),
                    Padding(
                      padding: EdgeInsets.symmetric(horizontal: 5.0),
                      child: Card(
                        elevation: 5,
                        shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(10)),
                        child: Padding(
                          padding: EdgeInsets.symmetric(
                              horizontal: 12.5, vertical: 15.0),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text('Repeat Order',
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.75,
                                      fontWeight: FontWeight.w600)),
                              Text('Nomor Transaksi $nomorTransaksi',
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.0)),
                              SizedBox(height: 5.0),
                              Text(
                                  'Anda dapat memesan kembali produk - produk yang sudah pernah anda beli sebelumnya. Silahkan pilih produk produk yang ingin anda beli ulang.',
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 2.85)),
                              SizedBox(height: 7.5),
                              Text(
                                  'NOTE : Harga dan diskon yang tertera pada tiap - tiap produk dalam transaksi ini bisa saja berbeda dengan harga dan diskon pada produk yang sekarang. Silahkan cek dahulu sebelum checkout di keranjang setelah anda memasukkan belanjaan ke keranjang.',
                                  style: TextStyle(
                                      color: CustomColors.textInfo,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 2.85))
                            ],
                          ),
                        ),
                      ),
                    ),
                    Consumer<HeaderViewModel>(
                        builder: (context, headerStateManager, _) {
                      bool isADJDSet = headerStateManager.isADJDSet;

                      return (isADJDSet)
                          ? RepeatOrderItemTransaksi(idTransaksi, () {
                              initialization();
                            })
                          : Expanded(
                              child: Center(
                                  child: Padding(
                              padding: EdgeInsets.symmetric(
                                  horizontal:
                                      SizeConfig.horizontalBlock * 12.5),
                              child: Column(
                                  mainAxisAlignment: MainAxisAlignment.center,
                                  children: [
                                    Image.asset(
                                        ClientPath.assetsImg +
                                            '/adjd-belum-ditentukan.png',
                                        width: SizeConfig.horizontalBlock * 30),
                                    SizedBox(height: 10),
                                    Text(
                                        'Tentukan Alamat dan Jadwal Delivery dulu ya mak, supaya bisa belanja ulang :)',
                                        textAlign: TextAlign.center,
                                        style: TextStyle(
                                            color: CustomColors.textMuted,
                                            fontSize:
                                                SizeConfig.horizontalBlock *
                                                    3.5)),
                                    SizedBox(height: 15),
                                    CustomButton.button(context,
                                        'Tentukan Lokasi dan Jadwal Delivery',
                                        () async {
                                      Map<String, dynamic> adjdSet =
                                          await HeaderModel.adjd(context);
                                      String? latitude = adjdSet['latitude'];
                                      String? longitude = adjdSet['longitude'];
                                      String? jadwalDeliveryCode =
                                          adjdSet['kodeJadwalDelivery'];

                                      bool isADJDCompleted = false;
                                      if (latitude != null &&
                                          longitude != null &&
                                          jadwalDeliveryCode != null) {
                                        isADJDCompleted = true;
                                      }
                                      Provider.of<HeaderViewModel>(context,
                                              listen: false)
                                          .isADJDSet = isADJDCompleted;
                                    },
                                        color: CustomButton.themeColor,
                                        isPill: true)
                                  ]),
                            )));
                    })
                  ]))
            : Center(
                child: SizedBox(
                    width: 25.0,
                    height: 25.0,
                    child: CircularProgressIndicator(strokeWidth: 1.5))),
        bottomNavigationBar: Footer());
  }
}
