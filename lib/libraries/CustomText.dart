import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

class CustomText {
  CustomText(BuildContext buildContext) {
    SizeConfig().initSize(buildContext);
  }

  static TextStyle headline1(
      {Color? color,
      FontWeight? fontWeight,
      FontStyle? fontStyle,
      TextDecoration? decoration}) {
    return TextStyle(
        fontSize: SizeConfig.horizontalBlock * 5.50,
        color: color,
        fontWeight: fontWeight,
        fontStyle: fontStyle,
        decoration: decoration);
  }

  static TextStyle headline2(
      {Color? color,
      FontWeight? fontWeight,
      FontStyle? fontStyle,
      TextDecoration? decoration}) {
    return TextStyle(
        fontSize: SizeConfig.horizontalBlock * 5.0,
        color: color,
        fontWeight: fontWeight,
        fontStyle: fontStyle,
        decoration: decoration);
  }

  static TextStyle headline3(
      {Color? color,
      FontWeight? fontWeight,
      FontStyle? fontStyle,
      TextDecoration? decoration}) {
    return TextStyle(
        fontSize: SizeConfig.horizontalBlock * 4.75,
        color: color,
        fontWeight: fontWeight,
        fontStyle: fontStyle,
        decoration: decoration);
  }

  static TextStyle headline4(
      {Color? color,
      FontWeight? fontWeight,
      FontStyle? fontStyle,
      TextDecoration? decoration}) {
    return TextStyle(
        fontSize: SizeConfig.horizontalBlock * 4.50,
        color: color,
        fontWeight: fontWeight,
        fontStyle: fontStyle,
        decoration: decoration);
  }

  static TextStyle headline5(
      {Color? color,
      FontWeight? fontWeight,
      FontStyle? fontStyle,
      TextDecoration? decoration}) {
    return TextStyle(
        fontSize: SizeConfig.horizontalBlock * 4.25,
        color: color,
        fontWeight: fontWeight,
        fontStyle: fontStyle,
        decoration: decoration);
  }

  static TextStyle headline6(
      {Color? color,
      FontWeight? fontWeight,
      FontStyle? fontStyle,
      TextDecoration? decoration}) {
    return TextStyle(
        fontSize: SizeConfig.horizontalBlock * 4.0,
        color: color,
        fontWeight: fontWeight,
        fontStyle: fontStyle,
        decoration: decoration);
  }

  static TextStyle contentSize(
      {Color? color,
      FontWeight? fontWeight,
      FontStyle? fontStyle,
      TextDecoration? decoration}) {
    return TextStyle(
        fontSize: SizeConfig.horizontalBlock * 3.25,
        color: color,
        fontWeight: fontWeight,
        fontStyle: fontStyle,
        decoration: decoration);
  }

  static TextStyle hargaCoret() {
    return TextStyle(
        decoration: TextDecoration.lineThrough,
        fontSize: SizeConfig.horizontalBlock * 3.0,
        color: CustomColors.textLightMuted,
        fontWeight: FontWeight.w700);
  }

  static TextStyle hargaBersih() {
    return TextStyle(
        fontSize: SizeConfig.horizontalBlock * 3.75,
        color: CustomColors.green,
        fontWeight: FontWeight.w700);
  }

  static TextStyle textXS() {
    return TextStyle(
        fontSize: SizeConfig.horizontalBlock * 3.0,
        color: CustomColors.nearlyBlack);
  }

  static TextStyle textSM() {
    return TextStyle(fontSize: SizeConfig.horizontalBlock * 3.75);
  }

  static TextStyle textMD() {
    return TextStyle(fontSize: SizeConfig.horizontalBlock * 4.25);
  }

  static TextStyle textLG() {
    return TextStyle(fontSize: SizeConfig.horizontalBlock * 5.5);
  }
}
