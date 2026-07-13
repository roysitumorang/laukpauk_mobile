import 'package:flutter/material.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

class IntroPageIndicator extends StatelessWidget {
  final int indicatorLength, activePage;
  IntroPageIndicator({required this.indicatorLength, required this.activePage});
  Widget build(BuildContext context) {
    List<AnimatedContainer> _indicator = [];

    for (int i = 1; i <= indicatorLength; i++) {
      double _widthAndHeight = 10.0;
      Color _color = CustomColors.nearlyWhite;
      if (activePage == i) {
        _widthAndHeight = 15.0;
        _color = CustomColors.green;
      }
      AnimatedContainer _item = AnimatedContainer(
          duration: Duration(milliseconds: 250),
          width: _widthAndHeight,
          height: _widthAndHeight,
          margin: EdgeInsets.only(right: 5.0),
          decoration: BoxDecoration(
              color: _color,
              borderRadius:
                  BorderRadius.all(Radius.circular(_widthAndHeight))));
      _indicator.add(_item);
    }
    return Row(mainAxisSize: MainAxisSize.min, children: _indicator);
  }
}
