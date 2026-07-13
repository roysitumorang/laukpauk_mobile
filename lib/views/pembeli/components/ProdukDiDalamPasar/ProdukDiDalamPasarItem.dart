import 'package:flutter/material.dart';
import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/libraries/CustomText.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/views/Penjual.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/DefaultImage.dart';

import 'package:laukpauk/views/pembeli/DetailProduk.dart';
import 'package:laukpauk/models/ProdukModel.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

class ProdukInThisPasarItem extends StatefulWidget {
  final Map<String, dynamic> dataProduk;
  final double? widgetWidth;
  final void Function() deteksiADJD;
  ProdukInThisPasarItem(this.dataProduk, this.deteksiADJD, {this.widgetWidth});

  _ProdukInThisPasarItemState createState() => _ProdukInThisPasarItemState();
}

class _ProdukInThisPasarItemState extends State<ProdukInThisPasarItem> {
  dynamic _lowestPriceState;
  bool isButtonBeliBusy = false;

  void initState() {
    super.initState();
    _initialization();
  }

  void didUpdateWidget(ProdukInThisPasarItem oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.dataProduk != widget.dataProduk) {
      _initialization();
    }
  }

  Future<void> _initialization() async {
    Map<String, dynamic> _dataProduk = widget.dataProduk;
    String _idProduk = _dataProduk['id'];
    dynamic _lowestPrice = await ProdukModel.getLowestPrice(_idProduk);

    setState(() {
      _lowestPriceState = _lowestPrice;
    });
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    Map<String, dynamic> _dataProduk = widget.dataProduk;

    String _namaProduk = _dataProduk['nama'];
    String idPenjual = _dataProduk['idPenjual'];
    String fotoProduk = _dataProduk['foto'];
    String idProduk = _dataProduk['id'];
    String namaToko = _dataProduk['namaToko'] ?? '-';

    bool _isStockAvailable = true;
    if (_dataProduk.containsKey('stokAvailable')) {
      _isStockAvailable = _dataProduk['stokAvailable'] == '1';
    }

    bool isDefaultImage = (fotoProduk == DefaultImage.defaultProduk);

    double _persentaseDiskon = 0;
    int _hargaKotor = 0;
    int _hargaBersih = 0;
    int _diskon = 0;

    if (_lowestPriceState != null) {
      if (_lowestPriceState != false) {
        _diskon = int.parse(_lowestPriceState['diskon']);
        _hargaKotor = int.parse(_lowestPriceState['harga']);
        _hargaBersih = int.parse(_lowestPriceState['hargaBersih']);

        _persentaseDiskon = _diskon / _hargaKotor * 100;
      }
    }

    bool _isInActivePromo = false;
    // List<Map<String, dynamic>> _promoData;
    if (_dataProduk.containsKey('promo')) {
      List _promo = _dataProduk['promo'];
      _isInActivePromo = (_promo.length >= 1);
      // _promoData = _promo;
    }

    String _deskripsiProduk = '';
    if (_dataProduk.containsKey('keteranganSingkat')) {
      String? _deskripsiProdukCanBeNull = _dataProduk['keteranganSingkat'];
      if (_deskripsiProdukCanBeNull != null) {
        _deskripsiProduk = _deskripsiProdukCanBeNull;
      }
    }

    return Container(
        width: widget.widgetWidth,
        margin: EdgeInsets.only(bottom: 5.0, top: 5.0),
        child: InkWell(
          onTap: () {
            Navigator.push(context, MaterialPageRoute(builder: (_) {
              return DetailProduk(idProduk);
            }));
          },
          child: Card(
              elevation: 8.0,
              shadowColor: Color(0x55F9F9F9),
              margin: EdgeInsets.only(left: 2.5, right: 2.5),
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10)),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Expanded(
                    child: LayoutBuilder(builder: (context, constraints) {
                      return Stack(
                        children: [
                          Container(
                              constraints: BoxConstraints(
                                  minWidth: constraints.maxWidth,
                                  minHeight: constraints.maxHeight),
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
                          (_persentaseDiskon > 0)
                              ? Positioned(
                                  top: 5.0,
                                  right: 5.0,
                                  child: Container(
                                      margin: EdgeInsets.only(left: 5),
                                      child: Text(
                                          'Hemat ' +
                                              Currency.formatSatuan(
                                                  _persentaseDiskon
                                                      .ceil()
                                                      .toInt(),
                                                  satuan: '%'),
                                          style: TextStyle(
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      2.75)),
                                      padding: EdgeInsets.symmetric(
                                          horizontal: 10, vertical: 2.5),
                                      decoration: BoxDecoration(
                                          color: Colors.amber,
                                          borderRadius:
                                              BorderRadius.circular(50))),
                                )
                              : SizedBox(),
                          (_isInActivePromo)
                              ? Positioned(
                                  top: 5.0,
                                  child: Container(
                                      margin: EdgeInsets.only(left: 5),
                                      child: Text('Promo',
                                          style: TextStyle(
                                              color: CustomColors.nearlyWhite,
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      2.75)),
                                      padding: EdgeInsets.symmetric(
                                          horizontal: 10, vertical: 2.5),
                                      decoration: BoxDecoration(
                                          color: CustomColors.textDanger,
                                          borderRadius:
                                              BorderRadius.circular(50))),
                                )
                              : SizedBox()
                        ],
                      );
                    }),
                  ),
                  Expanded(
                    child: (_lowestPriceState != null)
                        ? Container(
                            width: widget.widgetWidth,
                            padding: EdgeInsets.only(
                                top: 10.0, left: 7.5, right: 7.5),
                            child: SingleChildScrollView(
                              child: Column(
                                  mainAxisAlignment: MainAxisAlignment.center,
                                  children: [
                                    Column(
                                        mainAxisSize: MainAxisSize.min,
                                        children: [
                                          Text(_namaProduk,
                                              overflow: TextOverflow.ellipsis,
                                              style: TextStyle(
                                                  fontSize: SizeConfig
                                                          .horizontalBlock *
                                                      3.5,
                                                  fontWeight: FontWeight.w700),
                                              textAlign: TextAlign.center),
                                          SizedBox(height: 2),
                                          Container(
                                            padding:
                                                EdgeInsets.only(bottom: 7.5),
                                            child: InkWell(
                                              onTap: () {
                                                Navigator.push(
                                                    context,
                                                    MaterialPageRoute(
                                                        builder: (_) => Penjual(
                                                            id: idPenjual)));
                                              },
                                              child: SingleChildScrollView(
                                                scrollDirection:
                                                    Axis.horizontal,
                                                child: Text(
                                                    (namaToko.length >= 1)
                                                        ? namaToko
                                                        : 'Penjual Tidak Diketahui',
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                        color: CustomColors
                                                            .textMuted,
                                                        fontSize: SizeConfig
                                                                .horizontalBlock *
                                                            2.75)),
                                              ),
                                            ),
                                          ),
                                          (_deskripsiProduk.isNotEmpty)
                                              ? Padding(
                                                  padding: EdgeInsets.only(
                                                      bottom: 7.5),
                                                  child: SingleChildScrollView(
                                                    scrollDirection:
                                                        Axis.horizontal,
                                                    child: Text(
                                                        _deskripsiProduk,
                                                        maxLines: 2,
                                                        style: TextStyle(
                                                            fontSize: SizeConfig
                                                                    .horizontalBlock *
                                                                2.75,
                                                            color: CustomColors
                                                                .textMuted)),
                                                  ),
                                                )
                                              : SizedBox(),
                                          Container(
                                              margin:
                                                  EdgeInsets.only(bottom: 3.5),
                                              child: SingleChildScrollView(
                                                scrollDirection:
                                                    Axis.horizontal,
                                                child: Row(
                                                  mainAxisAlignment:
                                                      MainAxisAlignment.center,
                                                  children: [
                                                    Text(
                                                        Currency.formatRupiah(
                                                            _hargaBersih, 0),
                                                        style: CustomText
                                                                .hargaBersih()
                                                            .copyWith(
                                                                fontSize: SizeConfig
                                                                        .horizontalBlock *
                                                                    2.5)),
                                                    (_persentaseDiskon > 0)
                                                        ? Padding(
                                                            padding:
                                                                EdgeInsets.only(
                                                                    left: 5.0),
                                                            child: Text(
                                                                Currency.formatRupiah(
                                                                    _hargaKotor,
                                                                    0),
                                                                style: CustomText
                                                                        .hargaCoret()
                                                                    .copyWith(
                                                                        fontSize:
                                                                            SizeConfig.horizontalBlock *
                                                                                2.5,
                                                                        color: CustomColors
                                                                            .nearlyBlack)),
                                                          )
                                                        : SizedBox()
                                                  ],
                                                ),
                                              )),
                                          SizedBox(
                                              height:
                                                  (_lowestPriceState != null)
                                                      ? 3.5
                                                      : 0),
                                        ]),
                                    (_isStockAvailable)
                                        ? (_lowestPriceState != null)
                                            ? (_lowestPriceState != false)
                                                ? Padding(
                                                    padding: EdgeInsets.only(
                                                        top: 5.0, bottom: 10.0),
                                                    child: CustomButton.button(
                                                        context, 'Beli',
                                                        () async {
                                                      setState(() {
                                                        isButtonBeliBusy = true;
                                                      });
                                                      bool isADJDSet =
                                                          await LocalStorage
                                                              .isAlamatDeliveryDanJadwalDeliverySet();
                                                      if (isADJDSet) {
                                                        ProdukModel
                                                            .showSatuanProduk(
                                                                context,
                                                                _dataProduk);
                                                      } else {
                                                        bool?
                                                            lakukanDeteksiADJD =
                                                            await ProdukModel
                                                                .adjdNotSetAlert(
                                                                    context);
                                                        if (lakukanDeteksiADJD !=
                                                                null &&
                                                            lakukanDeteksiADJD) {
                                                          widget.deteksiADJD();
                                                        }
                                                      }
                                                      setState(() {
                                                        isButtonBeliBusy =
                                                            false;
                                                      });
                                                    },
                                                        color: CustomButton
                                                            .themeColor,
                                                        size:
                                                            CustomButton.smSize,
                                                        isPill: true,
                                                        isBusy:
                                                            isButtonBeliBusy),
                                                  )
                                                : Text('Tidak ada daftar harga',
                                                    textAlign: TextAlign.center,
                                                    style: CustomText.textXS()
                                                        .copyWith(
                                                            fontSize: SizeConfig
                                                                    .horizontalBlock *
                                                                2.75))
                                            : SizedBox()
                                        : Text('Stok tidak tersedia',
                                            style: TextStyle(
                                                color: CustomColors.textDanger,
                                                fontSize:
                                                    SizeConfig.horizontalBlock *
                                                        2.75))
                                  ]),
                            ),
                          )
                        : Loading(
                            circularProgressIndicatorSize: Size(30.0, 30.0),
                          ),
                  ),
                  (_deskripsiProduk.isNotEmpty)
                      ? SizedBox(height: 15.0)
                      : SizedBox()
                ],
              )),
        ));
  }
}
