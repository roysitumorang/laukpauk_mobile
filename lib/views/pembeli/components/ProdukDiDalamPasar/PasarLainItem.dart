import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/ProdukDiDalamPasar.dart';

class PasarLainItem extends StatelessWidget {
  final Map dataPasar;
  final bool showDistance;
  PasarLainItem(this.dataPasar, {this.showDistance = false});

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String imageURL = ServerPath.baseURL +
        '/' +
        ServerPath.uploadGambarPasar +
        '/compress/' +
        dataPasar['gambar'];

    if (dataPasar['pasarUmum'] == '0') {
      imageURL = ClientPath.assetsImg + '/laukpauk.png';
    }

    String distanceString = '';
    if (showDistance) {
      double distanceDouble = double.parse(dataPasar['distance']);
      distanceString = distanceDouble.toStringAsFixed(1);
    }

    return Container(
      width: SizeConfig.horizontalBlock * 35,
      child: Column(children: [
        Material(
          borderRadius: BorderRadius.circular(10),
          child: InkWell(
            borderRadius: BorderRadius.circular(10),
            onTap: () {
              Navigator.pushReplacement(
                  context,
                  MaterialPageRoute(
                      builder: (_) => ProdukDiDalamPasar(
                          idPasar: dataPasar['id'],
                          namaPasar: dataPasar['nama'])));
            },
            child: Container(
                decoration: BoxDecoration(
                    image: DecorationImage(
                        fit: BoxFit.fill, image: NetworkImage(imageURL)),
                    borderRadius: BorderRadius.circular(10)),
                width: SizeConfig.horizontalBlock * 35,
                height: SizeConfig.horizontalBlock * 22.5),
          ),
        ),
        SizedBox(height: 6.5),
        SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Text(dataPasar['nama'].toUpperCase(),
                style: TextStyle(fontSize: SizeConfig.horizontalBlock * 2.75),
                textAlign: TextAlign.center)),
        (showDistance)
            ? SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: Text(distanceString + ' KM',
                    style:
                        TextStyle(fontSize: SizeConfig.horizontalBlock * 2.25),
                    textAlign: TextAlign.center))
            : SizedBox()
      ]),
    );
  }
}
