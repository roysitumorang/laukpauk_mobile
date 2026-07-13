import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

class Yuhu extends StatelessWidget {
  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Container(
      margin: EdgeInsets.only(top: 15),
      decoration: BoxDecoration(
          color: Color(0x5566CC99), borderRadius: BorderRadius.circular(8)),
      padding: EdgeInsets.symmetric(horizontal: 25, vertical: 15.0),
      child: Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
        Image.asset(ClientPath.assetsImg + '/icons/timbangan.png',
            width: SizeConfig.horizontalBlock * 10.0),
        SizedBox(width: 20.0),
        Expanded(
          child:
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text('Yuhuuu…',
                style: TextStyle(
                    color: CustomColors.nearlyBlack,
                    fontSize: SizeConfig.horizontalBlock * 3.5,
                    fontWeight: FontWeight.w700)),
            SizedBox(height: 3.25),
            Text('Akan kami hubungi jika ada selisih timbangan ya…',
                style: TextStyle(
                    color: CustomColors.nearlyBlack,
                    fontSize: SizeConfig.horizontalBlock * 3.0))
          ]),
        )
      ]),
    );
  }
}
