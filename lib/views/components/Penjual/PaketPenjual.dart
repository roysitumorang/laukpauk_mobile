import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/components/Penjual/PaketPenjualCard.dart';

import 'package:laukpauk/global-components/Pencarian.dart';
import 'package:laukpauk/models/HeaderModel.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';

import 'package:laukpauk/models/PaketModel.dart';

class PaketPenjual extends StatefulWidget {
  final String idPenjual;
  PaketPenjual({required this.idPenjual});

  _PaketPenjualState createState() => _PaketPenjualState();
}

class _PaketPenjualState extends State<PaketPenjual> {
  bool isReadyToShow = false;
  List paketPenjualState = [];
  List paketPenjualImmutableState = [];
  String pencarian = '';

  Future<List> getPaketPenjual() async {
    String idPenjual = widget.idPenjual.toString();
    String getPaketEntities = 'nama, foto, id, harga, diskon, idPenjual';
    String _params = await HTTPRequest.getQueryString(
        params: 'getPaketEntities=' + getPaketEntities);

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Penjual/getPaketPenjual/' +
        idPenjual +
        '?' +
        _params);

    http.Response httpResponse = await http.get(_url);
    if (httpResponse.statusCode == 200) {
      dynamic dataObject = jsonDecode(httpResponse.body);
      List paketPenjual = dataObject['paketPenjual'];

      return paketPenjual;
    }
    return [];
  }

  void initialization() async {
    List paketPenjual = await getPaketPenjual();

    setState(() {
      isReadyToShow = true;
      paketPenjualState = paketPenjual;
      paketPenjualImmutableState = paketPenjual;
    });
  }

  void initState() {
    super.initState();
    initialization();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    List<Widget> paketPenjualWidget = (paketPenjualState.length >= 1)
        ? paketPenjualState.map((produk) {
            return PaketPenjualCard(produk,
                deteksiADJD: () async {
                  Map<String, dynamic> adjdSet =
                      await HeaderModel.adjd(context);
                  String? latitude = adjdSet['latitude'];
                  String? longitude = adjdSet['longitude'];
                  String? jadwalDeliveryCode = adjdSet['kodeJadwalDelivery'];

                  bool isADJDCompleted = false;
                  if (latitude != null &&
                      longitude != null &&
                      jadwalDeliveryCode != null) {
                    isADJDCompleted = true;
                    String idPaket = produk['id'];
                    bool statusAddToCart = await PaketModel.addToCart(idPaket);

                    String _text = (statusAddToCart)
                        ? 'Produk berhasil ditambahkan ke keranjang !'
                        : 'Produk gagal ditambahkan ke keranjang, silahkan coba lagi.';

                    SnackBar _snackBar = PaketModel.addToCartSnackBar(
                        context: context, text: _text);
                    ScaffoldMessenger.of(context).showSnackBar(_snackBar);

                    int jumlahKeranjang = await HeaderModel.getJumlahCart();
                    Provider.of<HeaderViewModel>(context, listen: false)
                        .jumlahCart = jumlahKeranjang;
                  }
                  Provider.of<HeaderViewModel>(context, listen: false)
                      .isADJDSet = isADJDCompleted;
                },
                reload: () => initialization());
          }).toList()
        : [];

    return (isReadyToShow)
        ? (paketPenjualState.length >= 1)
            ? Column(
                children: [
                  Padding(
                    padding: EdgeInsets.only(top: 5.0),
                    child: Pencarian(
                        onSearch: (String string) {
                          String stringLC = string.toLowerCase();
                          List paketPenjualFiltered =
                              paketPenjualImmutableState.where((produk) {
                            String namaProduk = produk['nama'];
                            String namaProdukLC = namaProduk.toLowerCase();

                            return namaProdukLC.indexOf(stringLC) >= 0;
                          }).toList();

                          setState(() {
                            paketPenjualState = paketPenjualFiltered;
                            pencarian = stringLC;
                          });
                        },
                        onClose: () {
                          setState(() {
                            paketPenjualState = paketPenjualImmutableState;
                            pencarian = '';
                          });
                        },
                        hint: 'Cari Produk di Sini',
                        text: pencarian),
                  ),
                  SizedBox(height: 5.0),
                  Expanded(
                    child: GridView.count(
                        padding: EdgeInsets.only(top: 0),
                        crossAxisCount: 3,
                        childAspectRatio: 0.5,
                        children: paketPenjualWidget),
                  ),
                ],
              )
            : Column(children: [
                Padding(
                  padding: EdgeInsets.only(top: 5.0),
                  child: Pencarian(
                      onSearch: (String string) {
                        String stringLC = string.toLowerCase();
                        List paketPenjualFiltered =
                            paketPenjualImmutableState.where((produk) {
                          String namaProduk = produk['nama'];
                          String namaProdukLC = namaProduk.toLowerCase();

                          return namaProdukLC.indexOf(stringLC) >= 0;
                        }).toList();

                        setState(() {
                          paketPenjualState = paketPenjualFiltered;
                          pencarian = stringLC;
                        });
                      },
                      onClose: () {
                        setState(() {
                          paketPenjualState = paketPenjualImmutableState;
                          pencarian = '';
                        });
                      },
                      hint: 'Cari Produk di Sini',
                      text: pencarian),
                ),
                SizedBox(height: 5.0),
                Expanded(
                  child: LayoutBuilder(builder: (context, constraints) {
                    return Container(
                        constraints:
                            BoxConstraints(minHeight: constraints.maxHeight),
                        child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Image.asset(ClientPath.assetsImg + '/empty.png',
                                  width: SizeConfig.horizontalBlock * 25),
                              Padding(
                                  padding: EdgeInsets.only(top: 5),
                                  child: Text('Tidak Ada Produk',
                                      style: TextStyle(
                                          color: CustomColors.textMuted,
                                          fontSize: SizeConfig.horizontalBlock *
                                              3.5)))
                            ]));
                  }),
                )
              ])
        : Center(
            child: SizedBox(
                width: 25,
                height: 25,
                child: CircularProgressIndicator(strokeWidth: 1.5)));
  }
}
