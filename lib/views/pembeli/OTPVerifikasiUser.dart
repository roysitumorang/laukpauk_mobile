import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:laukpauk/global-components/CustomAlert.dart';

import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/SplashScreen.dart';
import 'package:laukpauk/views/pembeli/components/OTPVerifikasiUser/ListPasar.dart';
import 'package:laukpauk/views/pembeli/PinGMAPS.dart';

class OTPVerifikasiUser extends StatefulWidget {
  final Widget? routeTo;
  OTPVerifikasiUser({this.routeTo});
  _OTPVerifikasiUser createState() => _OTPVerifikasiUser();
}

class _OTPVerifikasiUser extends State<OTPVerifikasiUser> {
  String lokasiBerjualan = 'pasarTersedia';
  String pasarSelected = '0';
  Map<String, dynamic>? lokasiTersendiriState;
  bool isSellerState = false;

  TextEditingController verificationCodeController = TextEditingController();

  void dispose() {
    verificationCodeController.dispose();
    super.dispose();
  }

  void initialization() async {
    String _params = await HTTPRequest.getQueryString(
        authToken: true, params: 'getUserEntities=isSeller');

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/User/getDetailUser?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      String isSeller = dataObject['detailUser']['isSeller'];

      setState(() {
        isSellerState = (isSeller == '1');
      });
    }
  }

  void initState() {
    super.initState();
    this.initialization();
  }

  void lokasiBerjualanChanged(String lokasiBerjualanSelected,
      {bool pilihLokasiLain = false}) async {
    String lokasiBerjualanSelectedLC = lokasiBerjualanSelected.toLowerCase();
    if (lokasiBerjualanSelectedLC == 'lokasitersendiri') {
      if (lokasiTersendiriState == null || pilihLokasiLain) {
        Map<String, dynamic>? lokasiTersendiriFromMaps = await Navigator.push(
            context,
            MaterialPageRoute(
                builder: (_) =>
                    PinGMAPS(appBarText: 'Pilih Lokasi Berjualan')));
        if (lokasiTersendiriFromMaps != null) {
          setState(() {
            lokasiBerjualan = lokasiBerjualanSelected;
            lokasiTersendiriState = lokasiTersendiriFromMaps;
          });
        } else {
          setState(() {
            lokasiBerjualan = 'pasarTersedia';
          });
        }
      } else {
        setState(() {
          lokasiBerjualan = 'lokasiTersendiri';
        });
      }
    } else {
      setState(() {
        lokasiBerjualan = lokasiBerjualanSelected;
      });
    }
  }

  void konfirmasiOTP() async {
    String idUser = await LocalStorage.getCurrentUserID();

    Map<String, dynamic> otpData = {
      'idUser': idUser,
      'verificationCode': verificationCodeController.text,
      'isSeller': (isSellerState) ? 'yes' : 'no',
    };
    if (isSellerState) {
      otpData['idPasar'] = pasarSelected;
    }

    Map<String, dynamic> _body = await HTTPRequest.postBody(body: otpData);

    Uri _url = Uri.parse(ServerPath.apiBaseURL + '/Auth/verifikasiUser');
    Response httpResponse = await post(_url, body: _body);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      bool isOTPSuccess = dataObject['statusVerifikasiUser'];
      if (isOTPSuccess) {
        Navigator.pushReplacement(
            context,
            MaterialPageRoute(
                builder: (_) => (widget.routeTo != null)
                    ? widget.routeTo!
                    : SplashScreen()));
      } else {
        String _title = 'Verifikasi User';
        String _subTitle = 'Verifikasi User gagal, silahkan ulangi kembali !';
        Widget _actions =
            Row(mainAxisAlignment: MainAxisAlignment.center, children: [
          TextButton(
              child: Text('OK'),
              onPressed: () {
                Navigator.pop(context);
              })
        ]);

        await CustomAlert.textComponent(context,
            title: _title, subTitle: _subTitle, actions: _actions);
      }
    }
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String dan = (isSellerState)
        ? ' dan tentukan lokasi anda berjualan untuk dapat berjualan.'
        : '.';

    bool isFormComplete = (isSellerState)
        ? (verificationCodeController.text.length >= 4 && pasarSelected != '0')
        : verificationCodeController.text.length >= 4;

    TextStyle judul({Color? color}) {
      return TextStyle(
          color: (color == null) ? CustomColors.nearlyBlack : color,
          fontSize: SizeConfig.horizontalBlock * 3.5,
          fontWeight: FontWeight.w700);
    }

    return Scaffold(
        body: SingleChildScrollView(
            child: Container(
      padding: EdgeInsets.symmetric(horizontal: 10, vertical: 15),
      height: MediaQuery.of(context).size.height,
      width: MediaQuery.of(context).size.width,
      decoration: BoxDecoration(color: Color(0xFFF9F9F9)),
      child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Image.asset(ClientPath.assetsImg + '/laukpauk.png',
                width: MediaQuery.of(context).size.width - 100),
            SizedBox(height: 50),
            // Image.asset(ClientPath.assetsImg + '/recovery-password.png',
            //     width: 85),
            // SizedBox(height: 40),
            Padding(
              padding: EdgeInsets.symmetric(horizontal: 25),
              child: Container(
                padding: EdgeInsets.symmetric(horizontal: 30),
                decoration: BoxDecoration(
                    color: CustomColors.lightGreen,
                    borderRadius: BorderRadius.circular(50)),
                child: TextField(
                  style: TextStyle(color: CustomColors.darkText),
                  controller: verificationCodeController,
                  keyboardType: TextInputType.number,
                  cursorColor: CustomColors.darkText,
                  textAlignVertical: TextAlignVertical.center,
                  textAlign: TextAlign.left,
                  decoration: InputDecoration(
                      border: InputBorder.none,
                      hintText: 'Kode Verifikasi',
                      hintStyle: TextStyle(color: CustomColors.darkText)),
                ),
              ),
            ),
            SizedBox(height: 15),
            (isSellerState)
                ? Padding(
                    padding: EdgeInsets.symmetric(horizontal: 10),
                    child: Container(
                        padding:
                            EdgeInsets.symmetric(horizontal: 15, vertical: 10),
                        decoration: BoxDecoration(
                            border: Border.all(
                                width: .5, color: CustomColors.green),
                            color: Color(0x2266CC99),
                            borderRadius: BorderRadius.circular(10)),
                        child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Row(
                                    mainAxisAlignment:
                                        MainAxisAlignment.spaceBetween,
                                    children: [
                                      Text('Pasar Tempat Berjualan',
                                          style: TextStyle(
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      3.75,
                                              fontWeight: FontWeight.w500)),
                                      InkWell(
                                        onTap: () async {
                                          Map<String, dynamic>?
                                              pinnedLocationMap =
                                              await Navigator.push(
                                                  context,
                                                  MaterialPageRoute(
                                                      builder: (_) =>
                                                          PinGMAPS()));
                                          if (pinnedLocationMap != null) {
                                            // String latitude =
                                            //     pinnedLocationMap['lat'];
                                            // String longitude =
                                            //     pinnedLocationMap['lng'];

                                            // String pinnedLocationString =
                                            //     await ADJDModel
                                            //         .reverseGeocoding(
                                            //             latitude: latitude,
                                            //             longitude: longitude);

                                            // Map<String, dynamic> lokasiBaru = {
                                            //   'latitude': latitude,
                                            //   'longitude': longitude,
                                            //   'readAbleAddress':
                                            //       pinnedLocationString
                                            // };
                                          }
                                        },
                                        child: Text('Lokasi Tersendiri',
                                            style: judul(
                                                color:
                                                    CustomColors.textPrimary)),
                                      )
                                    ],
                                  ),
                                  SizedBox(height: 5),
                                  ListPasar(
                                      pasarSelected: pasarSelected,
                                      onPasarClicked: (idPasar) {
                                        setState(() {
                                          pasarSelected = idPasar;
                                        });
                                      }),
                                ],
                              ),
                              SizedBox(height: 10),
                              Text(
                                  'Anda adalah penjual di aplikasi sebelumnya, oleh karena itu anda diminta untuk menentukan lokasi berjualan agar pembeli mudah menemukan anda',
                                  textAlign: TextAlign.center,
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 2.5))
                            ])),
                  )
                : SizedBox(),

            Padding(
              padding: EdgeInsets.symmetric(horizontal: 25, vertical: 10),
              child: Text(
                  'Silahkan masukkan kode OTP Verifikasi User yang dikirim ke nomor yang anda daftarkan$dan',
                  textAlign: TextAlign.center),
            ),
            (isFormComplete) ? SizedBox(height: 15) : SizedBox(),
            (isFormComplete)
                ? Padding(
                    padding: EdgeInsets.symmetric(horizontal: 25),
                    child: CustomButton.button(context, 'Verifikasi', () {
                      konfirmasiOTP();
                    }, color: CustomButton.themeColor, isPill: true))
                : SizedBox()
          ]),
    )));
  }
}
