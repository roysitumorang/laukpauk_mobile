import 'package:flutter/material.dart';
import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/CustomText.dart';
import 'package:url_launcher/url_launcher.dart';

import 'package:laukpauk/global-components/CustomAlert.dart';

import 'package:laukpauk/models/SettingModel.dart';

import 'package:laukpauk/libraries/Currency.dart';

import 'package:laukpauk/views/pembeli/HubungiKami.dart';
import 'package:laukpauk/views/pembeli/KodeReferal.dart';

class UserMenu extends StatelessWidget {
  Future<int> _getNilaiDompetReferral() async {
    dynamic _settingValue =
        await SettingModel.getSettingValue('referralWallet');
    if (_settingValue != false) {
      int _nilaiDompetReferral;
      if (_settingValue.runtimeType == String) {
        _nilaiDompetReferral = int.parse(_settingValue);
      } else {
        _nilaiDompetReferral = _settingValue;
      }

      return _nilaiDompetReferral;
    }

    return 0;
  }

  final TextStyle _title = CustomText.textSM()
      .copyWith(fontWeight: FontWeight.bold, color: CustomColors.grey);

  final TextStyle _subTitle = CustomText.textXS();

  Widget build(BuildContext context) {
    return FutureBuilder<int>(
        future: _getNilaiDompetReferral(),
        builder: (futureBuilderContext, snapshot) {
          if (snapshot.hasData) {
            if (snapshot.data != null) {
              int _nilaiDompet = snapshot.data!;
              return Container(
                padding: EdgeInsets.all(15.0),
                child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Menu Lainnya',
                          style: CustomText.textMD()
                              .copyWith(fontWeight: FontWeight.bold)),
                      SizedBox(height: 15.0),
                      Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Expanded(
                              child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text('Kode Referal', style: _title),
                                    SizedBox(height: 3),
                                    Text(
                                        'Bagikan kode ini kepada siapa saja dan dapatkan uang belanja gratis ' +
                                            Currency.formatRupiah(
                                                _nilaiDompet, 0),
                                        style: _subTitle)
                                  ]),
                            ),
                            SizedBox(width: 15),
                            InkWell(
                              onTap: () {
                                Navigator.push(
                                    context,
                                    MaterialPageRoute(
                                        builder: (_) => KodeReferal()));
                              },
                              child: Icon(Icons.arrow_forward_ios_outlined,
                                  size: 20.0),
                            )
                          ]),
                      SizedBox(height: 10.0),
                      Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text('Privasi dan Kebijakan', style: _title),
                            SizedBox(width: 15),
                            InkWell(
                              onTap: () async {
                                String _url =
                                    await SettingModel.getSettingValue(
                                        'kebijakanPrivasiURL');
                                Uri _uri = Uri.parse(_url);
                                bool _canLaunch = await canLaunchUrl(_uri);

                                if (_canLaunch) {
                                  await launchUrl(_uri);
                                } else {
                                  CustomAlert.urlCantBeLaunched(context,
                                      url: _url);
                                }
                              },
                              child: Icon(Icons.arrow_forward_ios_outlined,
                                  size: 20.0),
                            )
                          ]),
                      SizedBox(height: 10.0),
                      Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text('Hubungi Kami', style: _title),
                            SizedBox(width: 15),
                            InkWell(
                              onTap: () {
                                Navigator.push(
                                    context,
                                    MaterialPageRoute(
                                        builder: (_) => HubungiKami()));
                              },
                              child: Icon(Icons.arrow_forward_ios_outlined,
                                  size: 20.0),
                            )
                          ])
                    ]),
              );
            }
          }
          return Loading();
        });
  }
}
