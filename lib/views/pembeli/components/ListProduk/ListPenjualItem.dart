import 'package:flutter/material.dart';

import 'package:laukpauk/views/Penjual.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/DefaultImage.dart';

class ListPenjualItem extends StatelessWidget {
  final Map<String, dynamic> dataPenjual;
  final double? widgetWidth;
  final void Function() deteksiADJD;
  ListPenjualItem(this.dataPenjual, this.deteksiADJD, {this.widgetWidth});

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String idPenjual = dataPenjual['id'];
    String bannerToko = dataPenjual['bannerToko'];
    String namaToko = dataPenjual['namaToko'];
    String alamatToko = dataPenjual['alamatToko'];

    bool isDefaultImage = (bannerToko == DefaultImage.defautBannerPenjual);

    return Container(
        width: widgetWidth,
        child: InkWell(
          onTap: () {
            Navigator.push(context, MaterialPageRoute(builder: (_) {
              return Penjual(id: idPenjual);
            }));
          },
          child: Card(
              margin: EdgeInsets.only(bottom: 10, left: 2.5, right: 2.5),
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10)),
              child: Column(
                children: [
                  Expanded(
                    child: LayoutBuilder(builder: (context, constraints) {
                      return Container(
                          margin: EdgeInsets.only(bottom: 15.0),
                          constraints: BoxConstraints(
                              minHeight: constraints.maxHeight,
                              minWidth: constraints.maxWidth),
                          decoration: BoxDecoration(
                              borderRadius: BorderRadius.only(
                                  topLeft: Radius.circular(10.0),
                                  topRight: Radius.circular(10.0)),
                              image: DecorationImage(
                                  image: NetworkImage(ServerPath.baseURL +
                                      '/' +
                                      ServerPath.uploadGambarBannerPenjual +
                                      '/compress/' +
                                      bannerToko),
                                  fit: (isDefaultImage)
                                      ? BoxFit.contain
                                      : BoxFit.cover)));
                    }),
                  ),
                  Expanded(
                    child: SingleChildScrollView(
                      child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Padding(
                              padding: EdgeInsets.symmetric(horizontal: 15.0),
                              child: Text(namaToko,
                                  overflow: TextOverflow.ellipsis,
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5,
                                      fontWeight: FontWeight.w700),
                                  textAlign: TextAlign.center),
                            ),
                            SizedBox(height: 2),
                            Padding(
                              padding: EdgeInsets.symmetric(horizontal: 15.0),
                              child: InkWell(
                                onTap: () {
                                  Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                          builder: (_) =>
                                              Penjual(id: idPenjual)));
                                },
                                child: Text(
                                    (namaToko.length >= 1)
                                        ? namaToko
                                        : 'Penjual Tidak Diketahui',
                                    textAlign: TextAlign.center,
                                    style: TextStyle(
                                        color: CustomColors.textMuted,
                                        fontSize:
                                            SizeConfig.horizontalBlock * 2.75)),
                              ),
                            ),
                            SizedBox(height: 10.0),
                            Padding(
                                padding: EdgeInsets.symmetric(horizontal: 10.0),
                                child: Text(alamatToko,
                                    textAlign: TextAlign.center,
                                    style: TextStyle(
                                        fontSize:
                                            SizeConfig.horizontalBlock * 3.25,
                                        color: CustomColors.textMuted)))
                          ]),
                    ),
                  ),
                ],
              )),
        ));
  }
}
