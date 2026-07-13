import 'package:flutter/material.dart';
import 'package:laukpauk/element-class/Gift.dart';
import 'package:laukpauk/element-class/GiftTransaksi.dart';
import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/CustomText.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/models/GiftModel.dart';

import 'package:laukpauk/models/GiftTransaksiModel.dart';

class MyGift extends StatefulWidget {
  final String? cartCode;
  const MyGift({Key? key, this.cartCode}) : super(key: key);

  @override
  State<MyGift> createState() => _MyGiftState();
}

class _MyGiftState extends State<MyGift> {
  @override
  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    double _hb = SizeConfig.horizontalBlock;

    String? _cartCode = widget.cartCode;

    return Scaffold(
      appBar: AppBar(title: Text('Gift'), backgroundColor: CustomColors.green),
      backgroundColor: CustomColors.nearlyWhite,
      body: Container(
        padding: EdgeInsets.symmetric(horizontal: 5.0, vertical: 5.0),
        child: FutureBuilder<List<GiftTransaksi>>(
            future: GiftTransaksiModel.getGiftTransaksi(cartCode: _cartCode),
            builder: ((context, snapshot) {
              if (snapshot.hasData) {
                List<GiftTransaksi>? _data = snapshot.data;
                if (_data != null) {
                  List<GiftTransaksi> _listGiftTransaksi = _data;

                  if(_listGiftTransaksi.length >= 1){
                    return SingleChildScrollView(
                    child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: _listGiftTransaksi.map((giftTransaksi) {
                          GiftTransaksi _gT = giftTransaksi;
                          String _idGift = _gT.idGift!;

                          return FutureBuilder<Gift?>(
                              future: GiftModel.getGift(_idGift),
                              builder: (context, giftSnapshot) {
                                if (snapshot.hasData) {
                                  Gift? _g = giftSnapshot.data;
                                  if (_g != null) {
                                    String _nama = _g.nama ?? 'Gift';
                                    String _foto = _g.foto ?? 'gift-default.png';
                                    String _keterangan  = _g.deskripsi ?? '';

                                    return Card(
                                      color: Colors.white,
                                      shape: RoundedRectangleBorder(
                                        borderRadius: BorderRadius.all(Radius.circular(100.0))),
                                      shadowColor: CustomColors.textMuted,
                                      elevation: 5.0,
                                      child: ListTile(
                                        leading: ClipRRect(
                                          borderRadius: BorderRadius.circular(50.0),
                                          child: Container(
                                            width: 50.0,
                                            height: 50.0,
                                            decoration: BoxDecoration(image: DecorationImage(
                                              fit: BoxFit.fill,
                                              image: NetworkImage('${ServerPath.baseURL}/${ServerPath.uploadGambarGift}/compress/$_foto'))),)),
                                        title: Text(_nama, style: CustomText.textSM()),
                                        subtitle: Text(_keterangan, style: CustomText.textXS().copyWith(color: CustomColors.textMuted))),
                                    );
                                  }
                                }
                                return Loading(
                                    circularProgressIndicatorSize:
                                        Size(35.0, 35.0));
                              });
                        }).toList()),
                  );
                  }
                }

                return Center(
                    child: Padding(
                  padding: EdgeInsets.symmetric(horizontal: _hb * 10.0),
                  child: Column(mainAxisSize: MainAxisSize.min, children: [
                    Image.asset('${ClientPath.assetsImg}/empty.png',
                        width: _hb * 35.0),
                    SizedBox(height: 15.0),
                    Text('Hadiah', style: CustomText.textSM()),
                    SizedBox(height: 10.0),
                    Text(
                        'Yaahhhhh, hadiahnya belum bisa didapat ya mak ... Yuk belanja lebih banyak lagi mak supaya dapat hadiah...',
                        textAlign: TextAlign.center,
                        style: CustomText.textXS()),
                    SizedBox(height: 25.0),
                    CustomButton.button(context, 'Belanja Lagi', () { 
                      Navigator.pop(context);
                    }, color: CustomColors.green, isPill: true)
                  ]),
                ));
              } else {
                return Loading(circularProgressIndicatorSize: Size(25.0, 25.0));
              }
            })),
      ),
    );
  }
}
