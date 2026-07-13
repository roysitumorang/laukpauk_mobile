import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/views/penjual/components/UpdateHarga/SatuanProdukItem.dart';
import 'package:laukpauk/views/penjual/components/UpdateHarga/EditSatuanProduk.dart';

class SatuanProduk extends StatefulWidget {
  final String idProduk;
  final bool updateAble, showOptions, showNomorUrut;
  SatuanProduk(this.idProduk,
      {this.showNomorUrut = false,
      this.updateAble = true,
      this.showOptions = false});
  _SatuanProdukState createState() => _SatuanProdukState();
}

class _SatuanProdukState extends State<SatuanProduk> {
  int onSatuanProdukSuccessDeletedState = 0;

  Future<List> getSatuanProduk() async {
    String entitasHargaProduk =
        'id, nama, harga, diskon, jenisHarga, keterangan, idProduk';
    String _params = await HTTPRequest.getQueryString(
        params:
            'idProduk=${widget.idProduk}&entitasHargaProduk=$entitasHargaProduk');

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Produk/getHargaProduk?' + _params);
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      List satuanProduk = dataObject['hargaProduk'];

      return satuanProduk;
    }
    return [];
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text('Daftar Satuan Produk',
                style: TextStyle(
                    fontSize: SizeConfig.horizontalBlock * 4,
                    fontWeight: FontWeight.w700)),
            InkWell(
              onTap: () async {
                String idProduk = widget.idProduk;
                bool? statusAddSatuan = await Navigator.push(
                    context,
                    MaterialPageRoute(
                        builder: (_) => EditSatuanProduk(
                            idProduk: idProduk,
                            appBarTitle: 'Satuan Produk Baru',
                            isUpdate: false)));

                if (statusAddSatuan != null) {
                  if (statusAddSatuan) {
                    setState(() {
                      onSatuanProdukSuccessDeletedState =
                          onSatuanProdukSuccessDeletedState + 1;
                    });
                  } else {
                    showDialog(
                        context: context,
                        builder: (context) => AlertDialog(
                                title: Text('Tambah Satuan Produk',
                                    style: TextStyle(
                                        fontSize:
                                            SizeConfig.horizontalBlock * 4,
                                        fontWeight: FontWeight.w700)),
                                content: Text(
                                    'Gagal menambah data satuan (harga) produk. Silahkan coba lagi !',
                                    style: TextStyle(
                                        fontSize:
                                            SizeConfig.horizontalBlock * 3.25)),
                                actions: [
                                  TextButton(
                                      onPressed: () {
                                        Navigator.pop(context);
                                      },
                                      child: Text('OK'))
                                ]));
                  }
                }
              },
              child: Text('Tambah Harga Baru',
                  style: TextStyle(
                      fontSize: SizeConfig.horizontalBlock * 3.25,
                      color: CustomColors.textPrimary)),
            )
          ],
        ),
        SizedBox(height: 10),
        FutureBuilder<List>(
          builder: (context, snapshot) {
            if (snapshot.hasData) {
              if (snapshot.data != null) {
                List satuanProduk = snapshot.data!;
                if (satuanProduk.length >= 1) {
                  int index = 0;
                  List<Widget> satuanProdukWidget = satuanProduk.map((satuan) {
                    index++;
                    return SatuanProdukItem(satuan,
                        nomorUrut: (widget.showNomorUrut) ? index : false,
                        showOptions: widget.showOptions,
                        updateAble: widget.updateAble,
                        onSatuanProdukDeleted: () {
                      setState(() {
                        onSatuanProdukSuccessDeletedState =
                            onSatuanProdukSuccessDeletedState + 1;
                      });
                    }, onSatuanProdukUpdated: () {
                      setState(() {
                        onSatuanProdukSuccessDeletedState =
                            onSatuanProdukSuccessDeletedState + 1;
                      });
                    });
                  }).toList();

                  return Column(children: satuanProdukWidget);
                }
                return Padding(
                  padding: EdgeInsets.symmetric(vertical: 15),
                  child: Center(
                    child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Image.asset(ClientPath.assetsImg + '/empty.png',
                              width: SizeConfig.horizontalBlock * 25),
                          SizedBox(height: 5),
                          Text('Daftar satuan (harga) tidak ada',
                              style: TextStyle(
                                  fontSize: SizeConfig.horizontalBlock * 3))
                        ]),
                  ),
                );
              }
            }
            return Center(
                child: Padding(
              padding: EdgeInsets.symmetric(vertical: 25),
              child: SizedBox(
                  width: 15,
                  height: 15,
                  child: CircularProgressIndicator(strokeWidth: 1.5)),
            ));
          },
          future: getSatuanProduk(),
        ),
      ],
    );
  }
}
