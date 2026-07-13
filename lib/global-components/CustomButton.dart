import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

class CustomButton {
  static Color themeColor = Color(0xFF66CC99);
  static Color defaultColor = Color(0xFFe9ecef);
  static Color primaryColor = Color(0xFF0069d9);
  static Color secondaryColor = Color(0xFF5a6268);
  static Color successColor = Color(0xFF218838);
  static Color infoColor = Color(0xFF138496);
  static Color dangerColor = Color(0xFFc82333);
  static Color warningColor = Color(0xFFe0a800);

  static String lgSize = 'lg'; //+2.5
  static String mdSize = 'md';
  static String smSize = 'sm'; //-2.5
  static String xsSize = 'xs'; //-5

  static Widget button(BuildContext context, String text, void Function() onTap,
      {Color? color,
      Color? borderColor,
      double elevation = 0.0,
      bool isBlock = false,
      bool isBusy = false,
      bool outline = false,
      bool isPill = false,
      String? size}) {
    SizeConfig().initSize(context);

    borderColor = (borderColor == null) ? Color(0x55e9ecef) : borderColor;
    color = (color == null) ? Color(0xFFe9ecef) : color;
    size = (size == null) ? mdSize : size;

    double pillCorner = SizeConfig.horizontalBlock * 7.5; //ukuran md
    double paddingTopAndBottom = SizeConfig.horizontalBlock * 2.75; //ukuran md
    double paddingLeftAndRight = SizeConfig.horizontalBlock * 7.5;
    double fontSize = SizeConfig.horizontalBlock * 3.5; //ukuran md

    if (size == 'lg') {
      paddingLeftAndRight = paddingLeftAndRight + 2.5;
      paddingTopAndBottom = paddingTopAndBottom + 1.25;
      fontSize = fontSize + 5;
    }

    if (size == 'sm') {
      paddingLeftAndRight = paddingLeftAndRight - 5;
      paddingTopAndBottom = paddingTopAndBottom - 1.5;
      fontSize = fontSize - 2.5;
    }

    if (size == 'xs') {
      paddingLeftAndRight = paddingLeftAndRight - 7.5;
      paddingTopAndBottom = paddingTopAndBottom - 2.75;
      fontSize = fontSize - 3.5;
    }

    return Container(
      decoration: BoxDecoration(
          borderRadius: BorderRadius.circular((isPill) ? pillCorner : 7.5),
          border: Border.all(width: 1, color: (outline) ? color : borderColor)),
      child: Material(
        color: (outline) ? CustomColors.nearlyWhite : color,
        elevation: elevation,
        borderRadius: BorderRadius.circular((isPill) ? pillCorner : 7.5),
        child: InkWell(
            borderRadius: BorderRadius.circular((isPill) ? pillCorner : 7.5),
            splashColor: (outline) ? color : Colors.white,
            onTap: (isBusy) ? null : onTap,
            child: Padding(
                padding: EdgeInsets.only(
                    top: paddingTopAndBottom,
                    bottom: paddingTopAndBottom,
                    left: paddingLeftAndRight,
                    right: paddingLeftAndRight),
                child: (isBusy)
                    ? Center(
                        child: SizedBox(
                            width: 15,
                            height: 15,
                            child: CircularProgressIndicator(
                                strokeWidth: 1.5,
                                backgroundColor: (outline)
                                    ? color
                                    : CustomColors.nearlyWhite,
                                valueColor: AlwaysStoppedAnimation<Color>(
                                    (outline)
                                        ? CustomColors.nearlyWhite
                                        : color))))
                    : Container(
                        width: (isBlock) ? double.infinity : null,
                        child: Text(text,
                            textAlign: TextAlign.center,
                            style: TextStyle(
                                color: (outline) ? color : Colors.white,
                                fontSize: fontSize)),
                        decoration: BoxDecoration(
                          borderRadius: BorderRadius.circular(25),
                        )))),
      ),
    );
  }
}
