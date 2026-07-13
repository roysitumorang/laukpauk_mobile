import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:http/http.dart';
import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:share/share.dart';

import 'package:laukpauk/global-components/Header.dart';
import 'package:laukpauk/global-components/Footer.dart';
import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/components/Resep/ResepItem.dart';

class DetailResep extends StatefulWidget {
  final String id;
  DetailResep(this.id);

  DetailResepState createState() => DetailResepState();
}

class DetailResepState extends State<DetailResep> {
  Future<dynamic> getDetailResep() async {
    String idResep = widget.id.toString();
    String entitasResep = 'id, nama, foto, keterangan, igLink, fbLink';

    String _params =
        await HTTPRequest.getQueryString(params: 'entitasResep=$entitasResep');

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Resep/getResep/$idResep?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      dynamic dataObject = jsonDecode(httpResponse.body);
      return dataObject['dataResep'];
    }

    return FractionalOffset.centerLeft;
  }

  openLink(BuildContext context, String link) async {
    Uri _uriLink = Uri.parse(link);
    if (await canLaunchUrl(_uriLink)) {
      await launchUrl(_uriLink);
    } else {
      linkNotValid(context);
    }
  }

  void linkNotValid(BuildContext context) {
    SnackBar snackBarLinkNotValid = SnackBar(
        duration: Duration(seconds: 1), content: Text('URL Tidak Valid !'));

    ScaffoldMessenger.of(context).showSnackBar(snackBarLinkNotValid);
  }

  bagikan(BuildContext context, String namaResep) async {
    await Share.share(
        'Mak, pengen buat ${namaResep.toUpperCase()}, terus gak pakek ribet belanja, sekali klik, semua bahan resep langsung masuk keranjang belanja, ya cuma di Laukpauk lah. Lagi ada promo tuh mak. Install-kan terus apps nya di bit.ly/laukpauk',
        subject: 'Belanja Resep ${namaResep.toUpperCase()}');
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Scaffold(
        backgroundColor: CustomColors.nearlyWhite,
        body: Column(mainAxisAlignment: MainAxisAlignment.start, children: [
          Header(),
          Expanded(
            child: RefreshIndicator(
              onRefresh: () async {
                setState(() {});
              },
              child: FutureBuilder(
                  builder: (context, snapshot) {
                    if (snapshot.hasData) {
                      dynamic dataResep = snapshot.data;
                      if (dataResep != false) {
                        return ListView(padding: EdgeInsets.zero, children: [
                          Stack(children: [
                            Container(
                                height: SizeConfig.verticalBlock * 30,
                                decoration: BoxDecoration(
                                    image: DecorationImage(
                                        fit: BoxFit.cover,
                                        image: NetworkImage(ServerPath.baseURL +
                                            '/' +
                                            ServerPath.uploadGambarResep +
                                            '/' +
                                            dataResep['foto'])))),
                            Positioned(
                                left: 15,
                                bottom: 15,
                                child: Material(
                                    borderRadius: BorderRadius.circular(50),
                                    elevation: 10,
                                    child: Padding(
                                        padding: EdgeInsets.all(5),
                                        child: InkWell(
                                          onTap: () {
                                            openLink(
                                                context, dataResep['fbLink']);
                                          },
                                          child: Icon(
                                              FontAwesomeIcons.telegramPlane,
                                              size: 20),
                                        )))),
                            Positioned(
                                left: 55,
                                bottom: 15,
                                child: Material(
                                  borderRadius: BorderRadius.circular(50),
                                  elevation: 10,
                                  child: Padding(
                                    padding: EdgeInsets.all(5),
                                    child: InkWell(
                                        onTap: () {
                                          openLink(
                                              context, dataResep['igLink']);
                                        },
                                        child: Icon(FontAwesomeIcons.instagram,
                                            size: 20)),
                                  ),
                                ))
                          ]),
                          Padding(
                              padding: EdgeInsets.symmetric(horizontal: 15.0),
                              child: Column(
                                  mainAxisAlignment: MainAxisAlignment.start,
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Padding(
                                      padding: EdgeInsets.only(
                                          top: 15.0, bottom: 15.0),
                                      child: Row(
                                          mainAxisAlignment:
                                              MainAxisAlignment.spaceBetween,
                                          children: [
                                            Expanded(
                                                child: CustomButton.button(
                                                    context, 'Cara Masak', () {
                                              openLink(
                                                  context, dataResep['igLink']);
                                            },
                                                    color:
                                                        CustomButton.themeColor,
                                                    outline: true,
                                                    isPill: true)),
                                            SizedBox(width: 7.5),
                                            Expanded(
                                                child: CustomButton.button(
                                                    context, 'Share Resep', () {
                                              bagikan(
                                                  context, dataResep['nama']);
                                            },
                                                    isPill: true,
                                                    color: CustomButton
                                                        .themeColor))
                                          ]),
                                    ),
                                    Column(
                                        crossAxisAlignment:
                                            CrossAxisAlignment.start,
                                        children: [
                                          Text(dataResep['nama'],
                                              textAlign: TextAlign.left,
                                              style: TextStyle(
                                                  color: CustomColors.green,
                                                  fontSize: SizeConfig
                                                          .horizontalBlock *
                                                      4.5,
                                                  fontWeight: FontWeight.w700)),
                                          SizedBox(height: 5),
                                          Padding(
                                            padding: EdgeInsets.only(right: 10),
                                            child: Column(
                                              crossAxisAlignment:
                                                  CrossAxisAlignment.start,
                                              children: [
                                                SizedBox(height: 3),
                                                Text(dataResep['keterangan'],
                                                    style: TextStyle(
                                                        color: CustomColors
                                                            .textMuted,
                                                        fontSize: SizeConfig
                                                                .horizontalBlock *
                                                            3))
                                              ],
                                            ),
                                          ),
                                          Container(
                                              height: .5,
                                              color: CustomColors.green,
                                              margin: EdgeInsets.symmetric(
                                                  vertical: 15)),
                                          Padding(
                                            padding:
                                                EdgeInsets.only(bottom: 15.0),
                                            child: ResepItem(dataResep['id']),
                                          )
                                        ]),
                                  ]))
                        ]);
                      } else {
                        return LayoutBuilder(builder: (context, constraints) {
                          return ListView(
                            padding: EdgeInsets.zero,
                            children: [
                              Container(
                                constraints: BoxConstraints(
                                    minHeight: constraints.maxHeight),
                                child: Column(
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    children: [
                                      Image.asset(
                                          ClientPath.assetsImg + '/empty.png',
                                          width: SizeConfig.horizontalBlock *
                                              30.0),
                                      SizedBox(height: 10),
                                      Text('Detail Resep tidak Ditemukan !',
                                          style: TextStyle(
                                              color: CustomColors.textMuted,
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      3.5,
                                              fontWeight: FontWeight.w600))
                                    ]),
                              ),
                            ],
                          );
                        });
                      }
                    } else {
                      return Loading();
                    }
                  },
                  future: getDetailResep()),
            ),
          ),
        ]),
        bottomNavigationBar: Footer());
  }
}
