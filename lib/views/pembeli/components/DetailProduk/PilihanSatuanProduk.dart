import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ClientPath.dart';

import 'package:laukpauk/views/pembeli/components/DetailProduk/PilihanSatuanProdukItem.dart';

class PilihanSatuanProduk extends StatefulWidget {
  final String idProduk;
  final void Function(Map<String, dynamic> satuanAndQuantitySelected)
      onSatuanAndQuantitySelected;
  PilihanSatuanProduk(this.idProduk,
      {required this.onSatuanAndQuantitySelected});

  _PilihanSatuanProdukState createState() => _PilihanSatuanProdukState();
}

class _PilihanSatuanProdukState extends State<PilihanSatuanProduk> {
  Map<String, dynamic>? satuanDanQuantitySelectedState;

  Future<List> getHargaProduk() async {
    String idProduk = widget.idProduk;
    String _params =
        await HTTPRequest.getQueryString(params: 'idProduk=$idProduk');

    print(ServerPath.apiBaseURL + '/Produk/getHargaProduk?' + _params);

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Produk/getHargaProduk?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      List hargaProduk = dataObject['hargaProduk'];

      return hargaProduk;
    }

    return [];
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return FutureBuilder<List>(
        future: getHargaProduk(),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            if (snapshot.data != null) {
              List listHargaProduk = snapshot.data!;
              if (listHargaProduk.length >= 1) {
                List<Widget> listHargaProdukWidget =
                    listHargaProduk.map((hargaProduk) {
                  return PilihanSatuanProdukItem(
                      hargaProduk, satuanDanQuantitySelectedState,
                      (returnedSatuanDanQuantityTerpilih) {
                    widget.onSatuanAndQuantitySelected(
                        returnedSatuanDanQuantityTerpilih);
                    setState(() {
                      satuanDanQuantitySelectedState =
                          returnedSatuanDanQuantityTerpilih;
                    });
                  });
                }).toList();

                return Column(
                    mainAxisSize: MainAxisSize.min,
                    children: listHargaProdukWidget);
              }
            }
            return Center(
              child: Column(children: [
                Image.asset(ClientPath.assetsImg + '/empty.png',
                    width: SizeConfig.horizontalBlock * 25),
                SizedBox(height: 5),
                Text('Mohon maaf, produk ini tidak memiliki daftar harga',
                    style: TextStyle(
                        fontSize: SizeConfig.horizontalBlock * 3,
                        color: CustomColors.textMuted)),
                SizedBox(height: 15)
              ]),
            );
          } else {
            return Padding(
                padding: EdgeInsets.symmetric(vertical: 20),
                child: Center(
                    child: SizedBox(
                        height: 15,
                        width: 15,
                        child: CircularProgressIndicator(strokeWidth: 1.5))));
          }
        });
  }
}
