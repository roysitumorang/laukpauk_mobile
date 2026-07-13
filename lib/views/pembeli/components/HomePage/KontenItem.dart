import 'package:flutter/material.dart';
import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/models/KontenModel.dart';
import 'package:laukpauk/models/ProdukModel.dart';
import 'package:laukpauk/view-models/HeaderViewModel.dart';
import 'package:laukpauk/models/HeaderModel.dart';
import 'package:laukpauk/models/ADJDModel.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/components/ProdukDiDalamPasar/ProdukDiDalamPasarItem.dart';
import 'package:laukpauk/views/pembeli/components/HomePage/KontenItemBanner.dart';
import 'package:provider/provider.dart';

class KontenItem extends StatelessWidget {
  final Map<String, dynamic> detailKonten;
  KontenItem(this.detailKonten);

  Widget build(BuildContext context) {
    String idKonten = detailKonten['id'].toString();
    String jenisItem = detailKonten['jenisItem'].toString();
    SizeConfig().initSize(context);

    return FutureBuilder<List>(
        future: KontenModel.getProdukKonten(idKonten, limit: 8),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            if (snapshot.data != null) {
              List kontenItem = snapshot.data!;
              if (kontenItem.length >= 1) {
                return (jenisItem == 'produk')
                    ? Container(
                        width: SizeConfig.horizontalBlock * 100,
                        height: 275.0,
                        child: ListView.builder(
                            padding: EdgeInsets.zero,
                            scrollDirection: Axis.horizontal,
                            itemCount: kontenItem.length,
                            itemBuilder: (context, index) {
                              Map<String, dynamic> dataProduk =
                                  kontenItem[index];

                              return ProdukInThisPasarItem(dataProduk,
                                  () async {
                                ADJDModel.deleteAlamatDanJadwalDelivery();
                                Map<String, dynamic> adjdSet =
                                    await HeaderModel.adjd(context);

                                String? latitude = adjdSet['latitude'];
                                String? longitude = adjdSet['longitude'];
                                String? kodeJadwalDelivery =
                                    adjdSet['kodeJadwalDelivery'];

                                bool isADJDCompleted = false;
                                if (latitude != null &&
                                    longitude != null &&
                                    kodeJadwalDelivery != null) {
                                  isADJDCompleted = true;
                                  Provider.of<HeaderViewModel>(context,
                                          listen: false)
                                      .isADJDSet = isADJDCompleted;
                                  await ProdukModel.showSatuanProduk(
                                      context, dataProduk);
                                }
                              }, widgetWidth: SizeConfig.horizontalBlock * 35);
                            }),
                      )
                    : KontenItemBanner(detailKonten, kontenItem);
              }
            }

            return Container(
              padding: EdgeInsets.symmetric(vertical: 15),
              width: SizeConfig.horizontalBlock * 100,
              child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    Image.asset(ClientPath.assetsImg + '/empty.png',
                        width: SizeConfig.horizontalBlock * 25),
                    SizedBox(height: 10),
                    Text('Tidak Ada Item Konten !',
                        style: TextStyle(
                            color: CustomColors.textMuted,
                            fontSize: SizeConfig.horizontalBlock * 3.5))
                  ]),
            );
          } else {
            return Loading();
          }
        });
  }
}
