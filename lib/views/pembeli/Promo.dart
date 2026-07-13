import 'package:flutter/material.dart';
import 'package:laukpauk/views/pembeli/components/Promo/ProdukPromo.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';
import 'package:laukpauk/models/HeaderModel.dart';
import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/global-components/Header.dart';
import 'package:laukpauk/global-components/Footer.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:provider/provider.dart';

class Promo extends StatefulWidget {
  PromoState createState() => PromoState();
}

class PromoState extends State<Promo> {
  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Scaffold(
      body: Column(mainAxisAlignment: MainAxisAlignment.start, children: [
        Header(),
        Consumer<HeaderViewModel>(builder: (context, headerStateManager, _) {
          bool isADJDSet = headerStateManager.isADJDSet;

          return (isADJDSet)
              ? ProdukPromo()
              : Expanded(
                  child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Padding(
                          padding: EdgeInsets.symmetric(
                              horizontal: SizeConfig.horizontalBlock * 15.0),
                          child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Image.asset(
                                    ClientPath.assetsImg +
                                        '/adjd-belum-ditentukan.png',
                                    width: SizeConfig.horizontalBlock * 35),
                                SizedBox(height: 5),
                                Text(
                                    'Oops, anda harus menentukan lokasi anda untuk melihat promo kami',
                                    textAlign: TextAlign.center)
                              ]),
                        ),
                        SizedBox(height: 15),
                        CustomButton.button(
                            context, 'Tentukan Lokasi dan Jadwal Delivery',
                            () async {
                          Map<String, dynamic> adjdSet =
                              await HeaderModel.adjd(context);
                          String? latitude = adjdSet['latitude'];
                          String? longitude = adjdSet['longitude'];
                          String? jadwalDeliveryCode =
                              adjdSet['kodeJadwalDelivery'];

                          bool isADJDCompleted = false;
                          if (latitude != null &&
                              longitude != null &&
                              jadwalDeliveryCode != null) {
                            isADJDCompleted = true;
                          }
                          Provider.of<HeaderViewModel>(context, listen: false)
                              .isADJDSet = isADJDCompleted;
                        }, color: CustomButton.themeColor, isPill: true)
                      ]),
                );
        })
      ]),
      bottomNavigationBar: Footer(),
    );
  }
}
