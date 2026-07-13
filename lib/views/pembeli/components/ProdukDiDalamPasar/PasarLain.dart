import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';

import 'package:laukpauk/views/pembeli/components/ProdukDiDalamPasar/PasarLainDalamCA.dart';
import 'package:laukpauk/views/pembeli/components/ProdukDiDalamPasar/PasarLainLuarCA.dart';

import 'package:laukpauk/models/HeaderModel.dart';
import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class PasarLain extends StatelessWidget {
  final String idPasar;
  PasarLain(this.idPasar);

  Widget build(BuildContext context) {
    return Consumer<HeaderViewModel>(builder: (context, headerStateManager, _) {
      bool isADJDSet = headerStateManager.isADJDSet;
      return (isADJDSet)
          ? Column(children: [
              Padding(
                padding: EdgeInsets.symmetric(horizontal: 10, vertical: 15),
                child: PasarLainDalamCA(currentPasar: idPasar),
              ),
              Padding(
                padding: EdgeInsets.symmetric(horizontal: 10, vertical: 10),
                child: PasarLainLuarCA(),
              )
            ])
          : Padding(
              padding: EdgeInsets.fromLTRB(15, 10, 15, 20),
              child: Center(
                child: Column(children: [
                  Image.asset(
                      ClientPath.assetsImg + '/adjd-belum-ditentukan.png',
                      width: SizeConfig.horizontalBlock * 25),
                  SizedBox(height: 5),
                  Text(
                      'Anda belum menentukan lokasi dan jadwal pengantaran. Untuk melihat pasar lain yang berada didalam dan diluar area anda silahkan tentukan terlebih dahulu lokasi dan jadwal pengantaran.',
                      textAlign: TextAlign.center,
                      style: TextStyle(
                          color: CustomColors.textMuted,
                          fontSize: SizeConfig.horizontalBlock * 2.75)),
                  SizedBox(height: 15),
                  CustomButton.button(
                      context, 'Tentukan Lokasi dan Alamat Pengantaran',
                      () async {
                    Map<String, dynamic> adjdSet =
                        await HeaderModel.adjd(context);

                    String? latitude = adjdSet['latitude'];
                    String? longitude = adjdSet['longitude'];
                    String? kodeJadwalDelivery = adjdSet['kodeJadwalDelivery'];

                    bool isADJDCompleted = false;
                    if (latitude != null &&
                        longitude != null &&
                        kodeJadwalDelivery != null) {
                      isADJDCompleted = true;
                      Provider.of<HeaderViewModel>(context, listen: false)
                          .isADJDSet = isADJDCompleted;
                    }
                  },
                      isPill: true,
                      color: CustomButton.themeColor,
                      size: CustomButton.smSize)
                ]),
              ),
            );
    });
  }
}
