import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/views/SplashScreen.dart';
import 'package:shared_preferences/shared_preferences.dart';

class IntroPageItem extends StatelessWidget {
  final BuildContext context;
  final Map<String, dynamic> dataPage;
  final int indexPage;
  IntroPageItem(this.context, this.indexPage, this.dataPage);

  void startApp() async {
    SharedPreferences _sP = await SharedPreferences.getInstance();

    _sP.setBool('isAppStarted', true);

    Navigator.push(context, MaterialPageRoute(builder: (_) => SplashScreen()));
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Stack(
      children: [
        Center(
          child: Container(
            decoration: BoxDecoration(
                image: DecorationImage(
                    fit: BoxFit.fill, image: AssetImage(dataPage['img']))),
          ),
        ),
        (indexPage == 1)
            ? Positioned(
                bottom: SizeConfig.horizontalBlock * 22.5,
                child: Container(
                  width: SizeConfig.horizontalBlock * 100.0,
                  child: Center(
                      child: CustomButton.button(context, 'Ke Pasar Sekarang',
                          () {
                    startApp();
                  },
                          outline: true,
                          isPill: true,
                          color: CustomButton.themeColor)),
                ),
              )
            : SizedBox()
      ],
    );
  }
}
