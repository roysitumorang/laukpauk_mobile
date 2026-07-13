import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:url_launcher/url_launcher.dart';
import 'package:http/http.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

class HubungiKami extends StatefulWidget {
  HubungiKamiState createState() => HubungiKamiState();
}

class HubungiKamiState extends State<HubungiKami> {
  Future<dynamic> getContactUsWhatsappConfiguration() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Pengaturan/getContactUsWhatsappConfiguration?' +
        _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = json.decode(httpResponse.body);
      return dataObject['contactUsWhatsappConfiguration'];
    }
    return false;
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Scaffold(
        appBar: AppBar(
            title: Text('Hubungi Kami'), backgroundColor: CustomColors.green),
        body: Container(
            height: MediaQuery.of(context).size.height,
            padding: EdgeInsets.symmetric(horizontal: 5, vertical: 5),
            child: FutureBuilder(
                future: getContactUsWhatsappConfiguration(),
                builder: (context, snapshot) {
                  if (snapshot.hasData) {
                    dynamic contactUsWhatsappConfiguration = snapshot.data;
                    if (contactUsWhatsappConfiguration != false) {
                      return Align(
                        alignment: Alignment.center,
                        child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Image.asset(
                                  ClientPath.assetsImg + '/hubungi-kami.png',
                                  width: SizeConfig.horizontalBlock * 30),
                              SizedBox(height: 5),
                              Text('Ada yang bisa kami bantu Mak ?',
                                  style: TextStyle(
                                      color: CustomColors.green,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5,
                                      fontWeight: FontWeight.w700)),
                              SizedBox(height: 25),
                              CustomButton.button(
                                  context, 'Chat Kami dari Whatsapp', () {
                                        Uri _uri = Uri.parse('https://wa.me/62618367205');
                                launchUrl(_uri, mode: LaunchMode.externalApplication);
                              },
                                  isPill: true,
                                  outline: true,
                                  color: CustomButton.themeColor)
                            ]),
                      );
                    } else {
                      return Column(children: [
                        Image.asset(ClientPath.assetsImg + '/empty.png',
                            width: SizeConfig.horizontalBlock * 30),
                        SizedBox(height: 10),
                        Text(
                            'Mohon maaf, server kami gagal mengambil data nomor telepon yang dapat anda gunakan untuk menghubungi Laukpauk. Silahkan cari informasi di laman web kami.',
                            style: TextStyle(
                                color: CustomColors.textDanger,
                                fontSize: SizeConfig.horizontalBlock * 3.5)),
                        SizedBox(height: 10),
                      ]);
                    }
                  } else {
                    return Center(
                        child: SizedBox(
                            width: 25,
                            height: 25,
                            child:
                                CircularProgressIndicator(strokeWidth: 1.5)));
                  }
                })));
  }
}
