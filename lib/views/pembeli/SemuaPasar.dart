import 'package:flutter/material.dart';
import 'package:laukpauk/global-components/Loading.dart';

import 'package:laukpauk/views/pembeli/components/HomePage/PasarSekitarSaya.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class SemuaPasar extends StatefulWidget {
  _SemuaPasarState createState() => _SemuaPasarState();
}

class _SemuaPasarState extends State<SemuaPasar> {
  List pasarState = [];
  List pasarImmutableState = [];
  bool isReadyToShow = true, isADJDSet = false;
  String pencarian = '';

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Scaffold(
        backgroundColor: CustomColors.aliceBlue,
        appBar: AppBar(
            title: Text('Semua Pasar'), backgroundColor: CustomColors.green),
        body: (isReadyToShow)
            ? Padding(
                padding: EdgeInsets.symmetric(vertical: 10.0, horizontal: 10.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    PasarSekitarSaya(
                        showProdukPasarSekitar: false,
                        showTitle: false,
                        showPilihLokasiLain: true,
                        showAll: true,
                        showPencarian: true)
                  ],
                ),
              )
            : Loading());
  }
}
