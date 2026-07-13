import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/views/pembeli/ProdukDiDalamPasar.dart';

import 'package:laukpauk/global-components/Footer.dart';
import 'package:laukpauk/global-components/Header.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/components/Penjual/ProdukPenjual.dart';
import 'package:laukpauk/views/components/Penjual/PaketPenjual.dart';

class Penjual extends StatefulWidget {
  final String id;
  final String? jenisProdukSelected;
  Penjual({required this.id, this.jenisProdukSelected});

  _PenjualState createState() => _PenjualState();
}

class _PenjualState extends State<Penjual> with TickerProviderStateMixin {
  int jenisProdukSelected = 0;
  bool showToTopButtonState = false;

  Future<dynamic> getDetailPenjual() async {
    String idPenjual = widget.id.toString();

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Penjual/getDetailPenjual/$idPenjual');

    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      dynamic dataObject = jsonDecode(httpResponse.body);
      return dataObject['detailPenjual'];
    }

    return false;
  }

  void initialization() async {
    String? jenisProdukSelectedString = widget.jenisProdukSelected;

    if (jenisProdukSelectedString != null) {
      setState(() {
        jenisProdukSelected = (jenisProdukSelectedString == 'satuan') ? 0 : 1;
      });
    }
  }

  void initState() {
    super.initState();
    this.initialization();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Scaffold(
        backgroundColor: CustomColors.nearlyWhite,
        body: FutureBuilder(
            future: getDetailPenjual(),
            builder: (context, snapshot) {
              if (snapshot.hasData) {
                dynamic detailPenjual = snapshot.data;

                if (detailPenjual != false) {
                  String? namaToko = (detailPenjual['namaToko'] == null)
                      ? 'Toko Tidak Dikenal'
                      : detailPenjual['namaToko'];
                  String namaPasar = (detailPenjual['namaPasar'] == null)
                      ? 'Belum Menentukan Pasar'
                      : (detailPenjual['namaPasar'].length >= 1)
                          ? detailPenjual['namaPasar']
                          : 'Pasar Tanpa Nama';

                  String _alamatToko = (detailPenjual['alamatToko'] == null)
                      ? 'Alamat belum ditentukan'
                      : detailPenjual['alamatToko'];

                  String idPasar = detailPenjual['pasar'].toString();

                  return Column(children: [
                    Header(),
                    Expanded(
                      child: RefreshIndicator(
                        onRefresh: () async {
                          setState(() {});
                        },
                        child: ListView(
                            shrinkWrap: true,
                            padding: EdgeInsets.zero,
                            children: [
                              Container(
                                  width: SizeConfig.horizontalBlock * 100,
                                  height: SizeConfig.horizontalBlock * 60,
                                  child: Image.network(
                                      ServerPath.baseURL +
                                          '/' +
                                          ServerPath.uploadGambarBannerPenjual +
                                          '/' +
                                          detailPenjual['banner'],
                                      fit: BoxFit.cover, errorBuilder:
                                          (context, error, stackTrace) {
                                    print('Error meload banner penjual');
                                    return Image.asset(
                                        ClientPath.assetsImg + '/no-image.png',
                                        fit: BoxFit.cover);
                                  })),
                              SizedBox(
                                height: 15,
                              ),
                              Text(namaToko ?? '',
                                  textAlign: TextAlign.center,
                                  style: TextStyle(
                                      fontStyle: (namaToko == null)
                                          ? FontStyle.italic
                                          : null,
                                      color: CustomColors.green,
                                      fontSize: SizeConfig.horizontalBlock * 5,
                                      fontWeight: FontWeight.w700)),
                              SizedBox(height: 5.0),
                              Text(_alamatToko,
                                  textAlign: TextAlign.center,
                                  style: TextStyle(
                                      color: CustomColors.grey,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.0)),
                              SizedBox(
                                  height: SizeConfig.horizontalBlock * 3.0),
                              GestureDetector(
                                  onTap: () => Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                          builder: (_) => ProdukDiDalamPasar(
                                              idPasar: idPasar,
                                              namaPasar: namaPasar))),
                                  child: Text(namaPasar,
                                      textAlign: TextAlign.center,
                                      style: TextStyle(
                                          color: CustomColors.textInfo,
                                          fontSize:
                                              SizeConfig.horizontalBlock * 3.5,
                                          fontWeight: FontWeight.w600))),
                              Text('Klik pasar untuk lihat produk di dalamnya',
                                  textAlign: TextAlign.center,
                                  style: TextStyle(
                                      color: CustomColors.textMuted,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 2.75)),
                              SizedBox(
                                height: 5,
                              ),
                              Padding(
                                padding: EdgeInsets.symmetric(horizontal: 50),
                                child: Center(
                                  child: TabBar(
                                      isScrollable: true,
                                      onTap: (value) {
                                        setState(() {
                                          jenisProdukSelected = value;
                                        });
                                      },
                                      unselectedLabelColor:
                                          CustomColors.textMuted,
                                      indicatorColor: CustomColors.green,
                                      labelColor: CustomColors.green,
                                      controller: TabController(
                                          length: 2,
                                          vsync: this,
                                          initialIndex: jenisProdukSelected),
                                      tabs: [
                                        Tab(child: Text('Produk')),
                                        Tab(child: Text('Paket'))
                                      ]),
                                ),
                              ),
                              SizedBox(height: 10),
                              (jenisProdukSelected == 0)
                                  ? Container(
                                      height: SizeConfig.verticalBlock * 69.0,
                                      padding: EdgeInsets.symmetric(
                                          horizontal: 10.0),
                                      child:
                                          ProdukPenjual(idPenjual: widget.id))
                                  : Container(
                                      height: SizeConfig.verticalBlock * 69.0,
                                      padding: EdgeInsets.symmetric(
                                          horizontal: 10.0),
                                      child: PaketPenjual(idPenjual: widget.id))
                            ]),
                      ),
                    )
                  ]);
                } else {
                  return Container(
                    width: MediaQuery.of(context).size.width,
                    child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Image.asset(ClientPath.assetsImg + '/empty.png',
                              width: SizeConfig.horizontalBlock * 30),
                          Padding(
                              padding: EdgeInsets.only(top: 15, bottom: 15),
                              child: Text('Data Penjual tidak Ditemukan',
                                  style: TextStyle(
                                      color: CustomColors.textMuted,
                                      fontSize: SizeConfig.horizontalBlock * 3,
                                      fontWeight: FontWeight.w700)))
                        ]),
                  );
                }
              } else {
                return Loading();
              }
            }),
        bottomNavigationBar: Footer());
  }
}
