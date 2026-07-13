import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/CustomText.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class Loading extends StatelessWidget {
  final bool showLoadingImage, showCircularProgressIndicator;
  final String? title, subTitle;
  final Size? circularProgressIndicatorSize;
  Loading(
      {this.showLoadingImage = false,
      this.showCircularProgressIndicator = true,
      this.title,
      this.subTitle,
      this.circularProgressIndicatorSize});
  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    double cPIHeight = 50.0;
    double cPIWidth = 50.0;

    if (circularProgressIndicatorSize != null) {
      cPIHeight = circularProgressIndicatorSize!.height;
      cPIWidth = circularProgressIndicatorSize!.width;
    }

    double _paddingSpace = 7.5;

    return Center(
      child: Padding(
        padding: EdgeInsets.symmetric(vertical: cPIHeight),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Stack(
              children: [
                SizedBox(
                    height: cPIHeight,
                    width: cPIWidth,
                    child: CircularProgressIndicator(
                        valueColor:
                            AlwaysStoppedAnimation<Color>(CustomColors.green),
                        strokeWidth: 2.5)),
                Positioned(
                  top: _paddingSpace,
                  left: _paddingSpace,
                  bottom: _paddingSpace,
                  right: _paddingSpace,
                  child: SizedBox(
                      height: cPIHeight - _paddingSpace,
                      width: cPIWidth - _paddingSpace,
                      child: Image.asset(
                          ClientPath.assetsImg + '/laukpauk-icon.png')),
                )
              ],
            ),
            (title != null)
                ? Padding(
                    padding: const EdgeInsets.only(top: 25.0),
                    child: Text(title!,
                        style: CustomText.textMD()
                            .copyWith(fontWeight: FontWeight.bold)),
                  )
                : SizedBox(),
            (subTitle != null)
                ? Padding(
                    padding: const EdgeInsets.only(top: 10.0),
                    child: Text(subTitle!,
                        style: CustomText.textXS()
                            .copyWith(color: CustomColors.textMuted)),
                  )
                : SizedBox()
          ],
        ),
      ),
    );
  }
}
