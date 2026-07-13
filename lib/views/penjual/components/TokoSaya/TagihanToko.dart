import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';

import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class TagihanToko extends StatefulWidget {
  _TagihanTokoState createState() => _TagihanTokoState();
}

class _TagihanTokoState extends State<TagihanToko> {
  Future<int> getTotalTagihan() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/TagihanPenjual/getTagihan?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      int totalTagihan = dataObject['tagihan'];

      return totalTagihan;
    }
    return 0;
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Card(
        elevation: 5,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        child: Padding(
          padding: EdgeInsets.symmetric(horizontal: 15, vertical: 15.0),
          child:
              Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
            Text('Tagihan Toko',
                style: TextStyle(
                    fontSize: SizeConfig.horizontalBlock * 4,
                    fontWeight: FontWeight.w700)),
            Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                FutureBuilder<int>(
                    future: getTotalTagihan(),
                    builder: (context, snapshot) {
                      if (snapshot.hasData) {
                        if (snapshot.data != null) {
                          int totalTagihan = snapshot.data!;
                          return Text(Currency.formatRupiah(totalTagihan, 0),
                              style: TextStyle(
                                  fontSize: SizeConfig.horizontalBlock * 4,
                                  fontWeight: FontWeight.w700,
                                  color: CustomColors.green));
                        }
                      }
                      return Center(
                          child: SizedBox(
                              width: 5,
                              height: 5,
                              child:
                                  CircularProgressIndicator(strokeWidth: 1.5)));
                    })
              ],
            )
          ]),
        ));
  }
}
