import 'package:flutter/material.dart';
import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/models/HomepageModel.dart';
import 'package:laukpauk/models/HeaderModel.dart';
import 'package:laukpauk/models/ADJDModel.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/global-components/Pencarian.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/components/HomePage/Pasar.dart';
import 'package:laukpauk/views/pembeli/SemuaPasar.dart';

import 'package:laukpauk/views/pembeli/components/HomePage/ProdukPasarSekitar.dart';

class PasarSekitarSaya extends StatefulWidget {
  final bool showPilihLokasiLain, showTitle;
  final bool showAll, showPencarian, showProdukPasarSekitar;
  PasarSekitarSaya(
      {this.showPilihLokasiLain = true,
      this.showTitle = true,
      this.showAll = false,
      this.showPencarian = false,
      this.showProdukPasarSekitar = true});
  _PasarSekitarSayaState createState() => _PasarSekitarSayaState();
}

class _PasarSekitarSayaState extends State<PasarSekitarSaya> {
  String pencarian = '';

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    List<Widget> pasarSekitarSayaWidget = [];

    bool _showProdukPasarSekitar = widget.showProdukPasarSekitar;

    return Consumer<HeaderViewModel>(builder: (context, headerStateManager, _) {
      //untuk fungsi ini sengaja tidak menggunakan library httprequest karena fungsinya tidak async

      bool showTitle = widget.showTitle;
      bool showPilihLokasiLain = widget.showPilihLokasiLain;
      bool showAll = widget.showAll;
      bool showPencarian = widget.showPencarian;

      bool isADJDSet = headerStateManager.isADJDSet;
      bool showJarakPasar = isADJDSet;

		Map<String, dynamic>? _savedADJD 	=	headerStateManager.savedADJD;
      

      return FutureBuilder<List>(
          future: HomepageModel.getDataPasar(showAll: showAll, isADJDSet: isADJDSet, pencarian: pencarian, savedADJD:_savedADJD ),
          builder: (context, snapshot) {
            if (snapshot.hasData) {
              if (snapshot.data != null) {
                List pasarSekitarSaya = snapshot.data!;

                bool _isPasarExist = pasarSekitarSaya.length >= 1;

                if (_isPasarExist) {
                  pasarSekitarSayaWidget = pasarSekitarSaya.map((pasar) {
                    Widget _returnedWidget = Container(
                        width: SizeConfig.horizontalBlock * 23.3,
                        height: SizeConfig.horizontalBlock * 23.3,
                        child: Pasar(pasar, showDistance: showJarakPasar));
                    return _returnedWidget;
                  }).toList();
                }

                return Column(children: [
                  (showTitle)
                      ? Padding(
                          padding: EdgeInsets.only(bottom: 15.0),
                          child: Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Text('Pasar',
                                    textAlign: TextAlign.left,
                                    style: TextStyle(
                                        fontWeight: FontWeight.w500,
                                        fontSize:
                                            SizeConfig.horizontalBlock * 4.0)),
                                InkWell(
                                    onTap: () {
                                      Navigator.push(
                                          context,
                                          MaterialPageRoute(
                                              builder: (_) => SemuaPasar()));
                                    },
                                    child: Text('Lihat semua',
                                        style: TextStyle(
                                            color: CustomColors.darkGreen,
                                            fontWeight: FontWeight.w500,
                                            fontSize:
                                                SizeConfig.horizontalBlock *
                                                    3.0)))
                              ]))
                      : SizedBox(),
                  (showPencarian)
                      ? Pencarian(
                          onSearch: (String string) {
                            String stringLC = string.toLowerCase();

                            setState(() {
                              pencarian = stringLC;
                            });
                          },
                          onClose: () {
                            setState(() {
                              pencarian = '';
                            });
                          },
                          hint: 'Cari Pasar dengan Nama',
                          text: pencarian)
                      : SizedBox(),
                  SizedBox(height: (showPencarian) ? 10.0 : 0),
                  (_isPasarExist)
                      ? Container(
                          width: SizeConfig.horizontalBlock * 100,
                          padding: EdgeInsets.only(bottom: 10.0),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Wrap(
                                  children: pasarSekitarSayaWidget,
                                  alignment: WrapAlignment.start,
                                  spacing: 1,
                                  runSpacing: 2.5),
                              SizedBox(height: 7.5)
                            ],
                          ),
                        )
                      : Container(
                          width: SizeConfig.horizontalBlock * 100,
                          padding: EdgeInsets.symmetric(
                              horizontal: SizeConfig.horizontalBlock * 20.0,
                              vertical: 15),
                          child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              crossAxisAlignment: CrossAxisAlignment.center,
                              children: [
                                Image.asset(ClientPath.assetsImg + '/empty.png',
                                    width: SizeConfig.horizontalBlock * 25.0),
                                SizedBox(height: 10),
                                Text(
                                    'Yahh! Belum ada pasar disekitar lokasimu mak. Maaf ya mak...',
                                    textAlign: TextAlign.center,
                                    style: TextStyle(
                                        color: CustomColors.textMuted,
                                        fontSize:
                                            SizeConfig.horizontalBlock * 3.25)),
                                SizedBox(height: 10)
                              ]),
                        ),
                  (showPilihLokasiLain)
                      ? CustomButton.button(context, 'Pilih Lokasi Lain',
                          () async {
                          ADJDModel.deleteAlamatDanJadwalDelivery();

                          await HeaderModel.adjd(context);
                        },
                          size: CustomButton.smSize,
                          isPill: true,
                          color: CustomButton.themeColor)
                      : SizedBox(),
                  SizedBox(height: (showPilihLokasiLain) ? 25.0 : 0.0),
                  (_isPasarExist)
                      ? (_showProdukPasarSekitar)
                          ? ProdukPasarSekitar(
                              pasarSekitarSaya: pasarSekitarSaya)
                          : SizedBox()
                      : SizedBox()
                ]);
              }
            }

            return Center(
                child: Padding(
              padding: EdgeInsets.symmetric(vertical: 25.0),
              child: Column(
                children: [
                  Loading(),
                  SizedBox(height: 10.0),
                  Text('Sedang Mencari Pasar ..',
                      style: TextStyle(
                          fontSize: SizeConfig.horizontalBlock * 3.25))
                ],
              ),
            ));
          });
    });
  }
}
