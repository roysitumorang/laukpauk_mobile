import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/views/pembeli/DetailPaket.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class PaketPenjualLainnya extends StatelessWidget {
  final String currentProduct;
  PaketPenjualLainnya({required this.currentProduct});

  Future<List> getPaketLainnyaFromThisPenjual() async {
    String idPaket = currentProduct;
    String _params =
        await HTTPRequest.getQueryString(params: 'idPaket=$idPaket');

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Produk/getPaketLainnya?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      dynamic dataObject = jsonDecode(httpResponse.body);
      return dataObject['paketLainnya'];
    }

    return [];
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return FutureBuilder<List>(
        future: getPaketLainnyaFromThisPenjual(),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            if (snapshot.data != null) {
              List produkLainnya = snapshot.data!;
              if (produkLainnya.length >= 1) {
                return Container(
                  height: SizeConfig.horizontalBlock * 29,
                  child: ListView.builder(
                      scrollDirection: Axis.horizontal,
                      itemCount: produkLainnya.length,
                      itemBuilder: (context, index) {
                        Map<String, dynamic> paketLainnyaItem =
                            produkLainnya[index];
                        return Row(
                          children: [
                            Container(
                              width: SizeConfig.horizontalBlock * 35,
                              child: InkWell(
                                onTap: () {
                                  Navigator.pushReplacement(
                                      context,
                                      MaterialPageRoute(
                                          builder: (_) => DetailPaket(
                                              paketLainnyaItem['id'])));
                                },
                                child: Column(
                                  children: [
                                    Container(
                                      width: SizeConfig.horizontalBlock * 35,
                                      height: SizeConfig.horizontalBlock * 22,
                                      decoration: BoxDecoration(
                                          color: CustomColors.nearlyWhite,
                                          border: Border.all(
                                              width: 0.5,
                                              color: CustomColors.green),
                                          borderRadius:
                                              BorderRadius.circular(10),
                                          image: DecorationImage(
                                              fit: BoxFit.cover,
                                              image: NetworkImage(ServerPath
                                                      .baseURL +
                                                  '/' +
                                                  ServerPath.uploadGambarPaket +
                                                  '/compress/' +
                                                  paketLainnyaItem['foto']))),
                                    ),
                                    SizedBox(height: 10),
                                    SingleChildScrollView(
                                      scrollDirection: Axis.horizontal,
                                      child: Text(paketLainnyaItem['nama'],
                                          style: TextStyle(
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      3.25)),
                                    )
                                  ],
                                ),
                              ),
                            ),
                            SizedBox(width: 5)
                          ],
                        );
                      }),
                );
              }
            }
            return Container(
              width: SizeConfig.horizontalBlock * 100,
              padding: EdgeInsets.symmetric(vertical: 15),
              child: Column(children: [
                Image.asset(ClientPath.assetsImg + '/empty.png',
                    width: SizeConfig.horizontalBlock * 20.0),
                SizedBox(height: 5),
                Text('Maaf mak, tidak ada paket lainnya…',
                    style: TextStyle(
                        color: CustomColors.textDanger,
                        fontSize: SizeConfig.horizontalBlock * 3.25))
              ]),
            );
          } else {
            return Center(
                child: SizedBox(
                    width: 15,
                    height: 15,
                    child: CircularProgressIndicator(strokeWidth: 1.5)));
          }
        });
  }
}
