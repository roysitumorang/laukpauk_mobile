import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/penjual/RequestPencairanDana.dart';
import 'package:laukpauk/views/penjual/HistoriPenjualan.dart';

class PenjualanDanTransferDana extends StatelessWidget {
  Future<int> getTotalPenjualan() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Penjual/getTotalPenjualan?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      int totalPenjualan = dataObject['totalPenjualan'];

      return totalPenjualan;
    }
    return 0;
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Container(
      padding: EdgeInsets.symmetric(horizontal: 25, vertical: 10),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          InkWell(
            onTap: () {
              Navigator.push(context,
                  MaterialPageRoute(builder: (_) => HistoriPenjualan()));
            },
            child: Row(children: [
              Image.asset(ClientPath.assetsImg + '/icons/penjualan.png',
                  width: SizeConfig.horizontalBlock * 10,
                  height: SizeConfig.horizontalBlock * 10),
              SizedBox(width: 10),
              Column(children: [
                Text('Penjualan',
                    style: TextStyle(
                        fontSize: SizeConfig.horizontalBlock * 3.5,
                        fontWeight: FontWeight.w700)),
                FutureBuilder<int>(
                    future: getTotalPenjualan(),
                    builder: (context, snapshot) {
                      if (snapshot.hasData) {
                        if (snapshot.data != null) {
                          int totalPenjualan = snapshot.data!;
                          return Container(
                            padding: EdgeInsets.only(top: 1.5),
                            child: Text(
                                Currency.formatRupiah(totalPenjualan, 0),
                                style: TextStyle(
                                    fontSize:
                                        SizeConfig.horizontalBlock * 2.75)),
                          );
                        }
                      }
                      return Center(
                          child: SizedBox(
                              height: 10,
                              width: 10,
                              child:
                                  CircularProgressIndicator(strokeWidth: 1.5)));
                    })
              ])
            ]),
          ),
          Container(width: 2, height: 35, color: CustomColors.green),
          InkWell(
            onTap: () {
              Navigator.push(context,
                  MaterialPageRoute(builder: (_) => RequestPencairanDana()));
            },
            child: Row(children: [
              Image.asset(ClientPath.assetsImg + '/icons/transfer-dana.png',
                  width: SizeConfig.horizontalBlock * 10,
                  height: SizeConfig.horizontalBlock * 10),
              SizedBox(width: 10),
              Column(children: [
                Text('Transfer Dana',
                    style: TextStyle(
                        fontSize: SizeConfig.horizontalBlock * 3.5,
                        fontWeight: FontWeight.w700))
              ])
            ]),
          )
        ],
      ),
      decoration: BoxDecoration(
          border: Border.all(color: CustomColors.green, width: 1),
          borderRadius: BorderRadius.circular(10)),
    );
  }
}
