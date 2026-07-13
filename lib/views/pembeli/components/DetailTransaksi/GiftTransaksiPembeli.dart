import 'package:flutter/material.dart';
import 'package:laukpauk/element-class/Gift.dart';

import 'package:laukpauk/global-components/Loading.dart';

import 'package:laukpauk/element-class/GiftTransaksi.dart';
import 'package:laukpauk/models/GiftModel.dart';

import 'package:laukpauk/models/GiftTransaksiModel.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class GiftTransaksiPembeli extends StatefulWidget {
  final String? idTransaksi;
  final String? cartCode;
  final bool scaffold, useCard;
  GiftTransaksiPembeli(
      {this.idTransaksi, this.cartCode, this.scaffold = false, this.useCard = false});

  _GiftTransaksiPembeliState createState() => _GiftTransaksiPembeliState();
}

class _GiftTransaksiPembeliState extends State<GiftTransaksiPembeli> {


  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String? _cartCode = widget.cartCode;

    TextStyle _contentSize(BuildContext context,
        {Color? color, TextDecoration? textDecoration}) {

      return TextStyle(
          decoration: textDecoration,
          fontSize: SizeConfig.horizontalBlock * 3.25,
          color: (color == null) ? CustomColors.nearlyBlack : color);
    }

    TextStyle _deskripsiSize(BuildContext context,
        {Color? color, TextDecoration? textDecoration}) {

      return TextStyle(
          decoration: textDecoration,
          fontSize: SizeConfig.horizontalBlock * 2.5,
          color: (color == null) ? CustomColors.textMuted : color);
    }

    Widget _futureBuilderWidget = (_cartCode != null)
        ? FutureBuilder<List<GiftTransaksi>>(
            future: GiftTransaksiModel.getGiftTransaksi(cartCode: _cartCode),
            builder: (context, snapshot) {
              bool _hasData = snapshot.hasData;
              if (_hasData) {
                List<GiftTransaksi>? _listGiftTransaksi = snapshot.data;
                if (_listGiftTransaksi != null) {
                  if (_listGiftTransaksi.length >= 1) {

                    List<Widget> _giftWidget  = _listGiftTransaksi.map((GiftTransaksi giftTransaksi){
                      String _idGift  = giftTransaksi.idGift!;

                      return FutureBuilder<Gift?>(
                        future: GiftModel.getGift(_idGift),
                        builder: (context, giftSnapshot){
                          bool _giftHasData   = giftSnapshot.hasData;
                          if(_giftHasData){
                            Gift? _giftDetail  = giftSnapshot.data;
                            if(_giftDetail != null){
                              String _namaGift  = _giftDetail.nama ?? '';
                              String _deskripsiGift = _giftDetail.deskripsi ?? '';
                              return Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Column(
                                    mainAxisSize: MainAxisSize.min,
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text('$_namaGift 1 pcs', style: _contentSize(context)),
                                      Text(_deskripsiGift, style: _deskripsiSize(context)),
                                    ],
                                  ),
                                  Text('Rp. 0', style: _contentSize(context))
                                ]
                              );
                            }
                            return Text('Gift Tidak Teridentifikasi!');
                          }

                          return Loading(circularProgressIndicatorSize: Size(15.0, 15.0));
                        });
                    }).toList();

                    Widget _listGiftTransaksiWidget = Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                mainAxisAlignment: MainAxisAlignment.start,
                                mainAxisSize: MainAxisSize.min,
                                children: _giftWidget
                                );

                    bool _useCard  = widget.useCard;
                    if(_useCard){
                      _listGiftTransaksiWidget  =  Card(
                        elevation: 5,
                        shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(10)),
                        child: Padding(
                            padding: EdgeInsets.symmetric(
                                horizontal: 15, vertical: 10),
                            child: _listGiftTransaksiWidget));
                    }

                    return _listGiftTransaksiWidget;
                  }
                  return SizedBox();
                }
              }
              return Loading();
            })
        : Text('Cart code tidak terdefinisi!');

    return (widget.scaffold)
        ? Scaffold(
            appBar: AppBar(
                title: Text('Hadiah Belanja'),
                backgroundColor: CustomColors.green),
            body: Padding(
                padding: EdgeInsets.symmetric(horizontal: 5, vertical: 5),
                child: _futureBuilderWidget),
          )
        : _futureBuilderWidget;
  }
}
