import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:intl/intl.dart';
import 'package:laukpauk/global-components/CustomAlert.dart';
import 'package:laukpauk/views/pembeli/DetailPaket.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/EditKeranjangItem.dart';
import 'package:laukpauk/views/pembeli/DetailProduk.dart';

import 'package:laukpauk/models/HeaderModel.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';

class ProductPenjual extends StatelessWidget {
  final Map<String, dynamic> dataProduk;
  final Function onRemoveItemSuccessed;
  final Function onEditItemSuccessed;
  ProductPenjual(this.dataProduk,
      {required this.onRemoveItemSuccessed, required this.onEditItemSuccessed});

  void deleteProductPenjual(
      BuildContext context, String idCart, String namaProduk) {
    String _title = 'Konfirmasi Hapus Keranjang!';
    String _subTitle =
        'Apakah anda yakin akan menghapus produk $namaProduk dari keranjang ?';
    Widget _actions =
        Row(mainAxisAlignment: MainAxisAlignment.center, children: [
      TextButton(
          onPressed: () {
            Navigator.pop(context);
          },
          child: Text('Tidak')),
      TextButton(
          onPressed: () async {
            await hapusCart(context, idCart);
            Navigator.pop(context);
          },
          child: Text('Ya'))
    ]);
    CustomAlert.textComponent(context,
        title: _title, subTitle: _subTitle, actions: _actions);
  }

  Future<void> hapusCart(BuildContext context, String idCart) async {
    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Keranjang/deleteCart/$idCart');
    Response httpResponse = await post(_url);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      bool statusDeleteCart = dataObject['statusDeleteCart'];
      if (statusDeleteCart) {
        int jumlahKeranjang = await HeaderModel.getJumlahCart();
        Provider.of<HeaderViewModel>(context, listen: false).jumlahCart =
            jumlahKeranjang;

        onRemoveItemSuccessed(idCart);
      }
    }
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    int harga = int.parse(dataProduk['harga']);
    int diskon = int.parse(dataProduk['diskon']);
    int quantity = int.parse(dataProduk['quantity']);
    int totalHarga = (harga - diskon) * quantity;

    String jenisProduk = dataProduk['jenisProduk'];
    String jenisProdukLC = jenisProduk.toLowerCase();
    String namaSatuan =
        (jenisProdukLC == 'paket') ? 'Paket' : dataProduk['namaSatuan'];
    String fotoProduk = dataProduk['fotoProduk'];

    String imageURL;
    if (jenisProdukLC == 'paket') {
      imageURL = ServerPath.baseURL +
          '/' +
          ServerPath.uploadGambarPaket +
          '/compress/' +
          fotoProduk;
    } else {
      imageURL = ServerPath.baseURL +
          '/' +
          ServerPath.uploadGambarProduk +
          '/compress/' +
          fotoProduk;
    }

    String catatanTambahan = dataProduk['catatan'];

