import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/models/PointModel.dart';

import 'package:laukpauk/views/pembeli/HistoriDompet.dart';
import 'package:laukpauk/views/pembeli/HistoriPoin.dart';
import 'package:laukpauk/views/pembeli/KodeReferal.dart';
import 'package:laukpauk/views/pembeli/Voucher.dart';

import 'package:laukpauk/models/AuthModel.dart';
import 'package:laukpauk/models/HomepageModel.dart';

class WalletAndVoucher extends StatefulWidget {
  WalletAndVoucherState createState() => WalletAndVoucherState();
}

class WalletAndVoucherState extends State<WalletAndVoucher> {
  bool _isUserLogin = false;

  void _initialization() async {
    bool isLogin = await AuthModel.isLogin();

    setState(() {
      _isUserLogin = isLogin;
    });
  }

  void initState() {
    super.initState();
    _initialization();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    double _iconWidthAndHeight = SizeConfig.horizontalBlock * 5.0;
    double _iconAndColumnSpace = 15.0;

    TextStyle _titleStyle = TextStyle(
        fontSize: SizeConfig.horizontalBlock * 3.0,
        fontWeight: FontWeight.w500);
    TextStyle _contentStyle =
        TextStyle(fontSize: SizeConfig.horizontalBlock * 2.25);

    TextStyle _textSMStyle =
        TextStyle(fontSize: SizeConfig.horizontalBlock * 2.0);

    return Container(
        decoration: BoxDecoration(
            color: CustomColors.nearlyWhite,
            borderRadius: BorderRadius.circular(10.0),
            border: Border.all(width: 1.25, color: CustomColors.green)),
        child: Padding(
          padding: EdgeInsets.symmetric(horizontal: 8.0, vertical: 10.0),
          child:
              Row(mainAxisAlignment: MainAxisAlignment.spaceEvenly, children: [
            InkWell(
              onTap: () {
                Navigator.push(context,
                    MaterialPageRoute(builder: (_) => HistoriDompet()));
              },
              child: Row(
                children: [
                  Image.asset(
                      ClientPath.assetsImg + '/icons/pembeli/dompet.png',
                      width: _iconWidthAndHeight,
                      height: _iconWidthAndHeight),
                  SizedBox(width: _iconAndColumnSpace),
                  Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Dompet', style: _titleStyle),
                        SizedBox(height: 3),
                        (!_isUserLogin)
                            ? SizedBox()
                            : FutureBuilder<int>(
                                future: HomepageModel.getTotalDompet(),
                                builder: (context, snapshot) {
                                  if (snapshot.hasData) {
                                    if (snapshot.data != null) {
                                      int totalDompet = snapshot.data!;
                                      return Text(
                                          Currency.formatRupiah(totalDompet, 0),
                                          style: _contentStyle);
                                    }
                                  }
                                  return Text('Mengambil data ...',
                                      style: _textSMStyle);
                                })
                      ])
                ],
              ),
            ),
            InkWell(
              onTap: () {
                Navigator.push(
                    context, MaterialPageRoute(builder: (_) => Voucher()));
              },
              child: Row(
                children: [
                  Image.asset(
                      ClientPath.assetsImg + '/icons/pembeli/voucher.png',
                      width: _iconWidthAndHeight,
                      height: _iconWidthAndHeight),
                  SizedBox(width: _iconAndColumnSpace),
                  Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Voucher', style: _titleStyle),
                        SizedBox(height: 3),
                        (!_isUserLogin)
                            ? SizedBox()
                            : FutureBuilder<int>(
                                future: HomepageModel.getJumlahVoucher(),
                                builder: (context, snapshot) {
                                  if (snapshot.hasData) {
                                    if (snapshot.data != null) {
                                      int jumlahVoucher = snapshot.data!;
                                      return Text(
                                          (jumlahVoucher >= 1)
                                              ? Currency.formatSatuan(
                                                  jumlahVoucher,
                                                  satuan: 'Voucher')
                                              : 'Belum Ada Voucher',
                                          style: _contentStyle);
                                    }
                                  }
                                  return Text('Mengambil data ...',
                                      style: _textSMStyle);
                                })
                      ])
                ],
              ),
            ),
            GestureDetector(
              onTap: () {
                Navigator.push(
                    context, MaterialPageRoute(builder: (_) => HistoriPoin()));
              },
              child: Row(
                children: [
                  Image.asset(ClientPath.assetsImg + '/poin.png',
                      width: _iconWidthAndHeight, height: _iconWidthAndHeight),
                  SizedBox(width: _iconAndColumnSpace),
                  Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Poin', style: _titleStyle),
                        SizedBox(height: 3),
                        (!_isUserLogin)
                            ? SizedBox()
                            : FutureBuilder(
                                future: PointModel.getPoint(
                                    queryString: 'point=true'),
                                builder: (context, snapshot) {
                                  if (snapshot.hasData) {
                                    dynamic _responsePoint = snapshot.data;
                                    if (_responsePoint != false) {
                                      int _totalPoint = _responsePoint['point'];
                                      return Text(
                                          Currency.formatRupiah(_totalPoint, 0),
                                          style: _contentStyle);
                                    } else {
                                      return Text('Rp. 0',
                                          style: _contentStyle);
                                    }
                                  } else {
                                    return Text('Mengambil data ...',
                                        style: _textSMStyle);
                                  }
                                })
                      ])
                ],
              ),
            ),
            GestureDetector(
              onTap: () {
                Navigator.push(
                    context, MaterialPageRoute(builder: (_) => KodeReferal()));
              },
              child: Row(
                children: [
                  Image.asset(ClientPath.assetsImg + '/referal.png',
                      width: _iconWidthAndHeight, height: _iconWidthAndHeight),
                  SizedBox(width: _iconAndColumnSpace),
                  Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Kode', style: _titleStyle),
                        Text('Referal', style: _titleStyle),
                      ])
                ],
              ),
            )
          ]),
        ));
  }
}
