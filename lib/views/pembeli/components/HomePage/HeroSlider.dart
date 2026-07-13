import 'package:flutter/material.dart';
import 'package:carousel_slider/carousel_slider.dart';
import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/models/BannerModel.dart';

class HeroSlider extends StatefulWidget {
  HeroSliderState createState() => HeroSliderState();
}

class HeroSliderState extends State<HeroSlider> {
  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Container(
      width: SizeConfig.horizontalBlock * 100,
      height: SizeConfig.horizontalBlock * 60,
      child: FutureBuilder<List>(
          future: BannerModel.getBanner(),
          builder: (context, snapshot) {
            if (snapshot.hasData) {
              if (snapshot.data != null) {
                List banners = snapshot.data!;

                return CarouselSlider.builder(
                    itemCount: banners.length,
                    itemBuilder: (context, index, _) {
                      dynamic banner = banners[index];
                      bool _isImageNull =
                          banner['gambar'].toString().length <= 1;

                      String bannerImg = banner['gambar'];
                      String bannerURL = ServerPath.baseURL +
                          '/' +
                          ServerPath.uploadGambarBanner +
                          '/' +
                          bannerImg;

                      String bannerName = banner['nama'];
                      String bannerDesc = banner['keterangan'];

                      return Stack(
                        children: [
                          Container(
                              width: SizeConfig.horizontalBlock * 100,
                              height: SizeConfig.horizontalBlock * 100,
                              child: (_isImageNull)
                                  ? Image.asset(
                                      ClientPath.assetsImg +
                                          '/no-image-img.png',
                                      fit: BoxFit.fitWidth)
                                  : Image.network(bannerURL,
                                      fit: BoxFit.cover)),
                          Positioned(
                              bottom: 10.0,
                              left: 10.0,
                              right: 10.0,
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(bannerName,
                                      style: TextStyle(
                                          color: CustomColors.nearlyWhite,
                                          fontSize:
                                              SizeConfig.horizontalBlock * 3.25,
                                          fontWeight: FontWeight.w500)),
                                  SizedBox(
                                      height:
                                          (bannerDesc.length >= 1) ? 2.5 : 0.0),
                                  (bannerDesc.length >= 1)
                                      ? Text(bannerDesc,
                                          style: TextStyle(
                                              color: CustomColors.nearlyWhite,
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      2.75))
                                      : SizedBox()
                                ],
                              ))
                        ],
                      );
                    },
                    options: CarouselOptions(
                        autoPlay: true,
                        disableCenter: true,
                        viewportFraction: 1,
                        enableInfiniteScroll: false));
              }
            }
            return Loading();
          }),
    );
  }
}

/*
 */
