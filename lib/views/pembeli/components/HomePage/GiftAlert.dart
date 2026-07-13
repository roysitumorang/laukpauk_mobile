import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/element-class/Gift.dart';

import 'package:laukpauk/views/components/IntroPage/IntroPageIndicator.dart';

class GiftAlert extends StatefulWidget {
  final List<Gift> gifts;
  final void Function() onClose;
  const GiftAlert({required this.gifts, required this.onClose});

  @override
  State<GiftAlert> createState() => _GiftAlertState();
}

class _GiftAlertState extends State<GiftAlert> {
  int _activeGift = 1;

  @override
  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    double _fullWidth = SizeConfig.horizontalBlock * 100.0;
    double _height = SizeConfig.verticalBlock * 50.0;

    List<Gift> _listOfGifts = widget.gifts;

    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            height: _height,
            child: PageView(
                onPageChanged: (indexPage) => setState(() {
                      _activeGift = indexPage + 1;
                    }),
                children: _listOfGifts.map((giftItem) {
                  Gift _gift = giftItem;

                  String _giftFoto = _gift.foto ?? 'gift-default.png';
                  String _src =
                      ServerPath.baseURL + '/upload/gambar/gift/' + _giftFoto;

                  return Container(
                      width: _fullWidth,
                      child: Stack(
                        children: [
                          Container(
                              decoration: BoxDecoration(
                                  image: DecorationImage(
                                      image: NetworkImage(_src)))),
                          Positioned(
                              top: 10.0,
                              right: SizeConfig.horizontalBlock * 20.0,
                              child: GestureDetector(
                                onTap: () => widget.onClose(),
                                child: Container(
                                  decoration: BoxDecoration(
                                      borderRadius: BorderRadius.circular(25.0),
                                      color: Colors.white,
                                      boxShadow: [
                                        BoxShadow(
                                            spreadRadius: 7.25,
                                            blurRadius: 20.0,
                                            color: Color(0XFF6c757d))
                                      ]),
                                  child: Icon(Icons.close, color: Colors.black),
                                ),
                              ))
                        ],
                      ));
                }).toList()),
          ),
          SizedBox(height: 25.0),
          Center(
              child: IntroPageIndicator(
                  activePage: _activeGift,
                  indicatorLength: _listOfGifts.length))
        ],
      ),
    );
  }
}
