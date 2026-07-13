import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';

import 'package:laukpauk/global-components/CustomForm.dart';
import 'package:laukpauk/libraries/CustomText.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/views/pembeli/UserAndDashboard.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/ServerPath.dart';

import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:shared_preferences/shared_preferences.dart';

class GantiNomorHP extends StatefulWidget {
  final Map<String, dynamic> profilSaya;
  GantiNomorHP(this.profilSaya);
  _GantiNomorHPState createState() => _GantiNomorHPState();
}

class _GantiNomorHPState extends State<GantiNomorHP> {
  int idPenggantianNomorState = 0;

  TextEditingController nomorHPController = TextEditingController();
  TextEditingController kodeVerifikasiController = TextEditingController();

  void initialization() async {
    SharedPreferences _sP = await SharedPreferences.getInstance();

    int idPenggantianNomor = _sP.getInt('idPenggantianNomor') ?? 0;

    setState(() {
      idPenggantianNomorState = idPenggantianNomor;
    });
  }

  void initState() {
    super.initState();

    this.initialization();
  }

  void dispose() {
    super.dispose();

    nomorHPController.dispose();
    kodeVerifikasiController.dispose();
  }

  Future<Map<String, dynamic>> simpanNomorBaru() async {
    Map<String, dynamic> httpBody = {'nomorBaru': nomorHPController.text};

    String _params = await HTTPRequest.getQueryString(authToken: true);
    Map<String, dynamic> _body = await HTTPRequest.postBody(body: httpBody);

    Uri _url = Uri.parse(ServerPath.apiBaseURL + '/Auth/gantiNomor?' + _params);

    Response httpResponse = await post(_url, body: _body);
    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      return dataObject;
    }
    return {'statusSimpanNomorBaru': false};
  }

  Future<bool> verifikasi() async {
    SharedPreferences _sP = await SharedPreferences.getInstance();

    String idPenggantianNomor = _sP.getInt('idPenggantianNomor').toString();

    Map<String, dynamic> httpBody = {
      'idPenggantianNomor': idPenggantianNomor,
      'kodeVerifikasi': kodeVerifikasiController.text
    };

    String _params = await HTTPRequest.getQueryString(authToken: true);
    Map<String, dynamic> _body = await HTTPRequest.postBody(body: httpBody);

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Auth/verifikasiNomor?' + _params);

    Response httpResponse = await post(_url, body: _body);
    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      bool statusVerifikasiNomor = dataObject['statusVerifikasiNomor'];

      return statusVerifikasiNomor;
    }
    return false;
  }

  Future<int> cekJumlahGantiNomorHPForToday() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Auth/jumlahGantiNomorHPForToday?' + _params);
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      int jumlahGantiNomorHPForToday = dataObject['jumlahGantiNomorHPForToday'];

      return jumlahGantiNomorHPForToday;
    }
    return 0;
  }

  final Color _borderColor = CustomColors.green;
  final double _borderWidth = 1.0;

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    bool isFormCompleted = (idPenggantianNomorState == 0)
        ? (nomorHPController.text.length >= 12)
        : (kodeVerifikasiController.text.length >= 6);

    Map<String, dynamic> profilSaya = widget.profilSaya;

    final EdgeInsets _padding =
        EdgeInsets.symmetric(horizontal: 15.0, vertical: 15);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
            height: 1.0, color: CustomColors.textLightMuted, margin: _padding),
        (idPenggantianNomorState == 0)
            ? Container(
                padding: _padding,
                child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Nomor HP',
                          style: TextStyle(
                              fontSize: SizeConfig.horizontalBlock * 4.0,
                              fontWeight: FontWeight.w700)),
                      SizedBox(height: 5),
                      Text(
                          'Anda dapat mengubah nomor telepon anda dengan mengisi nomor lama',
                          style: CustomText.textXS()),
                      SizedBox(height: 15),
                      Container(
                        padding:
                            EdgeInsets.symmetric(vertical: 5, horizontal: 10),
                        decoration: BoxDecoration(
                          borderRadius: BorderRadius.circular(15),
                          color: CustomColors.textPrimary,
                        ),
                        child: Text(
                            'Nomor telepon anda saat ini : ' +
                                profilSaya['nomorHP'],
                            style: TextStyle(
                                color: CustomColors.nearlyWhite,
                                fontSize: SizeConfig.horizontalBlock * 3)),
                      ),
                      SizedBox(height: 15),
                      FutureBuilder<int>(
                          future: cekJumlahGantiNomorHPForToday(),
                          builder: (context, snapshot) {
                            if (snapshot.hasData) {
                              if (snapshot.data != null) {
                                int jumlahGantiNomorHP = snapshot.data!;
                                if (jumlahGantiNomorHP >= 1) {
                                  return Center(
                                      child: Text(
                                          'Mohon maaf, anda sudah mencapai batas maksimal ganti nomor telepon untuk hari ini. Silahkan ulangi lagi keesokan harinya.',
                                          style: TextStyle(
                                              color: CustomColors.textDanger,
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      3.25)));
                                } else {
                                  return Column(
                                      crossAxisAlignment:
                                          CrossAxisAlignment.start,
                                      children: [
                                        CustomForm.label(context,
                                            label: 'Nomor HP Baru'),
                                        CustomForm.textField(context,
                                            hintText: 'Contoh : 0812XXXXXXXX',
                                            controller: nomorHPController,
                                            borderColor: _borderColor,
                                            borderWidth: _borderWidth),
                                        (isFormCompleted)
                                            ? Padding(
                                                padding:
                                                    EdgeInsets.only(top: 10),
                                                child: CustomButton.button(
                                                    context,
                                                    'Simpan Nomor Baru',
                                                    () async {
                                                  Map<String, dynamic>
                                                      simpanNomorBaruResponse =
                                                      await simpanNomorBaru();

                                                  bool statusSimpanNomorBaru =
                                                      simpanNomorBaruResponse[
                                                          'statusSimpanNomorBaru'];
                                                  int idPenggantianNomor =
                                                      simpanNomorBaruResponse[
                                                          'idPenggantianNomor'];

                                                  if (statusSimpanNomorBaru) {
                                                    SharedPreferences _sP =
                                                        await SharedPreferences
                                                            .getInstance();

                                                    _sP.setInt(
                                                        'idPenggantianNomor',
                                                        idPenggantianNomor);

                                                    setState(() {
                                                      idPenggantianNomorState =
                                                          idPenggantianNomor;
                                                    });
                                                  }
                                                },
                                                    isBlock: true,
                                                    outline: true,
                                                    color: CustomButton
                                                        .themeColor))
                                            : SizedBox(),
                                      ]);
                                }
                              }
                            }
                            return Padding(
                              padding: EdgeInsets.symmetric(vertical: 25),
                              child: Center(
                                  child: SizedBox(
                                      width: 20,
                                      height: 20,
                                      child: CircularProgressIndicator(
                                          strokeWidth: 1.5))),
                            );
                          }),
                      SizedBox(height: 10)
                    ]))
            : Container(
                padding: EdgeInsets.symmetric(horizontal: 15, vertical: 15),
                child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text('Verifikasi Nomor Telepon',
                              style: TextStyle(
                                  fontSize: SizeConfig.horizontalBlock * 4.0,
                                  fontWeight: FontWeight.w700)),
                          (idPenggantianNomorState != 0)
                              ? InkWell(
                                  onTap: () async {
                                    SharedPreferences _sP =
                                        await SharedPreferences.getInstance();

                                    _sP.remove('idPenggantianNomor');
                                    setState(() {
                                      idPenggantianNomorState = 0;
                                    });
                                  },
                                  child: Text('Batal',
                                      style: TextStyle(
                                          color: CustomColors.textDanger,
                                          fontSize:
                                              SizeConfig.horizontalBlock * 3.5,
                                          fontWeight: FontWeight.w700)),
                                )
                              : SizedBox(),
                        ],
                      ),
                      SizedBox(height: 5),
                      Text(
                          'Silahkan masukkan kode verifikasi yang kami kirim ke nomor baru anda, atau jika anda dapat membatalkan proses penggantian nomor baru.',
                          style: TextStyle(
                              fontSize: SizeConfig.horizontalBlock * 3.5)),
                      SizedBox(height: 15),
                      CustomForm.label(context, label: 'Kode Verifikasi'),
                      CustomForm.textField(context,
                          hintText: 'Kode Verifikasi',
                          controller: kodeVerifikasiController,
                          borderColor: _borderColor,
                          borderWidth: _borderWidth),
                      (isFormCompleted)
                          ? Padding(
                              padding: EdgeInsets.only(top: 10),
                              child: CustomButton.button(
                                  context, 'Verifikasi Nomor Baru', () async {
                                bool statusVerifikasiNomorTelepon =
                                    await verifikasi();
                                if (statusVerifikasiNomorTelepon) {
                                  SharedPreferences _sP =
                                      await SharedPreferences.getInstance();

                                  _sP.remove('idPenggantianNomor');
                                  Navigator.pushReplacement(
                                      context,
                                      MaterialPageRoute(
                                          builder: (_) => UserAndDashboard()));
                                } else {
                                  showDialog(
                                      context: context,
                                      builder: (context) => AlertDialog(
                                              title: Text(
                                                  'Verifikasi Nomor Telepon',
                                                  style: TextStyle(
                                                      fontSize: SizeConfig
                                                              .horizontalBlock *
                                                          3.75)),
                                              content: Text(
                                                  'Mohon maaf, verifikasi nomor telepon gagal! Silahkan coba lagi.',
                                                  style: TextStyle(
                                                      fontSize: SizeConfig
                                                              .horizontalBlock *
                                                          3.25)),
                                              actions: [
                                                TextButton(
                                                    onPressed: () {
                                                      Navigator.pop(context);
                                                    },
                                                    child: Text('Coba Lagi'))
                                              ]));
                                }
                              },
                                  color: CustomButton.themeColor,
                                  isPill: true,
                                  isBlock: true),
                            )
                          : SizedBox(),
                      SizedBox(height: 10)
                    ])),
        Container(
            height: 1.0, color: CustomColors.textLightMuted, margin: _padding),
      ],
    );
  }
}
