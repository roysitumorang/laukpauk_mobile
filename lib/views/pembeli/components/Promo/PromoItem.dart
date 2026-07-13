import 'package:flutter/material.dart';
import 'package:laukpauk/global-components/CustomAlert.dart';
import 'package:laukpauk/libraries/CustomText.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/DefaultImage.dart';

import 'package:laukpauk/models/ProdukModel.dart';
import 'package:laukpauk/views/pembeli/DetailPromoItem.dart';

import 'package:laukpauk/views/pembeli/Keranjang.dart';

import 'package:laukpauk/models/HeaderModel.dart';
import 'package:laukpauk/view-models/HeaderViewModel.dart';
import 'package:laukpauk/global-components/CustomButton.dart';

class PromoItem extends StatelessWidget {
  final Map<String, dynamic>? dataProduk;
  final double? widgetWidth;
  PromoItem(this.dataProduk, {this.widgetWidth});

  Widget build(BuildContext context) {
    if (dataProduk != null) {
      SizeConfig().initSize(context);

      String idPromo = dataProduk!['idPromo'];
      String idPromoItem = dataProduk!['idPromoItem'];
      // String namaPromo = dataProduk!['namaPromo'];
      // String _catatan = dataProduk!['catatanPromo'];

      int diskonPromo = int.parse(dataProduk!['diskonPromo']);
      int hargaKotor = int.parse(dataProduk!['hargaProduk']);
      int diskon = int.parse(dataProduk!['diskonProduk']);

      int _hargaBersihPromo = hargaKotor - diskon - diskonPromo;
      double persentaseDiskon = 0;

      String idProduk = dataProduk!['idProduk'];
      String idSatuan = dataProduk!['idHargaProduk'];

      String fotoProduk = dataProduk!['fotoProduk'];

      bool isDefaultImage = (fotoProduk == DefaultImage.defaultProduk);

      if (diskon >= 1 && hargaKotor >= 1) {
        persentaseDiskon = diskon / hargaKotor * 100;
      }

      String _maxPesan = dataProduk!['maxOrder'];

      String _deskripsiProduk = dataProduk!['keteranganSingkat'] ?? '';

      return InkWell(
        onTap: () {
          Navigator.push(context, MaterialPageRoute(builder: (_) {
            return DetailPromoItem(idPromoItem);
          }));
        },
        child: Card(
          margin: EdgeInsets.only(bottom: 10, left: 1.5, right: 1.5),
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
          child: Container(
            width: widgetWidth,
            decoration: BoxDecoration(borderRadius: BorderRadius.circular(7.5)),
            child: Center(
              child: Column(
                children: [
                  Expanded(
                      child: LayoutBuilder(builder: (context, constraints) {
                    return Stack(
                      children: [
                        Container(
                            constraints: BoxConstraints(
                                minHeight: constraints.maxHeight,
                                minWidth: constraints.maxWidth),
                            margin: EdgeInsets.only(bottom: 10.0),
                            decoration: BoxDecoration(
                                borderRadius: BorderRadius.only(
                                    topLeft: Radius.circular(10.0),
                                    topRight: Radius.circular(10.0)),
                                image: DecorationImage(
                                    image: NetworkImage(ServerPath.baseURL +
                                        '/' +
                                        ServerPath.uploadGambarProduk +
                                        '/compress/' +
                                        fotoProduk),
                                    fit: (isDefaultImage)
                                        ? BoxFit.contain
                                        : BoxFit.fill))),
                        (persentaseDiskon >= 0.1)
                            ? Positioned(
                                top: 5.0,
                                right: 5.0,
                                child: Container(
                                    margin: EdgeInsets.only(left: 5),
                                    child: Text(
                                        'Hemat ' +
                                            persentaseDiskon
                                                .toStringAsFixed(1) +
                                            ' %',
                                        style: TextStyle(
                                            fontSize:
                                                SizeConfig.horizontalBlock *
                                                    2.0)),
                                    padding: EdgeInsets.symmetric(
                                        horizontal: 7.5, vertical: 2.5),
                                    decoration: BoxDecoration(
                                        color: CustomColors.textWarning,
                                        borderRadius:
                                            BorderRadius.circular(50))))
                            : SizedBox(),
                        Positioned(
                          left: 5.0,
                          right: 5.0,
                          bottom: 20.0,
                          child: Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Container(
                                    padding: EdgeInsets.symmetric(
                                        horizontal: 7.5, vertical: 2.5),
                                    decoration: BoxDecoration(
                                        borderRadius: BorderRadius.all(
                                            Radius.circular(15.0)),
                                        color: CustomColors.textWarning),
                                    child: Text('Max ' + _maxPesan + 'x pesan',
                                        style: TextStyle(
                                            fontSize:
                                                SizeConfig.horizontalBlock *
                                                    2.0))),
                              ]),
                        )
                      ],
                    );
                  })),
                  Expanded(
                    child: Padding(
                      padding: EdgeInsets.only(bottom: 10.0),
                      child: SingleChildScrollView(
                        child: Container(
                          padding: EdgeInsets.symmetric(horizontal: 5.0),
                          child: Column(children: [
                            Text(dataProduk!['namaProduk'],
                                overflow: TextOverflow.ellipsis,
                                style: TextStyle(
                                    fontSize: SizeConfig.horizontalBlock * 3.5,
                                    fontWeight: FontWeight.w700),
                                textAlign: TextAlign.center),
                            SizedBox(height: 2),
                            SingleChildScrollView(
                              scrollDirection: Axis.horizontal,
                              child: Text(
                                  (dataProduk!['namaToko'].length >= 1)
                                      ? dataProduk!['namaToko']
                                      : 'Penjual Tidak Diketahui',
                                  textAlign: TextAlign.center,
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 2.75)),
                            ),
                            (_deskripsiProduk.length >= 1)
                                ? Padding(
                                    padding: EdgeInsets.only(
                                        top: 5.0, right: 5.0, left: 5.0),
                                    child: SingleChildScrollView(
                                      scrollDirection: Axis.horizontal,
                                      child: Text(_deskripsiProduk,
                                          maxLines: 2,
                                          style: CustomText.textXS().copyWith(
                                              color: CustomColors.textMuted,
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      2.75)),
                                    ),
                                  )
                                : SizedBox(),
                            SizedBox(height: 5.0),
                            Container(
                                margin: EdgeInsets.only(bottom: 3.5, top: 2.5),
                                child: Column(
                                  mainAxisSize: MainAxisSize.min,
                                  mainAxisAlignment: MainAxisAlignment.center,
                                  children: [
                                    Text(
                                        Currency.formatRupiah(
                                            _hargaBersihPromo, 0),
                                        style: CustomText.hargaBersih()
                                            .copyWith(
                                                fontSize:
                                                    SizeConfig.horizontalBlock *
                                                        2.5)),
                                    SizedBox(height: 3.5),
                                    Text(Currency.formatRupiah(hargaKotor, 0),
                                        style: CustomText.hargaCoret().copyWith(
                                            fontSize:
                                                SizeConfig.horizontalBlock *
                                                    2.5,
                                            color: CustomColors.nearlyBlack))
                                  ],
                                )),
                            (dataProduk != null)
                                ? Padding(
                                    padding: EdgeInsets.only(
                                        top: 5.0, right: 10.0, left: 10.0),
                                    child: CustomButton.button(context, 'Beli',
                                        () async {
                                      Map<String, dynamic>? _response =
                                          await ProdukModel.addToCart(
                                              idProduk, idSatuan, "1",
                                              idPromo: idPromo);

                                      if (_response != null) {
                                        int jumlahKeranjang =
                                            await HeaderModel.getJumlahCart();

                                        bool statusAddCart =
                                            _response['statusAddCart'];

                                        if (statusAddCart) {
                                          SnackBar snackBar = SnackBar(
                                              duration: Duration(seconds: 3),
                                              action: SnackBarAction(
                                                label: 'Lihat Keranjang',
                                                textColor: CustomColors.green,
                                                onPressed: () {
                                                  Navigator.push(
                                                      context,
                                                      MaterialPageRoute(
                                                          builder: (_) =>
                                                              Keranjang()));
                                                },
                                              ),
                                              content: Text(
                                                  'Berhasil ditambahkan ke dalam keranjang !',
                                                  style: TextStyle(
                                                      fontSize: SizeConfig
                                                              .horizontalBlock *
                                                          3.25)));
                                          ScaffoldMessenger.of(context)
                                              .showSnackBar(snackBar);
                                          Provider.of<HeaderViewModel>(context,
                                                  listen: false)
                                              .jumlahCart = jumlahKeranjang;
                                        } else {
                                          String _messageCart =
                                              'Maaf mak, produk gagal ditambahkan ke dalam keranjang. Coba lagi yah...';
                                          if (_response
                                              .containsKey('message')) {
                                            _messageCart = _response['message'];
                                          }

                                          String _title = 'Keranjang';
                                          String _subTitle = _messageCart;
                                          Widget _actions = Row(
                                              mainAxisAlignment:
                                                  MainAxisAlignment.center,
                                              children: [
                                                CustomButton.button(
                                                    context, 'OK', () {
                                                  Navigator.pop(context);
                                                }, color: CustomColors.green)
                                              ]);
                                          await CustomAlert.textComponent(
                                              context,
                                              title: _title,
                                              subTitle: _subTitle,
                                              actions: _actions);
                                        }
                                      }
                                    },
                                        size: CustomButton.xsSize,
                                        color: CustomButton.themeColor,
                                        isPill: true),
                                  )
                                : SizedBox()
                          ]),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      );
    } else {
      return SizedBox();
    }
  }
}
