import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/ServerPath.dart';

import 'package:laukpauk/views/pembeli/UserAndDashboard.dart';
import 'package:laukpauk/views/pembeli/components/OTPVerifikasiUser/ListPasar.dart';
import 'package:laukpauk/views/pembeli/PinGMAPS.dart';

class RequestJadiPenjual extends StatefulWidget {
  _RequestJadiPenjualState createState() => _RequestJadiPenjualState();
}

class _RequestJadiPenjualState extends State<RequestJadiPenjual> {
  TextEditingController nomorRekeningController = TextEditingController();
  TextEditingController namaTokoController = TextEditingController();
  TextEditingController alamatDetailTokoController = TextEditingController();
  TextEditingController bioPenjualController = TextEditingController();

  String lokasiBerjualan = 'pasarTersedia';
  String pasarSelected = '0';
  Map<String, dynamic>? lokasiTersendiriState;

  bool _isButtonAjukanBusy = false;

  ajukanRequest() async {
    setState(() {
      _isButtonAjukanBusy = true;
    });

    Map<String, dynamic> dataRequest = {
      'namaToko': namaTokoController.text,
      'alamatToko': alamatDetailTokoController.text,
      'bio': bioPenjualController.text,
      'lokasiBerjualan': lokasiBerjualan,
      'pasar': pasarSelected,
      'nomorRekening': nomorRekeningController.text
    };

    if (lokasiTersendiriState != null) {
      if (lokasiBerjualan.toLowerCase() == 'lokasitersendiri') {
        dataRequest['lat'] = lokasiTersendiriState!['lat'];
        dataRequest['long'] = lokasiTersendiriState!['lng'];
      }
    }

    String _params = await HTTPRequest.getQueryString(authToken: true);
    Map<String, dynamic> _body = await HTTPRequest.postBody(body: dataRequest);

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Penjual/ajukanRequestPenjual?' + _params);
    Response httpResponse = await post(_url, body: _body);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      bool statusRequest = dataObject['statusRequest'];

      String text = 'Pengajuan request gagal dilakukan !';
      List<Widget> actionButtons = [
        TextButton(
            onPressed: () {
              Navigator.pop(context);
            },
            child: Text('OK'))
      ];
      if (statusRequest) {
        text =
            'Pengajuan request berhasil dilakukan ! Tunggu hingga admin menyetujui request anda, dan anda dapat langsung berjualan.';
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

      setState(() {
        _isButtonAjukanBusy = false;
      });

      showDialog(
          context: context,
          builder: (context) => AlertDialog(
              title: Text('Pengajuan Request',
                  style: TextStyle(fontSize: SizeConfig.horizontalBlock * 4)),
              content: Text(text,
                  style:
                      TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25)),
              actions: actionButtons));
    }
  }

  void dispose() {
    super.dispose();
    nomorRekeningController.dispose();
    namaTokoController.dispose();
    alamatDetailTokoController.dispose();
    bioPenjualController.dispose();
  }

  lokasiBerjualanChanged(String lokasiBerjualanSelected,
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

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    bool isFormCompleted = (nomorRekeningController.text.length >= 1 &&
        namaTokoController.text.length >= 1 &&
        alamatDetailTokoController.text.length >= 1 &&
        bioPenjualController.text.length >= 1);

    double topBotPadding = SizeConfig.horizontalBlock * 3.0;

    return Scaffold(
        appBar: AppBar(
            title: Text('Request Menjadi Penjual'),
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
                              Text('Request Menjadi Penjual',
                                  style: TextStyle(
                                      fontSize: SizeConfig.horizontalBlock * 4,
                                      fontWeight: FontWeight.w700)),
                              SizedBox(height: 3),
                              Text(
                                  'Anda dapat bergabung bersama kami menjadi penjual dan dapatkan beragam keuntungan. Silahkan lengkapi data anda dengan form di bawah ini.',
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.25)),
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
                                    controller: namaTokoController,
                                    keyboardType: TextInputType.text,
                                    cursorColor: CustomColors.darkText,
                                    textAlignVertical: TextAlignVertical.center,
                                    textAlign: TextAlign.left,
                                    decoration: InputDecoration(
                                        isDense: true,
                                        contentPadding: EdgeInsets.only(
                                            top: topBotPadding,
                                            bottom: topBotPadding),
                                        labelText: 'Nama Toko',
                                        hintText: 'Misal : Toko Jaya Sejahtera',
                                        border: InputBorder.none,
                                        hintStyle: TextStyle(
                                            color: CustomColors.darkText)),
                                  )),
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
                                  controller: alamatDetailTokoController,
                                  keyboardType: TextInputType.text,
                                  cursorColor: CustomColors.darkText,
                                  textAlignVertical: TextAlignVertical.center,
                                  textAlign: TextAlign.left,
                                  decoration: InputDecoration(
                                      isDense: true,
                                      contentPadding: EdgeInsets.only(
                                          top: topBotPadding,
                                          bottom: topBotPadding),
                                      labelText: 'Alamat Detail Toko',
                                      border: InputBorder.none,
                                      hintText: 'Jl. Asrama No. 23 AB Medan',
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
                                  controller: bioPenjualController,
                                  keyboardType: TextInputType.text,
                                  cursorColor: CustomColors.darkText,
                                  textAlignVertical: TextAlignVertical.center,
                                  textAlign: TextAlign.left,
                                  decoration: InputDecoration(
                                      isDense: true,
                                      contentPadding: EdgeInsets.only(
                                          top: topBotPadding,
                                          bottom: topBotPadding),
                                      labelText: 'Bio Penjual',
                                      border: InputBorder.none,
                                      hintText:
                                          'Penjelasan tentang toko atau produk yang anda jual',
                                      hintStyle: TextStyle(
                                          color: CustomColors.darkText)),
                                ),
                              ),
                              SizedBox(height: 10),
                              Container(
                                  padding: EdgeInsets.symmetric(
                                      horizontal: 30, vertical: topBotPadding),
                                  decoration: BoxDecoration(
                                      color: CustomColors.lightGreen,
                                      borderRadius: BorderRadius.circular(50)),
                                  child: DropdownButton<String>(
                                      underline: DropdownButtonHideUnderline(
                                          child: SizedBox()),
                                      isDense: true,
                                      isExpanded: true,
                                      items: <DropdownMenuItem<String>>[
                                        DropdownMenuItem(
                                            value: null,
                                            child: Text('Lokasi Berjualan',
                                                style: TextStyle(
                                                    fontSize: SizeConfig
                                                            .horizontalBlock *
                                                        3.5))),
                                        DropdownMenuItem(
                                            value: 'pasarTersedia',
                                            child: Text(
                                                'Pasar yang sudah tersedia',
                                                style: TextStyle(
                                                    fontSize: SizeConfig
                                                            .horizontalBlock *
                                                        3.5))),
                                        DropdownMenuItem(
                                            value: 'lokasiTersendiri',
                                            child: Text('Lokasi Tersendiri',
                                                textAlign: TextAlign.center,
                                                style: TextStyle(
                                                    fontSize: SizeConfig
                                                            .horizontalBlock *
                                                        3.5)))
                                      ],
                                      value: lokasiBerjualan,
                                      onChanged: (lokasiBerjualanSelected) {
                                        if (lokasiBerjualanSelected != null) {
                                          lokasiBerjualanChanged(
                                              lokasiBerjualanSelected);
                                        }
                                      })),
                              SizedBox(height: 10),
                              (lokasiBerjualan == 'lokasiTersendiri')
                                  ? Padding(
                                      padding:
                                          EdgeInsets.symmetric(horizontal: 10),
                                      child: Column(
                                        crossAxisAlignment:
                                            CrossAxisAlignment.start,
                                        children: [
                                          Text(
                                              'Lokasi pasar anda akan sesuai dengan lokasi yang anda pilih :',
                                              style: TextStyle(
                                                  fontSize: SizeConfig
                                                          .horizontalBlock *
                                                      3.5)),
                                          SizedBox(height: 5),
                                          Text(
                                              'Latitude : ' +
                                                  lokasiTersendiriState![
                                                      'lat'] +
                                                  ', Longitude : ' +
                                                  lokasiTersendiriState!['lng'],
                                              style: TextStyle(
                                                  fontSize: SizeConfig
                                                          .horizontalBlock *
                                                      2.5)),
                                          SizedBox(height: 10),
                                          Center(
                                            child: InkWell(
                                              onTap: () {
                                                lokasiBerjualanChanged(
                                                    'lokasiTersendiri',
                                                    pilihLokasiLain: true);
                                              },
                                              child: Text('Pilih Lokasi Lain',
                                                  style: TextStyle(
                                                      color: CustomColors.green,
                                                      fontSize: SizeConfig
                                                              .horizontalBlock *
                                                          3.5)),
                                            ),
                                          ),
                                          SizedBox(height: 10)
                                        ],
                                      ),
                                    )
                                  : ListPasar(
                                      pasarSelected: pasarSelected,
                                      onPasarClicked: (idPasar) {
                                        setState(() {
                                          pasarSelected = idPasar;
                                        });
                                      }),
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
                                  controller: nomorRekeningController,
                                  keyboardType: TextInputType.text,
                                  cursorColor: CustomColors.darkText,
                                  textAlignVertical: TextAlignVertical.center,
                                  textAlign: TextAlign.left,
                                  decoration: InputDecoration(
                                      contentPadding: EdgeInsets.only(
                                          bottom: topBotPadding,
                                          top: topBotPadding),
                                      border: InputBorder.none,
                                      hintText: 'Nomor Rekening',
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
                                      isBlock: true,
                                      isBusy: _isButtonAjukanBusy,
                                      outline: true,
                                      color: CustomButton.themeColor,
                                      isPill: true)
                                  : SizedBox()
                            ]))))));
  }
}
