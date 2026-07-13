import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/views/components/IntroPage/IntroPageIndicator.dart';

import 'package:laukpauk/views/components/IntroPage/IntroPageItem.dart';

class IntroPage extends StatefulWidget {
  State<IntroPage> createState() => _IntroPageState();
}

class _IntroPageState extends State<IntroPage> {
  final List<Map<String, String>> introPageData = [
    {'img': ClientPath.assetsImg + '/intro-2.jpg'},
    {'img': ClientPath.assetsImg + '/intro-3.jpg'}
  ];

  int _activePage = 1;

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    int indexPage = 0;
    List<Widget> _introPageItems = introPageData.map((pageData) {
      indexPage++;
      return IntroPageItem(context, indexPage - 1, pageData);
    }).toList();

    return Scaffold(
      body: Stack(
        children: [
          PageView(
            children: _introPageItems,
            onPageChanged: (pageNumber) {
              setState(() {
                _activePage = pageNumber + 1;
              });
            },
          ),
          Positioned(
              bottom: 40.0,
              left: 15.0,
              right: 15.0,
              child: Center(
                  child: IntroPageIndicator(
                      activePage: _activePage,
                      indicatorLength: _introPageItems.length)))
        ],
      ),
    );
  }
}
