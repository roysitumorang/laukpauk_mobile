import 'package:flutter/material.dart';

import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/global-components/Pencarian.dart';
import 'package:laukpauk/global-components/Footer.dart';
import 'package:laukpauk/global-components/Header.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ClientPath.dart';

import 'package:laukpauk/views/pembeli/components/Resep/ResepCard.dart';

import 'package:laukpauk/models/ResepModel.dart';

class Resep extends StatefulWidget {
  _ResepState createState() => _ResepState();
}

class _ResepState extends State<Resep> {
  List resepState = [];
  List resepImmutableState = [];
  bool isReadyToShow = false;
  String pencarian = '';

  void initialization() async {
    List dataResep = await ResepModel.getResep(context);
    setState(() {
      isReadyToShow = true;
      resepState = dataResep;
      resepImmutableState = dataResep;
    });
  }

  void initState() {
    super.initState();
    initialization();
  }

  Widget build(BuildContext context) {
    List<Widget> listResepWidget = [];
    if (isReadyToShow) {
      if (resepState.length >= 1) {
        listResepWidget = resepState.map((resep) => ResepCard(resep)).toList();
      }
    }

    return Scaffold(
        backgroundColor: CustomColors.nearlyWhite,
        body: (isReadyToShow)
            ? Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Header(),
                  Padding(
                    padding:
                        EdgeInsets.symmetric(vertical: 10.0, horizontal: 5.0),
                    child: Pencarian(
                        onSearch: (String string) {
                          String stringLC = string.toLowerCase();

                          List filteredResep =
                              resepImmutableState.where((resep) {
                            String namaResep = resep['nama'];
                            String namaResepLC = namaResep.toLowerCase();

                            return namaResepLC.indexOf(stringLC) >= 0;
                          }).toList();

                          setState(() {
                            pencarian = stringLC;
                            resepState = filteredResep;
                          });
                        },
                        onClose: () {
                          setState(() {
                            pencarian = '';
                            resepState = resepImmutableState;
                          });
                        },
                        hint: 'Cari Resep dengan Nama',
                        text: pencarian),
                  ),
                  Expanded(
                      child: LayoutBuilder(builder: (context, constraints) {
                    return RefreshIndicator(
                        onRefresh: () async {
                          initialization();
                        },
                        child: (resepState.length >= 1)
                            ? ListView(
                                shrinkWrap: true,
                                padding: EdgeInsets.symmetric(horizontal: 10.0),
                                children: listResepWidget)
                            : ListView(
                                shrinkWrap: true,
                                padding: EdgeInsets.zero,
                                children: [
                                    Container(
                                        constraints: BoxConstraints(
                                            minHeight: constraints.maxHeight),
                                        child: Column(
                                            mainAxisAlignment:
                                                MainAxisAlignment.center,
                                            crossAxisAlignment:
                                                CrossAxisAlignment.center,
                                            children: [
                                              Image.asset(
                                                  ClientPath.assetsImg +
                                                      '/empty.png',
                                                  width: SizeConfig
                                                          .horizontalBlock *
                                                      35),
                                              SizedBox(height: 10),
                                              Text('Tidak Ada Resep',
                                                  style: TextStyle(
                                                      color: CustomColors
                                                          .textMuted,
                                                      fontSize: SizeConfig
                                                              .horizontalBlock *
                                                          3.50))
                                            ]))
                                  ]));
                  }))
                ],
              )
            : Loading(),
        bottomNavigationBar: Footer());
  }
}
