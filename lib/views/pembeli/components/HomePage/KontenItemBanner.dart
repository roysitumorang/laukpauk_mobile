import 'package:flutter/material.dart';
import 'package:carousel_slider/carousel_slider.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class KontenItemBanner extends StatelessWidget {
  final Map<String, dynamic> detailKontenMaster;
  final List kontenItem;
  KontenItemBanner(this.detailKontenMaster, this.kontenItem);

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String routeTo = detailKontenMaster['route'];

    return CarouselSlider.builder(
        itemCount: kontenItem.length,
        itemBuilder: (context, index, _) {
          Map<String, dynamic> banner = kontenItem[index];

          String idPilihan = banner['pilihan'];
          String bannerImage = banner['item'];
          String keteranganBanner = banner['keterangan'];
          if (keteranganBanner.length >= 31) {
            keteranganBanner =
                keteranganBanner.substring(0, 30) + '.. read more';
          }

          String bannerURL = ServerPath.baseURL +
              '/' +
              ServerPath.uploadGambarKontenItem +
              '/' +
              bannerImage;

          return InkWell(
            onTap: () {
              Map<String, dynamic> arguments = {routeTo: idPilihan};

              Navigator.pushNamed(context, '/$routeTo', arguments: arguments);
            },
            child: ClipRRect(
              borderRadius: BorderRadius.circular(10.0),
              child: Stack(
                children: [
                  Container(
                      width: double.infinity,
                      height: double.infinity,
                      child: Image.network(bannerURL, fit: BoxFit.cover)),
                  Positioned(
                      bottom: 10.0,
                      left: 10.0,
                      child: Material(
                        borderRadius: BorderRadius.circular(50),
                        elevation: 8,
                        child: Container(
                            padding: EdgeInsets.symmetric(
                                horizontal: 15, vertical: 5),
                            decoration: BoxDecoration(
                                color: Colors.white,
                                borderRadius: BorderRadius.circular(50)),
                            child: Text(keteranganBanner,
                                style: TextStyle(
                                    fontSize:
                                        SizeConfig.horizontalBlock * 2.75))),
                      ))
                ],
              ),
            ),
          );
        },
        options: CarouselOptions(
            autoPlay: true,
            disableCenter: true,
            viewportFraction: 1,
            enableInfiniteScroll: false));
  }
}
