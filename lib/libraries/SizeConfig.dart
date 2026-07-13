import 'package:flutter/material.dart';

class SizeConfig {
  static MediaQueryData? _mediaQueryData;
  static double screenWidth = 0.0;
  static double screenHeight = 0.0;
  static double horizontalBlock = 0.0;
  static double verticalBlock = 0.0;

  void initSize(BuildContext context) {
    _mediaQueryData = MediaQuery.of(context);

    if (_mediaQueryData != null) {
      screenWidth = _mediaQueryData!.size.width;
      screenHeight = _mediaQueryData!.size.height;
      horizontalBlock = screenWidth / 100;
      verticalBlock = screenHeight / 100;
    }
  }
}
