import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class JamDelivery extends StatefulWidget {
  _JamDeliveryState createState() => _JamDeliveryState();
}

class _JamDeliveryState extends State<JamDelivery> {
  bool sixNineSelected = false;
  bool nineTwelveSelected = false;
  bool thirdTeenSelected = false;

  void getJadwalDeliveryPenjual() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Penjual/getJadwalDeliveryPenjual?' + _params);
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      List jadwalDeliveryPenjual = dataObject['jadwalDeliveryPenjual'];
      if (jadwalDeliveryPenjual.length >= 1) {
        jadwalDeliveryPenjual.forEach((jadwalDelivery) {
          int jadwalDeliveryCode =
              int.parse(jadwalDelivery['jadwalDeliveryCode']);
          if (jadwalDeliveryCode == 1) {
            setState(() {
              sixNineSelected = true;
            });
          }
          if (jadwalDeliveryCode == 2) {
            setState(() {
              nineTwelveSelected = true;
            });
          }
          if (jadwalDeliveryCode == 3) {
            setState(() {
              thirdTeenSelected = true;
            });
          }
        });
      }
    }
  }

  void initState() {
    super.initState();
    getJadwalDeliveryPenjual();
  }

  toggleJadwalDelivery(String jadwalDeliveryCode, String readAbleJadwal) async {
    bool toggleStatus = false;
    Map<String, dynamic> body = {'jadwalDeliveryCode': jadwalDeliveryCode};

    String _params = await HTTPRequest.getQueryString(authToken: true);
    Map<String, dynamic> _body = await HTTPRequest.postBody(body: body);

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Penjual/toggleJadwalDelivery?' + _params);

    Response httpResponse = await post(_url, body: _body);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      toggleStatus = dataObject['toggleStatus'];
    }

    if (toggleStatus == false) {
      showDialog(
          context: context,
          builder: (context) => AlertDialog(
                title: Text('Pengaturan Jadwal Delivery',
                    style: TextStyle(fontSize: SizeConfig.horizontalBlock * 4)),
                content: Text(
                    'Gagal mengubah pengaturan jadwal delivery $readAbleJadwal'),
              ));
    }

    return toggleStatus;
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Card(
        elevation: 5,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        child: Padding(
          padding: EdgeInsets.symmetric(horizontal: 15, vertical: 10),
          child:
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text('Jam Delivery',
                style: TextStyle(
                    fontSize: SizeConfig.horizontalBlock * 4,
                    fontWeight: FontWeight.w700)),
            Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
              Text('Delivery Pagi',
                  style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3)),
              Text('Estimasi Jam 06.00 s/d 09.00 WIB',
                  style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3)),
              Checkbox(
                  materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                  value: sixNineSelected,
                  onChanged: (isSixNineSelected) async {
                    bool toggle = await toggleJadwalDelivery('1', 'Pagi');
                    if (toggle) {
                      setState(() {
                        sixNineSelected = !sixNineSelected;
                      });
                    }
                  })
            ]),
            Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
              Text('Delivery Siang',
                  style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3)),
              Text('Estimasi Jam 09.00 s/d 12.00 WIB',
                  style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3)),
              Checkbox(
                  materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                  value: nineTwelveSelected,
                  onChanged: (isNineTwelveSelected) async {
                    bool toggle = await toggleJadwalDelivery('2', 'Siang');
                    if (toggle) {
                      setState(() {
                        nineTwelveSelected = !nineTwelveSelected;
                      });
                    }
                  })
            ]),
            Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
              Text('Delivery Sore',
                  style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3)),
              Text('Estimasi Jam 13.00 s/d 15.00 WIB',
                  style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3)),
              Checkbox(
                  materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                  value: thirdTeenSelected,
                  onChanged: (isThirdteenSelected) async {
                    bool toggle = await toggleJadwalDelivery('3', 'Sore');
                    if (toggle) {
                      setState(() {
                        thirdTeenSelected = !thirdTeenSelected;
                      });
                    }
                  })
            ])
          ]),
        ));
  }
}
