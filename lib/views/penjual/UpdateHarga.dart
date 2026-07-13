import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/views/penjual/components/UpdateHarga/UpdateHargaItem.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/ClientPath.dart';

class UpdateHarga extends StatefulWidget {
  _UpdateHargaState createState() => _UpdateHargaState();
}

class _UpdateHargaState extends State<UpdateHarga> {
  String cariProdukStringState = '';
  TextEditingController cariProdukController = TextEditingController();

  Future<List> getProduk() async {
    if (cariProdukStringState.length >= 1) {
      String getProductEntities = 'id, foto, nama, namaKategoriProduk';
      String _params = await HTTPRequest.getQueryString(
          authToken: true,
          params:
              'getProductEntities=$getProductEntities&term=$cariProdukStringState');

      Uri _url = Uri.parse(
          ServerPath.apiBaseURL + '/Penjual/getProdukPenjual?' + _params);
      Response httpResponse = await get(_url);

      if (httpResponse.statusCode == 200) {
        Map dataObject = jsonDecode(httpResponse.body);
        List produkPenjual = dataObject['produkPenjual'];

        return produkPenjual;
      }
    }
    return [];
  }

  cariProduk() async {
    String cariProdukString = cariProdukController.text;
    setState(() {
      cariProdukStringState = cariProdukString;
    });
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Scaffold(
        appBar: AppBar(
            title: Text('Update Harga Produk'),
            backgroundColor: CustomColors.green),
        backgroundColor: CustomColors.aliceBlue,
        body: SingleChildScrollView(
            child: Padding(
          padding: EdgeInsets.symmetric(horizontal: 5, vertical: 10),
          child: Column(children: [
            Card(
                elevation: 5,
                shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(10)),
                child: Padding(
                  padding: EdgeInsets.symmetric(horizontal: 15, vertical: 20),
                  child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Update Harga Produk Massal',
                            style: TextStyle(
                                fontWeight: FontWeight.w700,
                                fontSize: SizeConfig.horizontalBlock * 4)),
                        SizedBox(height: 5),
                        Text(
                            'Isi kotak pencarian produk dan isi harga satuan - satuan dari produk tersebut',
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 3.5)),
                        SizedBox(height: 15),
                        Material(
                          color: Color(0x5566CC99),
                          shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(50)),
                          child: Padding(
                            padding: EdgeInsets.symmetric(horizontal: 10),
                            child: TextField(
                                onSubmitted: (cariProduk) {
                                  setState(() {
                                    cariProdukStringState = cariProduk;
                                  });
                                },
                                controller: cariProdukController,
                                textAlignVertical: TextAlignVertical.center,
                                textAlign: TextAlign.left,
                                style: TextStyle(
                                    color: CustomColors.darkText,
                                    fontSize: SizeConfig.horizontalBlock * 3.5),
                                decoration: InputDecoration(
                                    isDense: true,
                                    contentPadding: EdgeInsets.only(
                                        top: 5, bottom: 5, left: 15),
                                    suffixIcon: IconButton(
                                      onPressed: () {
                                        cariProduk();
                                      },
                                      icon: Icon(FontAwesomeIcons.search,
                                          color: CustomColors.darkGreen,
                                          size: 14),
                                    ),
                                    border: InputBorder.none,
                                    hintText:
                                        'Cari Produk Anda dengan Nama Produk')),
                          ),
                        )
                      ]),
                )),
            FutureBuilder<List>(
                builder: (context, snapshot) {
                  if (snapshot.hasData) {
                    if (snapshot.data != null) {
                      List dataProduk = snapshot.data!;
                      if (dataProduk.length >= 1) {
                        List<Widget> produkWidget = dataProduk
                            .map((produk) => UpdateHargaItem(produk))
                            .toList();
                        return Column(children: produkWidget);
                      } else {
                        SizeConfig().initSize(context);
                        return Container(
                            width: SizeConfig.horizontalBlock * 100,
                            padding: EdgeInsets.symmetric(vertical: 15),
                            child: Column(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                  Image.asset(
                                      ClientPath.assetsImg + '/empty.png',
                                      width: SizeConfig.horizontalBlock * 35),
                                  SizedBox(height: 5),
                                  Text(
                                      'Tidak ada data produk dengan nama ' +
                                          cariProdukController.text +
                                          ' !',
                                      style: TextStyle(
                                          color: CustomColors.textDanger,
                                          fontSize:
                                              SizeConfig.horizontalBlock * 4))
                                ]));
                      }
                    }
                  }
                  return Center(
                      child: SizedBox(
                          width: 15,
                          height: 15,
                          child: CircularProgressIndicator(strokeWidth: 1.5)));
                },
                future: getProduk())
          ]),
        )));
  }
}
