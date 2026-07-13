import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/DetailProduk.dart';

class ProdukPenjualLainnya extends StatelessWidget {
  final String currentProduct;
  ProdukPenjualLainnya({required this.currentProduct});

  Future<List> getProdukLainnyaFromThisPenjual() async {
    String idProduk = currentProduct;
    String _params =
        await HTTPRequest.getQueryString(params: 'idProduk=$idProduk');

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Produk/getProdukLainnya?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      dynamic dataObject = jsonDecode(httpResponse.body);
      return dataObject['produkLainnya'];
    }

    return [];
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return FutureBuilder<List>(
        future: getProdukLainnyaFromThisPenjual(),
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
                        return Row(
                          children: [
                            Container(
                              width: SizeConfig.horizontalBlock * 35,
                              child: InkWell(
                                onTap: () {
                                  Navigator.pushReplacement(
                                      context,
                                      MaterialPageRoute(
                                          builder: (_) => DetailProduk(
                                              produkLainnya[index]['id'])));
                                },
                                child: Column(
                                  children: [
                                    Container(
                                      width: SizeConfig.horizontalBlock * 35,
                                      height: SizeConfig.horizontalBlock * 22,
                                      decoration: BoxDecoration(
                                          color: CustomColors.nearlyWhite,
                                          borderRadius:
                                              BorderRadius.circular(10),
                                          image: DecorationImage(
                                              fit: BoxFit.cover,
                                              image: NetworkImage(
                                                  ServerPath.baseURL +
                                                      '/' +
                                                      ServerPath
                                                          .uploadGambarProduk +
                                                      '/compress/' +
                                                      produkLainnya[index]
                                                          ['foto']))),
                                    ),
                                    SizedBox(height: 7.5),
                                    SingleChildScrollView(
                                      scrollDirection: Axis.horizontal,
                                      child: Text(produkLainnya[index]['nama'],
                                          style: TextStyle(
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      2.75)),
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
                Text('Maaf mak, tidak ada produk lainnya…',
                    style: TextStyle(
                        color: CustomColors.textMuted,
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
