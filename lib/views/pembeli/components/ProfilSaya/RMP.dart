import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:url_launcher/url_launcher.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

class RMP extends StatelessWidget {
  final Map<String, dynamic> dataPenjual;
  RMP(this.dataPenjual);

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

    String namaToko = dataPenjual['namaToko'];
    String statusApprove = dataPenjual['status'];
    String statusApproveLC = statusApprove.toLowerCase();
    String imgURL = (statusApproveLC == 'pending')
        ? ClientPath.assetsImg + '/RMP_Pending.png'
        : ClientPath.assetsImg + '/RMP_Rejected.png';
    String text = (statusApproveLC == 'pending')
        ? 'Harap tunggu sampai admin menyetujui request anda menjadi penjual'
        : 'Mohon maaf, permintaan anda menjadi penjual tidak diizinkan oleh admin karena hal tertentu.';

    String statusReason =
        (statusApproveLC == 'rejected') ? dataPenjual['statusReason'] : '';

    TextStyle textStyle = (statusApproveLC == 'pending')
        ? TextStyle(
            fontWeight: FontWeight.w400,
            color: CustomColors.textMuted,
            fontSize: SizeConfig.horizontalBlock * 3.50)
        : TextStyle(
            fontWeight: FontWeight.w400,
            color: CustomColors.textDanger,
            fontSize: SizeConfig.horizontalBlock * 3.50);

    return Padding(
      padding: EdgeInsets.symmetric(vertical: 15, horizontal: 15),
      child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [
        Image.asset(imgURL, width: SizeConfig.horizontalBlock * 30),
        SizedBox(height: 25),
        Text('Hai, Penjual ' + namaToko,
            style: TextStyle(
                color: CustomColors.nearlyBlack,
                fontSize: SizeConfig.horizontalBlock * 5,
                fontWeight: FontWeight.w700)),
        SizedBox(height: 10),
        Text(text, style: textStyle, textAlign: TextAlign.center),
        (statusApproveLC == 'rejected')
            ? Container(
                margin: EdgeInsets.only(top: 5),
                child: Text(statusReason,
                    textAlign: TextAlign.center,
                    style: TextStyle(
                        fontSize: SizeConfig.horizontalBlock * 3,
                        color: CustomColors.textMuted)))
            : SizedBox(),
        SizedBox(height: 25),
        FutureBuilder(
            future: getContactUsWhatsappConfiguration(),
            builder: (context, snapshot) {
              if (snapshot.hasData) {
                dynamic contactUsWhatsappConfiguration = snapshot.data;
                if (contactUsWhatsappConfiguration != false) {
                  return CustomButton.button(
                      context, 'Tanya Kami Melalui Whatsapp', () {
                    Uri _uri = Uri.parse('https://wa.me/' +
                        contactUsWhatsappConfiguration['contactUsWhatsapp']);
                    launchUrl(_uri);
                  },
                      color: CustomButton.infoColor,
                      outline: true,
                      isPill: true);
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
                        width: 15,
                        height: 15,
                        child: CircularProgressIndicator(strokeWidth: 1.5)));
              }
            })
      ]),
    );
  }
}
