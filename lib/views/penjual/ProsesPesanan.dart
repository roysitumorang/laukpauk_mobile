import 'package:flutter/material.dart';

import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/global-components/Footer.dart';
import 'package:laukpauk/global-components/Header.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/views/penjual/components/ProsesPesanan/ProsesPesananItem.dart';

import 'package:laukpauk/models/TransaksiModel.dart';
import 'package:laukpauk/models/ProsesPesananModel.dart';

class ProsesPesanan extends StatefulWidget {
  final String idTransaksi;
  ProsesPesanan(this.idTransaksi);
  _ProsesPesananState createState() => _ProsesPesananState();
}

class _ProsesPesananState extends State<ProsesPesanan> {
  int reload = 0;

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String idTransaksi = widget.idTransaksi;

    return Scaffold(
        body: FutureBuilder(
            future: TransaksiModel.getDetailTransaksi(idTransaksi),
            builder: (context, snapshot) {
              if (snapshot.hasData) {
                dynamic detailTransaksi = snapshot.data;
                if (detailTransaksi != false) {
                  String status = detailTransaksi['status'];
                  String statusLC = status.toLowerCase();

                  return Container(
                      child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                        Header(),
                        SizedBox(height: 5),
                        (statusLC == 'processing')
                            ? Padding(
                                padding: EdgeInsets.symmetric(horizontal: 5.0),
                                child: Card(
                                  elevation: 5,
                                  shape: RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(10)),
                                  child: Padding(
                                    padding: EdgeInsets.symmetric(
                                        horizontal: 12.5, vertical: 10),
                                    child: Column(
                                      crossAxisAlignment:
                                          CrossAxisAlignment.start,
                                      children: [
                                        Row(
                                          mainAxisAlignment:
                                              MainAxisAlignment.spaceBetween,
                                          children: [
                                            Expanded(
                                              child: Column(
                                                crossAxisAlignment:
                                                    CrossAxisAlignment.start,
                                                children: [
                                                  Text('Pesanan Masuk',
                                                      style: TextStyle(
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              4,
                                                          fontWeight:
                                                              FontWeight.w600)),
                                                  SizedBox(height: 2.5),
                                                  Text(
                                                      'Berikut ini adalah pesanan yang masuk ke anda',
                                                      style: TextStyle(
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              3.25)),
                                                ],
                                              ),
                                            ),
                                            (statusLC == 'processing')
                                                ? CustomButton.button(
                                                    context, 'Proses',
                                                    () async {
                                                    bool statusProsesPesanan =
                                                        await ProsesPesananModel
                                                            .prosesPesanan(
                                                                idTransaksi);
                                                    if (statusProsesPesanan) {
                                                      Navigator.pop(context);
                                                    } else {
                                                      SnackBar
                                                          failedProsesPesananSnackbar =
                                                          SnackBar(
                                                              duration:
                                                                  Duration(
                                                                      seconds:
                                                                          3),
                                                              content: Text(
                                                                  'Mohon maaf, pesanan tersebut tidak dapat diproses. Silahkan coba lagi!'));

                                                      ScaffoldMessenger.of(
                                                              context)
                                                          .showSnackBar(
                                                              failedProsesPesananSnackbar);
                                                    }
                                                  },
                                                    color:
                                                        CustomButton.themeColor,
                                                    isPill: true)
                                                : SizedBox()
                                          ],
                                        ),
                                        SizedBox(height: 5.0),
                                        Text(
                                            'Secara default produk dianggap ada. Jika produk tidak tersedia, silahkan tekan tombol merah disetiap item pesanan',
                                            style: TextStyle(
                                                fontSize:
                                                    SizeConfig.horizontalBlock *
                                                        2.75,
                                                color: CustomColors.textInfo))
                                      ],
                                    ),
                                  ),
                                ),
                              )
                            : Expanded(
                                child: Center(
                                    child: Column(
                                        crossAxisAlignment:
                                            CrossAxisAlignment.center,
                                        mainAxisAlignment:
                                            MainAxisAlignment.center,
                                        children: [
                                    Image.asset(
                                        ClientPath.assetsImg +
                                            '/pesanan-diterima.png',
                                        width:
                                            SizeConfig.horizontalBlock * 30.0),
                                    SizedBox(height: 5.0),
                                    Text(
                                        (statusLC == 'otw')
                                            ? 'Sedang Dijemput'
                                            : 'Transaksi Selesai !',
                                        style: TextStyle(
                                            fontSize:
                                                SizeConfig.horizontalBlock *
                                                    3.5,
                                            fontWeight: FontWeight.w500,
                                            color: CustomColors.nearlyBlack)),
                                    SizedBox(height: 2.5),
                                    Text(
                                        (statusLC == 'otw')
                                            ? 'Pesanan akan dijemput oleh delivery Laukpauk...'
                                            : 'Yeay, pesanan sudah diterima oleh pembeli mak ..',
                                        style: TextStyle(
                                            fontSize:
                                                SizeConfig.horizontalBlock *
                                                    2.75,
                                            color: CustomColors.textMuted))
                                  ]))),
                        (statusLC == 'processing')
                            ? FutureBuilder<List>(
                                future: ProsesPesananModel.getPesananMasukItem(
                                    idTransaksi),
                                builder: (context, snapshot) {
                                  if (snapshot.hasData) {
                                    if (snapshot.data != null) {
                                      List pesanan = snapshot.data!;
                                      if (pesanan.length >= 1) {
                                        List<Widget> pesananItemWidget =
                                            pesanan.map((pesananItem) {
                                          return ProsesPesananItem(pesananItem,
                                              (returnedData) {
                                            if (returnedData != null) {
                                              if (returnedData) {
                                                setState(() {
                                                  reload++;
                                                });
                                              } else {
                                                SnackBar callbackSnackbar =
                                                    SnackBar(
                                                        duration: Duration(
                                                            seconds: 3),
                                                        content: Text(
                                                            'Mohon maaf, proses pengklaiman produk tidak tersedia gagal dilakukan. Silahkan coba lagi!'));

                                                ScaffoldMessenger.of(context)
                                                    .showSnackBar(
                                                        callbackSnackbar);
                                              }
                                            }
                                          });
                                        }).toList();

                                        return Expanded(
                                          child: Padding(
                                            padding: EdgeInsets.symmetric(
                                                horizontal: 10.0,
                                                vertical: 5.0),
                                            child: RefreshIndicator(
                                                onRefresh: () async {
                                                  setState(() {});
                                                },
                                                child: ListView(
                                                    padding: EdgeInsets.zero,
                                                    children:
                                                        pesananItemWidget)),
                                          ),
                                        );
                                      }
                                    }
                                    return Expanded(
                                        child: Center(
                                      child: Padding(
                                        padding: EdgeInsets.symmetric(
                                            horizontal:
                                                SizeConfig.horizontalBlock *
                                                    15.0),
                                        child: Column(
                                            mainAxisAlignment:
                                                MainAxisAlignment.center,
                                            children: [
                                              Image.asset(
                                                  ClientPath.assetsImg +
                                                      '/empty.png',
                                                  width: SizeConfig
                                                          .horizontalBlock *
                                                      30.0),
                                              SizedBox(height: 5),
                                              Text(
                                                  'Tidak ada barang yang dipesan di dalam transaksi ini',
                                                  textAlign: TextAlign.center),
                                              SizedBox(height: 25),
                                              CustomButton.button(
                                                  context, 'Kembali', () {
                                                Navigator.pop(context);
                                              },
                                                  color: CustomButton
                                                      .secondaryColor,
                                                  isPill: true)
                                            ]),
                                      ),
                                    ));
                                  } else {
                                    return Expanded(
                                        child: Center(
                                            child: SizedBox(
                                                width: 25,
                                                height: 25,
                                                child:
                                                    CircularProgressIndicator(
                                                        strokeWidth: 1.5))));
                                  }
                                })
                            : SizedBox()
                      ]));
                } else {
                  return Center(
                    child: Column(
                        crossAxisAlignment: CrossAxisAlignment.center,
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Image.asset(ClientPath.assetsImg + '/empty.png',
                              width: SizeConfig.horizontalBlock * 35.0),
                          SizedBox(height: 5.0),
                          Text('Transaksi Tidak Ditemukan !',
                              style: TextStyle(
                                  fontSize: SizeConfig.horizontalBlock * 3.5,
                                  fontWeight: FontWeight.w500,
                                  color: CustomColors.nearlyBlack)),
                          SizedBox(height: 2.5),
                          Text(
                              'Transaksi ini tidak ditemukan dalam sistem Laukpauk',
                              style: TextStyle(
                                  fontSize: SizeConfig.horizontalBlock * 2.75,
                                  color: CustomColors.textMuted))
                        ]),
                  );
                }
              } else {
                return Center(
                  child: SizedBox(
                      height: 25.0,
                      width: 25.0,
                      child: CircularProgressIndicator(strokeWidth: 1.5)),
                );
              }
            }),
        bottomNavigationBar: Footer());
  }
}
