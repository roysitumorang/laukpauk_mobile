import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

class JadwalPengantaran extends StatefulWidget {
  final Function onJadwalDeliverySelected;
  JadwalPengantaran({required this.onJadwalDeliverySelected});

  _JadwalPengantaranState createState() => _JadwalPengantaranState();
}

class _JadwalPengantaranState extends State<JadwalPengantaran> {
  String selectedJadwalPengantaran = '';

  Future<List> getJadwalDelivery() async {
    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/JadwalDelivery/getAvailableJadwalDelivery');
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      List availableJadwalDelivery = dataObject['availableJadwalDelivery'];

      return availableJadwalDelivery;
    }

    return [];
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return FutureBuilder<List>(
        future: getJadwalDelivery(),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            if (snapshot.data != null) {
              List jadwalDelivery = snapshot.data!;
              if (jadwalDelivery.length >= 1) {
                List<Widget> jadwalDeliveryWidget =
                    jadwalDelivery.map((jadwal) {
                  bool _isFull = jadwal['isFull'];
                  bool _isOpened = jadwal['isOpened'];

                  String readAbleJadwalPengantaran =
                      jadwal['day'] + ' ' + jadwal['sessi'];
                  bool isSelected =
                      (selectedJadwalPengantaran == readAbleJadwalPengantaran)
                          ? true
                          : false;

                  TextStyle _style = TextStyle(
                      color: (_isFull || !_isOpened)
                          ? CustomColors.textMuted
                          : (isSelected)
                              ? CustomColors.nearlyWhite
                              : CustomColors.green,
                      fontSize: SizeConfig.horizontalBlock * 3,
                      fontWeight: FontWeight.w400);

                  return GestureDetector(
                    onTap: () {
                      if (!_isFull && _isOpened) {
                        setState(() {
                          selectedJadwalPengantaran = readAbleJadwalPengantaran;
                        });
                        widget.onJadwalDeliverySelected(jadwal);
                      }
                    },
                    child: Container(
                        margin: EdgeInsets.only(left: 5.0),
                        width: SizeConfig.horizontalBlock * 32.5,
                        padding:
                            EdgeInsets.symmetric(horizontal: 5, vertical: 10),
                        child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            crossAxisAlignment: CrossAxisAlignment.center,
                            children: [
                              Text(jadwal['day'],
                                  style: TextStyle(
                                      color: (_isFull || !_isOpened)
                                          ? CustomColors.textMuted
                                          : (isSelected)
                                              ? CustomColors.nearlyWhite
                                              : CustomColors.green,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5,
                                      fontWeight: FontWeight.w700)),
                              Text('Delivery ' + jadwal['sessi'],
                                  style: _style),
                              SizedBox(height: 5.0),
                              Text('Jam ' + jadwal['rentangWaktu'],
                                  textAlign: TextAlign.center,
                                  style: TextStyle(
                                      color: (_isFull || !_isOpened)
                                          ? CustomColors.textMuted
                                          : (isSelected)
                                              ? CustomColors.nearlyWhite
                                              : CustomColors.green,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 2.75)),
                              (_isFull)
                                  ? Padding(
                                      padding: EdgeInsets.only(top: 5.0),
                                      child: Text('Full',
                                          style: TextStyle(
                                              color: CustomColors.textDanger,
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      3.25)),
                                    )
                                  : SizedBox(),
                                  Padding(
                                      padding: EdgeInsets.only(top: 5.0),
                                      child: Text((_isOpened)? 'Buka' : 'Full',
                                          style: TextStyle(
                                              color:(_isOpened)? CustomColors.textSuccess:  CustomColors.textDanger,
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      3.25)),
                                    ) 
                            ]),
                        decoration: BoxDecoration(
                            color: (_isFull || !_isOpened)
                                ? CustomColors.textLightMuted
                                : (isSelected)
                                    ? CustomColors.green
                                    : CustomColors.nearlyWhite,
                            borderRadius: BorderRadius.circular(10),
                            border: Border.all(
                                color: (_isFull || !_isOpened)
                                    ? CustomColors.textLightMuted
                                    : CustomColors.green,
                                width: 1))),
                  );
                }).toList();

                return Padding(
                  padding: EdgeInsets.only(top: 10),
                  child: Container(
                    width: SizeConfig.horizontalBlock * 100,
                    child: Wrap(
                        runSpacing: 5.0,
                        alignment: WrapAlignment.center,
                        children: jadwalDeliveryWidget),
                  ),
                );
              }
            }

            return Center(child: Text('Tidak ada jadwal pengantaran'));
          } else {
            return Padding(
                padding: EdgeInsets.symmetric(vertical: 25),
                child: Center(
                    child: SizedBox(
                        child: CircularProgressIndicator(strokeWidth: 1),
                        width: 15,
                        height: 15)));
          }
        });
  }
}
