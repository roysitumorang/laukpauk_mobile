import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';

import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class PesananItemTidakTersedia extends StatelessWidget {
  final Map<String, dynamic> pesananItem;
  PesananItemTidakTersedia(this.pesananItem);

  final TextEditingController keteranganController = TextEditingController();

  Future<bool> klaim() async {
    Map<String, dynamic> httpBody = {
      'idItem': pesananItem['id'],
      'keterangan': keteranganController.text
    };

    String _params = await HTTPRequest.getQueryString(authToken: true);
    Map<String, dynamic> _body = await HTTPRequest.postBody(body: httpBody);

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Transaksi/klaimProdukTidakTersedia?' +
        _params);
    Response httpResponse = await post(_url, body: _body);
    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      return dataObject['statusKlaim'];
    }
    return false;
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Scaffold(
        appBar: AppBar(
            title: Text('Pesanan Tidak Tersedia'),
            backgroundColor: CustomColors.green),
        body: Padding(
          padding: EdgeInsets.symmetric(horizontal: 5, vertical: 5),
          child: Container(
            child: Padding(
              padding: EdgeInsets.symmetric(horizontal: 10, vertical: 10),
              child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Beri Alasan',
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 4,
                            fontWeight: FontWeight.w600)),
                    SizedBox(height: 2.5),
                    Text(
                        'Silahkan berikan alasan mengapa produk ini tidak tersedia saat ini'),
                    SizedBox(height: 15),
                    Container(
                      margin: EdgeInsets.only(bottom: 7.5),
                      child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Container(
                                height: SizeConfig.horizontalBlock * 25,
                                padding: EdgeInsets.only(right: 15, left: 25),
                                decoration: BoxDecoration(
                                    borderRadius: BorderRadius.circular(10),
                                    color: Color(0xFFF0F8FF)),
                                child: TextField(
                                    maxLines: null,
                                    minLines: 5,
                                    controller: keteranganController,
                                    keyboardType: TextInputType.text,
                                    style: TextStyle(
                                        fontSize:
                                            SizeConfig.horizontalBlock * 3.5),
                                    decoration: InputDecoration(
                                        border: InputBorder.none,
                                        labelText: 'Keterangan',
                                        contentPadding: EdgeInsets.only(
                                            top: SizeConfig.horizontalBlock *
                                                3.25,
                                            bottom: SizeConfig.horizontalBlock *
                                                3.25),
                                        labelStyle: TextStyle(
                                            fontFamily: 'WorkSans',
                                            fontWeight: FontWeight.w500,
                                            color: Colors.grey)))),
                            (keteranganController.text.length >= 1)
                                ? Padding(
                                    padding: EdgeInsets.only(top: 10.0),
                                    child: CustomButton.button(
                                        context, 'Klaim Pesanan Tidak Tersedia',
                                        () async {
                                      bool klaimProdukTidakTersedia =
                                          await klaim();
                                      Navigator.pop(
                                          context, klaimProdukTidakTersedia);
                                    },
                                        color: CustomButton.themeColor,
                                        isPill: true,
                                        isBlock: true),
                                  )
                                : SizedBox()
                          ]),
                    )
                  ]),
            ),
          ),
        ));
  }
}
