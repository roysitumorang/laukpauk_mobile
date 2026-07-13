import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';

import 'package:laukpauk/views/pembeli/ProdukDiDalamPasar.dart';

class Pasar extends StatelessWidget {
  final Map dataPasar;
  final bool showDistance;
  Pasar(this.dataPasar, {this.showDistance = true});

  Widget build(BuildContext context) {
    String? distanceString = dataPasar['distance'];
    double? distance =
        (distanceString != null) ? double.parse(distanceString) : null;

    return ClipRRect(
      borderRadius: BorderRadius.circular(12.5),
      child: Container(
        decoration: BoxDecoration(
            border: Border.all(color: CustomColors.nearlyWhite),
            borderRadius: BorderRadius.circular(5)),
        child: Material(
          elevation: 5,
          shadowColor: CustomColors.aliceBlue,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(5)),
          child: InkWell(
            onTap: () {
              Navigator.push(
                  context,
                  MaterialPageRoute(
                      builder: (_) => ProdukDiDalamPasar(
                          idPasar: dataPasar['id'],
                          namaPasar: dataPasar['nama'])));
            },
            child: Container(
                decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(5),
                    image: DecorationImage(
                        fit: BoxFit.cover,
                        image: NetworkImage(ServerPath.baseURL +
                            '/' +
                            ServerPath.uploadGambarPasar +
                            '/compress/' +
                            dataPasar['gambar']))),
                padding: EdgeInsets.all(0),
                child: Stack(children: [
                  Positioned(
                      top: 0,
                      bottom: 0,
                      left: 0,
                      right: 0,
                      child: Container(
                          width: double.infinity,
                          height: double.infinity,
                          decoration: BoxDecoration(
                              color: Color.fromRGBO(0, 0, 0, 0.3)))),
                  Padding(
                    padding: EdgeInsets.all(5),
                    child: Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          SingleChildScrollView(
                            scrollDirection: Axis.horizontal,
                            child: Text(dataPasar['nama'],
                                style: TextStyle(
                                    color: Colors.white,
                                    fontWeight: FontWeight.w700),
                                textAlign: TextAlign.center),
                          ),
                          (showDistance && distance != null)
                              ? SizedBox(height: 7)
                              : SizedBox(),
                          (showDistance && distance != null)
                              ? Text(distance.toStringAsFixed(1) + ' KM',
                                  style: TextStyle(
                                      color: Colors.white,
                                      fontWeight: FontWeight.w700,
                                      fontSize: 13),
                                  textAlign: TextAlign.center)
                              : SizedBox()
                        ],
                      ),
                    ),
                  ),
                ])),
          ),
        ),
      ),
    );
  }
}
