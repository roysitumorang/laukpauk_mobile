import 'package:flutter/material.dart';
import 'package:carousel_slider/carousel_slider.dart';
import 'package:laukpauk/global-components/Loading.dart';
import 'package:url_launcher/url_launcher.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/models/HomepageModel.dart';
import 'package:laukpauk/models/SettingModel.dart';

class Artikel extends StatefulWidget {
  ArtikelState createState() => ArtikelState();
}

class ArtikelState extends State<Artikel> {
  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
            padding: EdgeInsets.only(bottom: 15),
            child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('Artikel',
                      textAlign: TextAlign.left,
                      style: TextStyle(
                          fontWeight: FontWeight.w500,
                          fontSize: SizeConfig.horizontalBlock * 4.0)),
                  InkWell(
                    onTap: () async {
                      String urlLihatSemua = await SettingModel.getSettingValue(
                          'seeAllArticleURL');
                      Uri urlLihatSemuaURI = Uri.parse(urlLihatSemua);
                      if (await canLaunchUrl(urlLihatSemuaURI)) {
                        await launchUrl(urlLihatSemuaURI,
                            webViewConfiguration: WebViewConfiguration());
                      }
                    },
                    child: Text('Lihat Semua',
                        textAlign: TextAlign.right,
                        style: TextStyle(
                            color: CustomColors.darkGreen,
                            fontWeight: FontWeight.w500,
                            fontSize: SizeConfig.horizontalBlock * 3.0)),
                  )
                ])),
        Container(
          height: SizeConfig.verticalBlock * 22.5,
          width: SizeConfig.horizontalBlock * 100,
          decoration: BoxDecoration(borderRadius: BorderRadius.circular(10)),
          child: FutureBuilder<List>(
              future: HomepageModel.getArtikel(),
              builder: (context, snapshot) {
                if (snapshot.hasData) {
                  if (snapshot.data != null) {
                    List banners = snapshot.data!;
                    if (banners.length >= 1) {
                      return CarouselSlider.builder(
                          itemCount: banners.length,
                          itemBuilder: (context, index, _) {
                            dynamic banner = banners[index];
                            String bannerURL = ServerPath.baseURL +
                                '/' +
                                ServerPath.uploadGambarArtikel +
                                '/' +
                                banner['foto'];

                            String _judulArtikel = banners[index]['judul'];
                            if (_judulArtikel.length >= 31) {
                              _judulArtikel = _judulArtikel.substring(0, 30) +
                                  ' .. read more';
                            }

                            return InkWell(
                              onTap: () async {
                                Uri url = Uri.parse(banner['link']);

                                if (await canLaunchUrl(url)) {
                                  launchUrl(url,
                                      webViewConfiguration:
                                          WebViewConfiguration());
                                } else {
                                  showDialog(
                                      context: context,
                                      builder: (context) => AlertDialog(
                                              title: Text('Lauk Pauk',
                                                  style: TextStyle(
                                                      fontSize: SizeConfig
                                                              .horizontalBlock *
                                                          4)),
                                              content: Text(
                                                  'URL tidak bisa diakses !',
                                                  style: TextStyle(
                                                      fontSize: SizeConfig
                                                              .horizontalBlock *
                                                          3.5)),
                                              actions: <TextButton>[
                                                TextButton(
                                                    onPressed: () {
                                                      Navigator.pop(context);
                                                    },
                                                    child: Text('OK'))
                                              ]));
                                }
                              },
                              child: Stack(
                                children: [
                                  Container(
                                      width: double.infinity,
                                      decoration: BoxDecoration(
                                          borderRadius:
                                              BorderRadius.circular(10),
                                          image: DecorationImage(
                                              image: NetworkImage(bannerURL),
                                              fit: BoxFit.cover))),
                                  Positioned(
                                      bottom: 10,
                                      left: 10,
                                      child: Material(
                                        borderRadius: BorderRadius.circular(50),
                                        elevation: 8,
                                        child: Container(
                                            padding: EdgeInsets.symmetric(
                                                horizontal: 15, vertical: 5),
                                            decoration: BoxDecoration(
                                                color: Colors.white,
                                                borderRadius:
                                                    BorderRadius.circular(50)),
                                            child: Text(_judulArtikel,
                                                style: TextStyle(
                                                    fontSize: SizeConfig
                                                            .horizontalBlock *
                                                        2.75))),
                                      ))
                                ],
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

                  return Container(
                    width: double.infinity,
                    child: Column(
                        crossAxisAlignment: CrossAxisAlignment.center,
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Image.asset(ClientPath.assetsImg + '/empty.png',
                              width: SizeConfig.horizontalBlock * 25),
                          SizedBox(height: 10),
                          Text('Tidak ada artikel yang bisa dibaca',
                              style: TextStyle(
                                  color: CustomColors.textMuted,
                                  fontSize: SizeConfig.horizontalBlock * 3.5))
                        ]),
                  );
                } else {
                  return Loading();
                }
              }),
        )
      ],
    );
  }
}

/*
 */
