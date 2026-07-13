import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/views/pembeli/components/HomePage/HeroSlider.dart';
import 'package:laukpauk/views/pembeli/components/HomePage/WalletAndVoucher.dart';
import 'package:laukpauk/views/pembeli/components/HomePage/PasarSekitarSaya.dart';
import 'package:laukpauk/views/pembeli/components/HomePage/Artikel.dart';
import 'package:laukpauk/views/pembeli/components/HomePage/KontenAplikasi.dart';

import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';


import 'package:laukpauk/models/MainModel.dart';
import 'package:laukpauk/models/HeaderModel.dart';

import 'package:laukpauk/global-components/Footer.dart';
import 'package:laukpauk/global-components/Header.dart';

class HomePage extends StatefulWidget {
  HomePageState createState() => HomePageState();
}

class HomePageState extends State<HomePage> with WidgetsBindingObserver {
  void initState() {
    super.initState();
    _initialization();
  }
  

  Future<void> _initialization() async {
    await MainModel.initCallbackFromNativeCode(context);
    await HeaderModel.giftChecking(context);
    await HeaderModel.makeSureADJD(context);

    int _jumlahNotifikasi = await HeaderModel.getJumlahNotifikasi();
    Provider.of<HeaderViewModel>(context, listen: false).jumlahNotifikasi =
        _jumlahNotifikasi;

  }

  int cobaLagi = 0;

  Widget build(BuildContext context) {

    return Scaffold(
        backgroundColor: CustomColors.nearlyWhite,
        body:  Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Header(askADJD: true, askADJDListener: cobaLagi),
                  Expanded(
                    child: RefreshIndicator(
                      onRefresh: () async {
                        setState(() {
                          cobaLagi++;
                        });
                      },
                      child: ListView(
                          shrinkWrap: true,
                          padding: EdgeInsets.zero,
                          children: [
                            HeroSlider(),
                            Padding(
                                padding: EdgeInsets.only(
                                    right: 10, left: 10, top: 10.0),
                                child: Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      WalletAndVoucher(),
                                      SizedBox(height: 20.0),
                                      PasarSekitarSaya(),
                                      KontenAplikasi(),
                                      Artikel(),
                                      SizedBox(height: 15.0)
                                    ]))
                          ]),
                    ),
                  ),
                ],
              ),
        bottomNavigationBar: Footer());
  }
}
