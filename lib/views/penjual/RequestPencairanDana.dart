import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/Currency.dart';

import 'package:laukpauk/views/pembeli/UserAndDashboard.dart';

class RequestPencairanDana extends StatefulWidget {
  _RequestPencairanDanaState createState() => _RequestPencairanDanaState();
}

class _RequestPencairanDanaState extends State<RequestPencairanDana> {
  TextEditingController nomorRekeningController = TextEditingController();
  TextEditingController namaPenerimaController = TextEditingController();
  TextEditingController nominalController = TextEditingController();
  TextEditingController keteranganController = TextEditingController();

  bool isFormCompleted = false;
  int saldoSaatIni = 0;

  getDetailCurrentSeller() async {
    String getPenjualEntities = 'nomorRekening, namaUser';
    String _params = await HTTPRequest.getQueryString(
        params: 'getPenjualEntities=$getPenjualEntities');
    String idPenjual = await LocalStorage.getCurrentUserID();

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Penjual/getDetailPenjual/$idPenjual?' +
        _params);

    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      Map detailCurrentSeller = dataObject['detailPenjual'];

      nomorRekeningController.text = detailCurrentSeller['nomorRekening'];
      namaPenerimaController.text = detailCurrentSeller['namaUser'];
    }
  }

  ajukanRequest() async {
    Map<String, dynamic> dataRequest = {
      'nomorRekening': nomorRekeningController.text,
      'namaPenerima': namaPenerimaController.text,
      'nominal': nominalController.text,
      'keterangan': keteranganController.text
    };

    String _params = await HTTPRequest.getQueryString(authToken: true);
    Map<String, dynamic> _body = await HTTPRequest.postBody(body: dataRequest);

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Penjual/ajukanRPD?' + _params);

    Response httpResponse = await post(_url, body: _body);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      bool statusRequest = dataObject['statusRequest'];

      String text = 'Pengajuan pencairan dana gagal dilakukan !';
      List<Widget> actionButtons = [
        TextButton(
            onPressed: () {
              Navigator.pop(context);
            },
            child: Text('OK'))
      ];
      if (statusRequest) {
        text =
            'Pengajuan request berhasil dilakukan ! Tunggu hingga admin mentransfer dana yang diperlukan ke rekening yang anda masukkan.';
        actionButtons = [
          TextButton(
              onPressed: () {
                Navigator.pop(context);
                Navigator.push(context,
                    MaterialPageRoute(builder: (_) => UserAndDashboard()));
              },
              child: Text('OK'))
        ];
      }

      showDialog(
          context: context,
          builder: (context) => AlertDialog(
              title: Text('Pengajuan Pencairan Dana',
                  style: TextStyle(fontSize: SizeConfig.horizontalBlock * 4)),
              content: Text(text,
                  style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.5)),
              actions: actionButtons));
    }
  }

  Future<int> getTotalSaldo() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Penjual/getTotalSaldo?' + _params);
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      int totalSaldo = dataObject['totalSaldo'];

      return totalSaldo;
    }
    return 0;
  }

  void setInitialState() async {
    int totalSaldo = await getTotalSaldo();
    setState(() {
      saldoSaatIni = totalSaldo;
    });
  }

  void initState() {
    super.initState();
    getDetailCurrentSeller();
    setInitialState();
  }

  void dispose() {
    super.dispose();
    nomorRekeningController.dispose();
    namaPenerimaController.dispose();
    nominalController.dispose();
    keteranganController.dispose();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    int intNominal = (nominalController.text.length >= 1)
        ? int.parse(nominalController.text)
        : 0;

    bool nominalIsLessThanSaldo = intNominal <= saldoSaatIni;
    isFormCompleted = (nomorRekeningController.text.length >= 1 &&
        namaPenerimaController.text.length >= 1 &&
        nominalController.text.length >= 1 &&
        nominalIsLessThanSaldo &&
        intNominal >= 1);

    return Scaffold(
        appBar: AppBar(
            title: Text('Request Pencairan Dana'),
            backgroundColor: CustomColors.green),
        backgroundColor: CustomColors.aliceBlue,
        body: SingleChildScrollView(
            child: Padding(
                padding: EdgeInsets.symmetric(horizontal: 5, vertical: 5),
                child: Card(
                    elevation: 5,
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10)),
                    child: Padding(
                        padding: EdgeInsets.only(
                            top: 20, bottom: 20, left: 15, right: 15),
                        child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text('Request Pencairan Dana',
                                  style: TextStyle(
                                      fontSize: SizeConfig.horizontalBlock * 4,
                                      fontWeight: FontWeight.w700)),
                              SizedBox(height: 3),
                              Text(
                                  'Lakukan request pencairan dana untuk mendapatkan uang hasil berdagang anda',
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.25)),
                              SizedBox(height: 10),
                              Text(
                                  'Saldo anda saat ini ' +
                                      Currency.formatRupiah(saldoSaatIni, 0),
                                  style: TextStyle(
                                      fontWeight: FontWeight.w700,
                                      color: CustomColors.green,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 4)),
                              Text(
                                  'Request dengan nominal 0 Rupiah tidak akan dapat diproses',
                                  style: TextStyle(
                                      color: CustomColors.textMuted,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3)),
                              SizedBox(height: 20),
                              Container(
                                padding: EdgeInsets.symmetric(horizontal: 30),
                                decoration: BoxDecoration(
                                    color: CustomColors.lightGreen,
                                    borderRadius: BorderRadius.circular(50)),
                                child: TextField(
                                  style: TextStyle(
                                      color: CustomColors.darkText,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5),
                                  controller: nomorRekeningController,
                                  keyboardType: TextInputType.text,
                                  cursorColor: CustomColors.darkText,
                                  textAlignVertical: TextAlignVertical.center,
                                  textAlign: TextAlign.left,
                                  decoration: InputDecoration(
                                      isDense: true,
                                      contentPadding: EdgeInsets.only(
                                          top: SizeConfig.verticalBlock * 2.5,
                                          bottom:
                                              SizeConfig.verticalBlock * 2.5),
                                      border: InputBorder.none,
                                      hintText: 'Nomor Rekening',
                                      hintStyle: TextStyle(
                                          color: CustomColors.darkText)),
                                ),
                              ),
                              SizedBox(height: 10),
                              Container(
                                padding: EdgeInsets.symmetric(horizontal: 30),
                                decoration: BoxDecoration(
                                    color: CustomColors.lightGreen,
                                    borderRadius: BorderRadius.circular(50)),
                                child: TextField(
                                  style: TextStyle(
                                      color: CustomColors.darkText,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5),
                                  controller: namaPenerimaController,
                                  keyboardType: TextInputType.text,
                                  cursorColor: CustomColors.darkText,
                                  textAlignVertical: TextAlignVertical.center,
                                  textAlign: TextAlign.left,
                                  decoration: InputDecoration(
                                      isDense: true,
                                      contentPadding: EdgeInsets.only(
                                          top: SizeConfig.verticalBlock * 2.5,
                                          bottom:
                                              SizeConfig.verticalBlock * 2.5),
                                      border: InputBorder.none,
                                      hintText: 'Nama Penerima',
                                      hintStyle: TextStyle(
                                          color: CustomColors.darkText)),
                                ),
                              ),
                              SizedBox(height: 10),
                              Container(
                                padding: EdgeInsets.symmetric(horizontal: 30),
                                decoration: BoxDecoration(
                                    color: CustomColors.lightGreen,
                                    borderRadius: BorderRadius.circular(50)),
                                child: TextField(
                                  style: TextStyle(
                                      color: CustomColors.darkText,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5),
                                  controller: nominalController,
                                  keyboardType: TextInputType.number,
                                  cursorColor: CustomColors.darkText,
                                  textAlignVertical: TextAlignVertical.center,
                                  textAlign: TextAlign.left,
                                  decoration: InputDecoration(
                                      isDense: true,
                                      contentPadding: EdgeInsets.only(
                                          top: SizeConfig.verticalBlock * 2.5,
                                          bottom:
                                              SizeConfig.verticalBlock * 2.5),
                                      border: InputBorder.none,
                                      hintText: 'Nominal (Rupiah)',
                                      hintStyle: TextStyle(
                                          color: CustomColors.darkText)),
                                ),
                              ),
                              (!nominalIsLessThanSaldo)
                                  ? Padding(
                                      padding:
                                          EdgeInsets.only(top: 5, left: 15),
                                      child: Text('Saldo anda tidak mencukupi',
                                          style: TextStyle(
                                              color: CustomColors.textDanger,
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      3)),
                                    )
                                  : SizedBox(),
                              SizedBox(height: 10),
                              Container(
                                padding: EdgeInsets.symmetric(horizontal: 30),
                                decoration: BoxDecoration(
                                    color: CustomColors.lightGreen,
                                    borderRadius: BorderRadius.circular(50)),
                                child: TextField(
                                  style: TextStyle(
                                      color: CustomColors.darkText,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5),
                                  controller: keteranganController,
                                  keyboardType: TextInputType.text,
                                  cursorColor: CustomColors.darkText,
                                  textAlignVertical: TextAlignVertical.center,
                                  textAlign: TextAlign.left,
                                  decoration: InputDecoration(
                                      isDense: true,
                                      contentPadding: EdgeInsets.only(
                                          top: SizeConfig.verticalBlock * 2.5,
                                          bottom:
                                              SizeConfig.verticalBlock * 2.5),
                                      border: InputBorder.none,
                                      hintText:
                                          'Keterangan Tambahan (Opsional)',
                                      hintStyle: TextStyle(
                                          color: CustomColors.darkText)),
                                ),
                              ),
                              SizedBox(height: 20),
                              (isFormCompleted)
                                  ? CustomButton.button(
                                      context, 'Ajukan Request', () {
                                      ajukanRequest();
                                    },
                                      color: CustomButton.themeColor,
                                      outline: true,
                                      isBlock: true,
                                      isPill: true)
                                  : SizedBox()
                            ]))))));
  }
}
