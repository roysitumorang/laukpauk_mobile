import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/models/AuthModel.dart';
import 'package:laukpauk/views/pembeli/Login.dart';
import 'package:share/share.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/global-components/CustomButton.dart';

class KodeReferal extends StatefulWidget {
  _KodeReferalState createState() => _KodeReferalState();
}

class _KodeReferalState extends State<KodeReferal> {
  Future<dynamic> getReferralConfiguration() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Pengaturan/getReferralConfiguration?' +
        _params);
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      return dataObject['referralConfiguration'];
    }
    return false;
  }

  Future<dynamic> getDetailUser() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/User/getDetailUser?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      return dataObject['detailUser'];
    }
    return false;
  }

  getReferralUserWallet() async {
    int referralUserWallet = 0;
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Pengaturan/getSettingValue/referralUserWallet?' +
        _params);
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      referralUserWallet = int.parse(dataObject['settingValue']);
    }

    return referralUserWallet;
  }

  bagikan(BuildContext context, detailActiveUser) async {
    String _nama = detailActiveUser['nama'];
    String kodeReferal = detailActiveUser['kodeReferal'];
    int nilaiDompet = await getReferralUserWallet();
    String nilaiDompetString = Currency.formatRupiah(nilaiDompet, 0);

    await Share.share(
        'Temanmu *$_nama* mengirimkan Kode Referal *$kodeReferal* senilai *$nilaiDompetString* untuk pembelian kebutuhan dapur pertamamu di Laukpauk. \n\nLangkah mudah belanja di Laukpauk ke Pedagang Pasar Tradisional : \n*Pilih Pasar - Belanja - Tunggu dirumah*\n\nYukk, teman-teman di Area Medan sekitarnya kalau apakali, belanja di https://laukpauk.id/app aja!',
        subject: 'Kode Referal $kodeReferal');
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    double width = MediaQuery.of(context).size.width;

    return FutureBuilder<bool>(
        future: AuthModel.isLogin(),
        builder: (loginContext, snapshot) {
          if (snapshot.hasData) {
            if (snapshot.data != null) {
              bool _isUserLoggedIn = snapshot.data!;
              if (_isUserLoggedIn) {
                return FutureBuilder(
                    builder: (context, snapshot) {
                      if (snapshot.hasData) {
                        dynamic referralConfiguration = snapshot.data;
                        if (referralConfiguration != false) {
                          int _referralWallet =
                              referralConfiguration['referralWallet'];
                          String _formattedReferralWallet =
                              Currency.formatRupiah(_referralWallet, 0);

                          return Scaffold(
                              appBar: AppBar(
                                  title: Text('Kode Referal'),
                                  backgroundColor: CustomColors.green),
                              backgroundColor: CustomColors.nearlyWhite,
                              body: Container(
                                  width: width,
                                  padding: EdgeInsets.symmetric(
                                      horizontal:
                                          SizeConfig.horizontalBlock * 5.0,
                                      vertical: 5),
                                  child: RefreshIndicator(
                                    onRefresh: () async {
                                      setState(() {});
                                    },
                                    child: ListView(children: [
                                      Container(
                                          padding: EdgeInsets.symmetric(
                                              vertical: 35),
                                          child: Column(
                                              mainAxisAlignment:
                                                  MainAxisAlignment.center,
                                              crossAxisAlignment:
                                                  CrossAxisAlignment.center,
                                              children: [
                                                Text('Kode Referal Anda',
                                                    style: TextStyle(
                                                        fontSize: SizeConfig
                                                                .horizontalBlock *
                                                            5,
                                                        fontWeight:
                                                            FontWeight.w700,
                                                        color: CustomColors
                                                            .nearlyBlack)),
                                                SizedBox(height: 5),
                                                FutureBuilder(
                                                    future: getDetailUser(),
                                                    builder:
                                                        (context, snapshot) {
                                                      if (snapshot.hasData) {
                                                        dynamic detailUser =
                                                            snapshot.data;
                                                        if (detailUser !=
                                                            false) {
                                                          String
                                                              kodeReferalString =
                                                              detailUser[
                                                                  'kodeReferal'];
                                                          return Container(
                                                            child: Column(
                                                              children: [
                                                                Text(
                                                                    kodeReferalString,
                                                                    style: TextStyle(
                                                                        letterSpacing:
                                                                            1.5,
                                                                        fontSize:
                                                                            SizeConfig.horizontalBlock *
                                                                                4.5,
                                                                        fontWeight:
                                                                            FontWeight
                                                                                .w700,
                                                                        color: CustomColors
                                                                            .green)),
                                                                SizedBox(
                                                                    height: 15),
                                                                Row(
                                                                    mainAxisAlignment:
                                                                        MainAxisAlignment
                                                                            .center,
                                                                    children: [
                                                                      CustomButton.button(
                                                                          context,
                                                                          'Salin',
                                                                          () {
                                                                        Clipboard.setData(ClipboardData(
                                                                            text:
                                                                                kodeReferalString));
                                                                        SnackBar
                                                                            snackBar =
                                                                            SnackBar(content: Text('Kode Referal Tersalin !', style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25)));

                                                                        ScaffoldMessenger.of(context)
                                                                            .showSnackBar(snackBar);
                                                                      },
                                                                          outline:
                                                                              true,
                                                                          isPill:
                                                                              true,
                                                                          color:
                                                                              CustomButton.themeColor),
                                                                      SizedBox(
                                                                          width:
                                                                              10),
                                                                      Builder(
                                                                          builder: (BuildContext context) =>
                                                                              CustomButton.button(context, 'Bagikan', () {
                                                                                bagikan(context, detailUser);
                                                                              }, color: CustomButton.themeColor, isPill: true))
                                                                    ])
                                                              ],
                                                            ),
                                                          );
                                                        } else {
                                                          return Text(
                                                              'Akun Anda tidak Dikenal',
                                                              style: TextStyle(
                                                                  letterSpacing:
                                                                      1.5,
                                                                  fontSize: 18,
                                                                  fontWeight:
                                                                      FontWeight
                                                                          .w700,
                                                                  color: CustomColors
                                                                      .textDanger));
                                                        }
                                                      } else {
                                                        return Loading(
                                                            circularProgressIndicatorSize:
                                                                Size(
                                                                    20.0, 20.0),
                                                            showLoadingImage:
                                                                true);
                                                      }
                                                    })
                                              ])),
                                      Text(
                                          'Silahkan bagikan kode ini ke keluarga, saudara, teman atau kepada siapa saja untuk mendapatkan uang belanja gratis sebesar '),
                                      Text(_formattedReferralWallet,
                                          style: TextStyle(
                                              fontWeight: FontWeight.bold)),
                                      SizedBox(height: 15.0),
                                      Text(
                                          'Uang belanja gratis akan otomatis masuk ke dompet anda ketika si penerima referral melakukan belanja, dan sudah selesai '),
                                      Text('(COMPLETED)',
                                          style: TextStyle(
                                              fontWeight: FontWeight.bold))
                                    ]),
                                  )));
                        } else {
                          return Scaffold(
                              body: Padding(
                            padding: EdgeInsets.symmetric(
                                horizontal: 50.0, vertical: 10),
                            child: Center(
                              child: Column(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  Image.asset(
                                      ClientPath.assetsImg + '/empty.png',
                                      width: SizeConfig.horizontalBlock * 25.0),
                                  SizedBox(height: 5),
                                  Text(
                                      'Pengaturan referral belum dibuat (Nilai Wallet dan User Wallet) dsb',
                                      style: TextStyle(
                                          color: CustomColors.textDanger,
                                          fontSize:
                                              SizeConfig.horizontalBlock * 3.5),
                                      textAlign: TextAlign.center),
                                  SizedBox(height: 15)
                                ],
                              ),
                            ),
                          ));
                        }
                      } else {
                        return Scaffold(body: Loading());
                      }
                    },
                    future: getReferralConfiguration());
              }
            }
            return Login(routeTo: KodeReferal());
          } else {
            return Scaffold(body: Loading());
          }
        });
  }
}
