import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class ResepLangkah extends StatelessWidget {
  final String idResep;
  ResepLangkah(this.idResep);

  Future<List> getResepLangkah() async {
    String entitasResepLangkah = 'id, langkah';
    String _params = await HTTPRequest.getQueryString(
        params: 'entitasResepLangkah=$entitasResepLangkah');

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Resep/getResepLangkahFromThisResep/$idResep?' +
        _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      dynamic dataObject = jsonDecode(httpResponse.body);
      return dataObject['dataResepLangkah'];
    }

    return [];
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
      elevation: 10,
      child: Padding(
        padding: EdgeInsets.symmetric(horizontal: 20, vertical: 20),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text('Langkah - langkah : ',
              style: TextStyle(
                  fontSize: SizeConfig.horizontalBlock * 4,
                  fontWeight: FontWeight.w700)),
          SizedBox(height: 10),
          FutureBuilder<List>(
              future: getResepLangkah(),
              builder: (context, snapshot) {
                if (snapshot.hasData) {
                  if (snapshot.data != null) {
                    List resepLangkah = snapshot.data!;

                    if (resepLangkah.length >= 1) {
                      int indexData = 0;
                      List<Widget> resepLangkahWidget =
                          resepLangkah.map((langkah) {
                        indexData++;
                        return Padding(
                          padding: EdgeInsets.only(bottom: 10),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.start,
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text((indexData).toString() + '.',
                                  style:
                                      TextStyle(fontWeight: FontWeight.w700)),
                              SizedBox(width: 15),
                              Expanded(
                                  child: Text(langkah['langkah'],
                                      style: TextStyle(
                                          fontSize: SizeConfig.horizontalBlock *
                                              3.25)))
                            ],
                          ),
                        );
                      }).toList();

                      return Container(
                          child: Column(children: resepLangkahWidget));
                    } else {
                      return Container(
                        width: double.infinity,
                        child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Image.asset(ClientPath.assetsImg + '/empty.png',
                                  width: SizeConfig.horizontalBlock * 30),
                              SizedBox(height: 5),
                              Text('Langkah - langkah tidak ada',
                                  style: TextStyle(
                                      color: CustomColors.textDanger,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5))
                            ]),
                      );
                    }
                  }
                }
                return Center(child: CircularProgressIndicator());
              }),
        ]),
      ),
    );
  }
}
