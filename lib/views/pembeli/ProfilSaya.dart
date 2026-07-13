import 'dart:convert';

import 'package:flutter/material.dart';

import 'package:laukpauk/global-components/CustomForm.dart';
import 'package:laukpauk/libraries/CustomText.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/models/TransaksiModel.dart';
import 'package:onesignal_flutter/onesignal_flutter.dart';
import 'package:provider/provider.dart';
import 'package:http/http.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/ServerPath.dart';

import 'package:laukpauk/views/pembeli/components/ProfilSaya/GantiPassword.dart';
import 'package:laukpauk/views/pembeli/components/ProfilSaya/GantiFotoProfil.dart';
import 'package:laukpauk/views/pembeli/components/ProfilSaya/GantiNomorHP.dart';
import 'package:laukpauk/models/ProfilSayaModel.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';

import 'package:laukpauk/views/SplashScreen.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ProfilSaya extends StatefulWidget {
  _ProfilSayaState createState() => _ProfilSayaState();
}

class _ProfilSayaState extends State<ProfilSaya> {
  bool isSimpanPerubahanDataBusy = false;

  TextEditingController namaLengkapController = TextEditingController();
  TextEditingController alamatController = TextEditingController();
  TextEditingController usernameController = TextEditingController();

  void dispose() {
    super.dispose();

    namaLengkapController.dispose();
    alamatController.dispose();
    usernameController.dispose();
  }

  initializeTextFieldValue() async {
    dynamic detailUser = await ProfilSayaModel.getDetailActiveUser();

    if (detailUser != false) {
      namaLengkapController.text = detailUser['nama'];
      alamatController.text = detailUser['alamat'];
      usernameController.text = detailUser['username'];
    }
  }

  void initState() {
    super.initState();
    initializeTextFieldValue();
  }

  void _logout() async {
    String cartCode = await LocalStorage.getCartCode();

    String _params = await HTTPRequest.getQueryString(
        authToken: true, params: 'cartCode=$cartCode');

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Auth/removeToken?' + _params);
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      if (dataObject['removeToken']) {
        SharedPreferences _sP = await SharedPreferences.getInstance();
        _sP.remove('authToken');

        OneSignal.shared.removeExternalUserId();
        await LocalStorage.removeExternalUserID();
        await TransaksiModel.removeTransactionSession(context);

        Provider.of<HeaderViewModel>(context, listen: false).jumlahNotifikasi =
            0;

        await Navigator.pushReplacement(
            context, MaterialPageRoute(builder: (_) => SplashScreen()));
      }
    }
  }

  final double _borderWidth = 1.0;
  final Color _borderColor = CustomColors.green;

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Scaffold(
      backgroundColor: CustomColors.nearlyWhite,
      appBar: AppBar(
          title: Text('Profil Saya'),
          backgroundColor: CustomColors.green,
          actions: [
            TextButton(
                onPressed: () => _logout(),
                child: Text('Keluar',
                    style: TextStyle(
                        color: CustomColors.nearlyWhite,
                        fontWeight: FontWeight.w500)))
          ]),
      body: FutureBuilder(
          builder: (context, snapshot) {
            if (snapshot.hasData) {
              dynamic profilSaya = snapshot.data;
              if (profilSaya != false) {
                return Padding(
                    padding: EdgeInsets.symmetric(horizontal: 5, vertical: 5),
                    child: Column(mainAxisSize: MainAxisSize.min, children: [
                      GantiFotoProfil(
                          detailDataUser: profilSaya,
                          gantiFotoProfilCallback: (statusGantiFotoProfil) {
                            if (statusGantiFotoProfil) {
                              setState(() {});
                            }
                          }),
                      Expanded(
                        child: SingleChildScrollView(
                          child:
                              Column(mainAxisSize: MainAxisSize.min, children: [
                            Padding(
                                padding: EdgeInsets.symmetric(
                                    horizontal: 15, vertical: 15),
                                child: Container(
                                    key: null,
                                    width: MediaQuery.of(context).size.width,
                                    child: Column(
                                        crossAxisAlignment:
                                            CrossAxisAlignment.start,
                                        children: [
                                          Text('Profil',
                                              style: TextStyle(
                                                  fontSize: SizeConfig
                                                          .horizontalBlock *
                                                      4.0,
                                                  fontWeight: FontWeight.w700)),
                                          SizedBox(height: 5),
                                          Text(
                                              'Anda dapat mengubah data profil anda dengan mengisi form di bawah ini',
                                              style: CustomText.textXS()),
                                          SizedBox(height: 15),
                                          CustomForm.label(context,
                                              label: 'Nama Lengkap'),
                                          CustomForm.textField(context,
                                              hintText: 'Nama Lengkap',
                                              controller: namaLengkapController,
                                              borderColor: _borderColor,
                                              borderWidth: _borderWidth),
                                          SizedBox(height: 15.0),
                                          CustomForm.label(context,
                                              label: 'Alamat Lengkap'),
                                          CustomForm.textField(context,
                                              hintText: 'Alamat Lengkap',
                                              controller: alamatController,
                                              borderColor: _borderColor,
                                              borderWidth: _borderWidth),
                                          SizedBox(height: 15.0),
                                          CustomForm.label(context,
                                              label: 'Username'),
                                          CustomForm.textField(context,
                                              hintText:
                                                  'Nama Pengguna (Username)',
                                              controller: usernameController,
                                              borderColor: _borderColor,
                                              borderWidth: _borderWidth),
                                          SizedBox(height: 15),
                                          CustomButton.button(context,
                                              'Simpan Perubahan Data Anda',
                                              () async {
                                            setState(() {
                                              isSimpanPerubahanDataBusy = true;
                                            });
                                            Map<String, dynamic> dataUser = {
                                              'namaLengkap':
                                                  namaLengkapController.text,
                                              'alamat': alamatController.text,
                                              'username':
                                                  usernameController.text
                                            };

                                            bool statusUpdateProfil =
                                                await ProfilSayaModel
                                                    .simpanPerubahanDataProfil(
                                                        dataUser);

                                            if (statusUpdateProfil) {
                                              initializeTextFieldValue();
                                            }

                                            setState(() {
                                              isSimpanPerubahanDataBusy = false;
                                            });
                                          },
                                              isBusy: isSimpanPerubahanDataBusy,
                                              isBlock: true,
                                              color: CustomButton.themeColor),
                                          SizedBox(height: 5)
                                        ]))),
                            GantiNomorHP(profilSaya),
                            GantiPassword()
                          ]),
                        ),
                      )
                    ]));
              } else {
                return Center(
                  child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Image.asset(ClientPath.assetsImg + '/empty.png',
                            width: 150),
                        SizedBox(height: 10),
                        Text('Data Akun Anda tidak Ditemukan',
                            style: TextStyle(
                                color: CustomColors.textDanger, fontSize: 14))
                      ]),
                );
              }
            } else {
              return Center(
                  child: SizedBox(
                      width: 35,
                      height: 35,
                      child: CircularProgressIndicator(strokeWidth: 1.5)));
            }
          },
          future: ProfilSayaModel.getDetailActiveUser()),
    );
  }
}
