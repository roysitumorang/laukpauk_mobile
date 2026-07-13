import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/views/pembeli/components/DetailTransaksi/GiftTransaksiPembeli.dart';
import 'package:url_launcher/url_launcher.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class ItemTransaksi extends StatefulWidget {
  final String idTransaksi;
  final String? idPenjual, cartCode;
  final bool scaffold;
  ItemTransaksi(this.idTransaksi, {this.idPenjual, this.scaffold = false, this.cartCode});

  _ItemTransaksiState createState() => _ItemTransaksiState();
}

class _ItemTransaksiState extends State<ItemTransaksi> {
  Future<List> getItemTransaksi() async {
    String idTransaksi = widget.idTransaksi;
    String entitasItemTransaksi =
        'id,  namaProduk, namaSatuan, quantity, harga, diskon, catatan, jenisProduk, status, diskonPromo';
    String idPenjual =
        (widget.idPenjual == null) ? '' : '&idPenjual=' + widget.idPenjual!;

    String _params = await HTTPRequest.getQueryString(
        authToken: true,
        params: 'entitasItemTransaksi=$entitasItemTransaksi$idPenjual');

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Transaksi/getItemTransaksiFromTransaksi/$idTransaksi?' +
        _params);
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map dataObject = json.decode(httpResponse.body);
      return dataObject['itemTransaksi'];
    }

    return [];
  }

  TextStyle titleSize(BuildContext context,
      {Color? color, TextDecoration? textDecoration}) {
    SizeConfig().initSize(context);

    return TextStyle(
        decoration: textDecoration,
        fontSize: SizeConfig.horizontalBlock * 3.75,
        fontWeight: FontWeight.w700,
        color: (color == null) ? CustomColors.nearlyBlack : color);
  }

  TextStyle contentSize(BuildContext context,
      {Color? color, TextDecoration? textDecoration}) {
    SizeConfig().initSize(context);

    return TextStyle(
        decoration: textDecoration,
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
    Widget futureBuilder = FutureBuilder<List>(
        future: getItemTransaksi(),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            if (snapshot.data != null) {
              List itemTransaksi = snapshot.data!;
              List<Widget> itemTransaksiWidget = itemTransaksi.map((item) {
                int harga = int.parse(item['harga']);
                int diskon = int.parse(item['diskon']);
                int _diskonPromo = int.parse(item['diskonPromo']);

                diskon = diskon + _diskonPromo;

                int quantity = int.parse(item['quantity']);
                int totalHarga = quantity * (harga - diskon);

                String jenisProduk = item['jenisProduk'];
                String namaSatuan;
                if (jenisProduk.toLowerCase() == 'nonpaket') {
                  namaSatuan = item['namaSatuan'];
                } else {
                  namaSatuan = 'paket';
                }

                String status = item['status'];
                String statusReadable = 'Sedang di proses';
                Color statusReadbleColor = CustomColors.textMuted;
                if (status.toLowerCase() == 'ok') {
                  statusReadable = 'Produk Ada';
                  statusReadbleColor = CustomColors.textSuccess;
                }

                bool isRejected = (status.toLowerCase() == 'rejected');

                if (isRejected) {
                  statusReadable = 'Produk Tidak Tersedia';
                  statusReadbleColor = CustomColors.textDanger;
                }

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
                                  item['namaProduk'] ?? 'Produk Tidak Dikenal' +
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
                                style: contentSize(context,
                                    color: (isRejected)
                                        ? CustomColors.textMuted
                                        : CustomColors.nearlyBlack,
                                    textDecoration: (isRejected)
                                        ? TextDecoration.lineThrough
                                        : null))
                          ]),
                      Text(statusReadable,
                          style:
                              catatanSize(context, color: statusReadbleColor)),
                      SizedBox(height: 3.5),
                      (item['catatan'].length >= 1)
                          ? SizedBox(height: 3)
                          : SizedBox(),
                      (item['catatan'].length >= 1)
                          ? Container(
                              width: SizeConfig.horizontalBlock * 100,
                              padding: EdgeInsets.symmetric(
                                  horizontal: 10, vertical: 5),
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

              String? idPenjual = widget.idPenjual;
              String idTransaksi = widget.idTransaksi;

              return Card(
                  elevation: 5,
                  shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(10)),
                  child: Padding(
                      padding:
                          EdgeInsets.symmetric(horizontal: 15, vertical: 10),
                      child: (itemTransaksi.length >= 1)
                          ? Column(
                              mainAxisSize: (idPenjual != null)
                                  ? MainAxisSize.min
                                  : MainAxisSize.max,
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                  Row(
                                    mainAxisAlignment:
                                        MainAxisAlignment.spaceBetween,
                                    children: [
                                      Text('Daftar Belanjaan',
                                          style: titleSize(context)),
                                      (idPenjual != null)
                                          ? InkWell(
                                              onTap: () async {
                                                Uri url = Uri.parse(ServerPath
                                                        .apiBaseURL +
                                                    '/Transaksi/invoice/$idTransaksi?idPenjual=$idPenjual');
                                                if (await canLaunchUrl(url)) {
                                                  await launchUrl(url);
                                                } else {
                                                  SnackBar snackBar = SnackBar(
                                                      content: Text(
                                                          'URL Tidak Valid !'),
                                                      duration:
                                                          Duration(seconds: 1));

                                                  ScaffoldMessenger.of(context)
                                                      .showSnackBar(snackBar);
                                                }
                                              },
                                              child: Text('Invoice',
                                                  style: contentSize(context,
                                                      color: CustomColors
                                                          .textPrimary)),
                                            )
                                          : SizedBox(),
                                    ],
                                  ),
                                  SizedBox(height: 15),
                                  Column(children: <Widget>[...itemTransaksiWidget, GiftTransaksiPembeli(cartCode: widget.cartCode)])
                                ])
                          : Container(
                              width: double.infinity,
                              margin: EdgeInsets.symmetric(
                                  vertical: SizeConfig.horizontalBlock * 5.0),
                              padding: EdgeInsets.symmetric(
                                  horizontal: SizeConfig.horizontalBlock * 25),
                              child: Column(
                                  mainAxisAlignment: MainAxisAlignment.center,
                                  crossAxisAlignment: CrossAxisAlignment.center,
                                  children: [
                                    Image.asset(
                                        ClientPath.assetsImg + '/empty.png',
                                        width: SizeConfig.horizontalBlock * 20),
                                    SizedBox(height: 5),
                                    Text(
                                        'Transaksi ini tidak memiliki item belanjaan',
                                        textAlign: TextAlign.center,
                                        style: TextStyle(
                                            color: CustomColors.textMuted,
                                            fontSize:
                                                SizeConfig.horizontalBlock *
                                                    3)),
                                    SizedBox(height: 10)
                                  ]),
                            )));
            }
          }
          return Loading();
        });
    return (widget.scaffold)
        ? Scaffold(
            appBar: AppBar(
                title: Text('Daftar Belanjaan'),
                backgroundColor: CustomColors.green),
            body: Padding(
                padding: EdgeInsets.symmetric(horizontal: 5, vertical: 5),
                child: futureBuilder),
          )
        : futureBuilder;
  }
}