    return Container(
        padding: EdgeInsets.only(bottom: 20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
              InkWell(
                onTap: () {
                  if (jenisProdukLC == 'nonpaket') {
                    Navigator.push(
                        context,
                        MaterialPageRoute(
                            builder: (_) =>
                                DetailProduk(dataProduk['idProduk'])));
                  }
                  if (jenisProdukLC == 'paket') {
                    Navigator.push(
                        context,
                        MaterialPageRoute(
                            builder: (_) =>
                                DetailPaket(dataProduk['idProduk'])));
                  }
                },
                child: ClipRRect(
                    borderRadius: BorderRadius.circular(50),
                    child: Image.network(imageURL,
                        width: SizeConfig.horizontalBlock * 15,
                        height: SizeConfig.horizontalBlock * 15)),
              ),
              SizedBox(width: 15),
              Expanded(
                  child: Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisAlignment: MainAxisAlignment.start,
                      children: [
                    Expanded(
                      child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            InkWell(
                              onTap: () {
                                if (jenisProdukLC == 'nonpaket') {
                                  Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                          builder: (_) => DetailProduk(
                                              dataProduk['idProduk'])));
                                }
                                if (jenisProdukLC == 'paket') {
                                  Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                          builder: (_) => DetailPaket(
                                              dataProduk['idProduk'])));
                                }
                              },
                              child: Text(dataProduk['namaProduk'],
                                  style: TextStyle(
                                      fontSize: SizeConfig.horizontalBlock * 4,
                                      fontWeight: FontWeight.w700,
                                      color: CustomColors.textSuccess)),
                            ),
                            SizedBox(height: 5),
                            Text(
                                NumberFormat.currency(
                                        locale: 'id',
                                        symbol: 'Rp. ',
                                        decimalDigits: 0)
                                    .format(totalHarga),
                                style: TextStyle(
                                    fontSize: SizeConfig.horizontalBlock * 3.5,
                                    fontWeight: FontWeight.w700,
                                    color: CustomColors.green)),
                            SizedBox(height: 5),
                            Row(
                              mainAxisAlignment: MainAxisAlignment.start,
                              children: [
                                Text(
                                    dataProduk['quantity'] + ' x ' + namaSatuan,
                                    style: TextStyle(
                                        fontSize:
                                            SizeConfig.horizontalBlock * 3.25)),
                                SizedBox(width: 5.0),
                                InkWell(
                                    onTap: () async {
                                      bool? statusEditKeranjangItem =
                                          await Navigator.push(
                                              context,
                                              MaterialPageRoute(
                                                  builder: (_) =>
                                                      EditKeranjangItem(
                                                          dataProduk)));
                                      if (statusEditKeranjangItem != null) {
                                        if (statusEditKeranjangItem) {
                                          onEditItemSuccessed();
                                        } else {
                                          String _title = 'Keranjang';
                                          String _subTitle =
                                              'Gagal mengubah item keranjang ! Silahkan coba lagi.';
                                          Widget _actions = Row(
                                              mainAxisAlignment:
                                                  MainAxisAlignment.center,
                                              children: [
                                                TextButton(
                                                    onPressed: () {
                                                      Navigator.pop(context);
                                                    },
                                                    child: Text('Coba Lagi'))
                                              ]);

                                          CustomAlert.textComponent(context,
                                              title: _title,
                                              subTitle: _subTitle,
                                              actions: _actions);
                                        }
                                      }
                                    },
                                    child: Icon(FontAwesomeIcons.pencilAlt,
                                        size: SizeConfig.horizontalBlock * 3.0,
                                        color: CustomColors.textWarning))
                              ],
                            ),
                            SizedBox(height: 2),
                            Text(
                                '@' +
                                    namaSatuan +
                                    ' ' +
                                    NumberFormat.currency(
                                            locale: 'id',
                                            symbol: 'Rp. ',
                                            decimalDigits: 0)
                                        .format(harga - diskon),
                                style: TextStyle(
                                    fontSize: SizeConfig.horizontalBlock * 3)),
                            (catatanTambahan.length >= 1)
                                ? SizedBox()
                                : SizedBox(height: 10.0),
                            (catatanTambahan.length >= 1)
                                ? SizedBox()
                                : InkWell(
                                    onTap: () async {
                                      bool? statusEditKeranjangItem =
                                          await Navigator.push(
                                              context,
                                              MaterialPageRoute(
                                                  builder: (_) =>
                                                      EditKeranjangItem(
                                                          dataProduk)));
                                      if (statusEditKeranjangItem != null) {
                                        if (statusEditKeranjangItem) {
                                          onEditItemSuccessed();
                                        } else {
                                          String _title = 'Keranjang';
                                          String _subTitle =
                                              'Gagal mengubah item keranjang! Coba lagi!';
                                          Widget _actions = Row(
                                              mainAxisAlignment:
                                                  MainAxisAlignment.center,
                                              children: [
                                                TextButton(
                                                    onPressed: () {
                                                      Navigator.pop(context);
                                                    },
                                                    child: Text('OK'))
                                              ]);

                                          CustomAlert.textComponent(context,
                                              title: _title,
                                              subTitle: _subTitle,
                                              actions: _actions);
                                        }
                                      }
                                    },
                                    child: Text('Tambah Catatan',
                                        style: TextStyle(
                                            color: CustomColors.textPrimary,
                                            fontSize:
                                                SizeConfig.horizontalBlock *
                                                    3.25)),
                                  )
                          ]),
                    ),
                  ])),
              SizedBox(width: 13),
              InkWell(
                child: Icon(FontAwesomeIcons.trash,
                    color: Colors.red, size: SizeConfig.horizontalBlock * 4),
                onTap: () {
                  deleteProductPenjual(
                      context, dataProduk['idCart'], dataProduk['namaProduk']);
                },
              ),
            ]),
            (catatanTambahan.length >= 1) ? SizedBox(height: 10) : SizedBox(),
            (catatanTambahan.length >= 1)
                ? Container(
                    width: double.infinity,
                    padding: EdgeInsets.symmetric(horizontal: 15, vertical: 10),
                    decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(15),
                        color: Color(0x4466CC99)),
                    child: Column(
                        mainAxisAlignment: MainAxisAlignment.start,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text('Catatan Tambahan : ',
                                  overflow: TextOverflow.clip,
                                  style: TextStyle(
                                      fontWeight: FontWeight.w600,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5)),
                              InkWell(
                                onTap: () async {
                                  bool? statusEditKeranjangItem =
                                      await Navigator.push(
                                          context,
                                          MaterialPageRoute(
                                              builder: (_) => EditKeranjangItem(
                                                  dataProduk)));
                                  if (statusEditKeranjangItem != null) {
                                    if (statusEditKeranjangItem) {
                                      onEditItemSuccessed();
                                    } else {
                                      String _title = 'Keranjang';
                                      String _subTitle =
                                          'Gagal mengubah item keranjang! SIlahkan coba lagi!';
                                      Widget _actions = Row(
                                          mainAxisAlignment:
                                              MainAxisAlignment.center,
                                          children: [
                                            TextButton(
                                                onPressed: () {
                                                  Navigator.pop(context);
                                                },
                                                child: Text('OK'))
                                          ]);

                                      CustomAlert.textComponent(context,
                                          title: _title,
                                          subTitle: _subTitle,
                                          actions: _actions);
                                    }
                                  }
                                },
                                child: Icon(FontAwesomeIcons.pen,
                                    color: CustomColors.textWarning,
                                    size: SizeConfig.horizontalBlock * 3.5),
                              ),
                            ],
                          ),
                          SizedBox(height: 5.0),
                          Text(catatanTambahan,
                              style: TextStyle(
                                  fontSize: SizeConfig.horizontalBlock * 3.25))
                        ]),
                  )
                : SizedBox()
          ],
        ));
  }
}
