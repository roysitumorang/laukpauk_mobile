import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

class UbahNamaToko extends StatefulWidget {
  final Function(bool? statusUbahNamaToko) ubahNamaTokoCallback;
  UbahNamaToko(this.ubahNamaTokoCallback);
  _UbahNamaTokoState createState() => _UbahNamaTokoState();
}

class _UbahNamaTokoState extends State<UbahNamaToko> {
  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Card(
        elevation: 5,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        child: Padding(
          padding: EdgeInsets.symmetric(horizontal: 15, vertical: 10),
          child:
              Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
            Text('Ubah Nama Toko',
                style: TextStyle(
                    fontSize: SizeConfig.horizontalBlock * 4,
                    fontWeight: FontWeight.w700)),
            InkWell(
                onTap: () async {
                  bool? statusUbahNamaToko = await Navigator.push(context,
                      MaterialPageRoute(builder: (_) => UbahNamaTokoForm()));
                  widget.ubahNamaTokoCallback(statusUbahNamaToko);
                },
                child: Icon(FontAwesomeIcons.arrowAltCircleRight,
                    size: SizeConfig.horizontalBlock * 5))
          ]),
        ));
  }
}

class UbahNamaTokoForm extends StatefulWidget {
  _UbahNamaTokoFormState createState() => _UbahNamaTokoFormState();
}

class _UbahNamaTokoFormState extends State<UbahNamaTokoForm> {
  TextEditingController namaTokoController = TextEditingController();

  Future<bool> ubahNamaToko() async {
    String namaToko = namaTokoController.text;
    Map<String, dynamic> body = {'namaToko': namaToko};

    String _params = await HTTPRequest.getQueryString(authToken: true);
    Map<String, dynamic> _body = await HTTPRequest.postBody(body: body);

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Penjual/ubahNamaToko?' + _params);

    Response httpResponse = await post(_url, body: _body);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      bool statusUbahNamaToko = dataObject['statusUbahNamaToko'];

      return statusUbahNamaToko;
    }
    return false;
  }

  void dispose() {
    super.dispose();
    namaTokoController.dispose();
  }

  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
          title: Text('Ubah Nama Toko'), backgroundColor: CustomColors.green),
      body: SingleChildScrollView(
        child: Padding(
          padding: EdgeInsets.symmetric(horizontal: 5, vertical: 5),
          child: Card(
              elevation: 5,
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10)),
              child: Padding(
                  padding: EdgeInsets.symmetric(horizontal: 15, vertical: 15),
                  child: Container(
                    child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Ubah Nama Toko',
                              style: TextStyle(
                                  fontSize: SizeConfig.horizontalBlock * 4,
                                  fontWeight: FontWeight.w700)),
                          SizedBox(height: 10),
                          Container(
                            padding: EdgeInsets.symmetric(horizontal: 30),
                            decoration: BoxDecoration(
                                color: CustomColors.lightGreen,
                                borderRadius: BorderRadius.circular(50)),
                            child: TextField(
                              style: TextStyle(
                                  color: CustomColors.darkText,
                                  fontSize: SizeConfig.horizontalBlock * 3.5),
                              controller: namaTokoController,
                              keyboardType: TextInputType.text,
                              cursorColor: CustomColors.darkText,
                              textAlignVertical: TextAlignVertical.center,
                              textAlign: TextAlign.left,
                              decoration: InputDecoration(
                                  isDense: true,
                                  contentPadding: EdgeInsets.only(
                                      top: SizeConfig.verticalBlock * 2.25,
                                      bottom: SizeConfig.verticalBlock * 2.25),
                                  border: InputBorder.none,
                                  hintText: 'Nama Toko Baru',
                                  hintStyle:
                                      TextStyle(color: CustomColors.darkText)),
                            ),
                          ),
                          (namaTokoController.text.length >= 1)
                              ? SizedBox(height: 10)
                              : SizedBox(),
                          (namaTokoController.text.length >= 1)
                              ? CustomButton.button(context, 'Ubah Nama Toko',
                                  () async {
                                  bool statusUbahNamaToko =
                                      await ubahNamaToko();
                                  Navigator.pop(context, statusUbahNamaToko);
                                },
                                  color: CustomButton.themeColor, isBlock: true)
                              : SizedBox()
                        ]),
                  ))),
        ),
      ),
    );
  }
}
