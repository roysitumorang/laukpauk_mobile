import 'package:flutter/material.dart';
import 'package:laukpauk/views/pembeli/HomePage.dart';

import 'package:laukpauk/views/pembeli/UserAndDashboard.dart';
import 'package:laukpauk/views/pembeli/Resep.dart';
import 'package:laukpauk/views/pembeli/Promo.dart';
import 'package:laukpauk/views/pembeli/PesananSaya.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class Footer extends StatefulWidget {
  FooterState createState() => FooterState();
}

class FooterState extends State<Footer> {
  int currentIndex = 0;
  List<Map<String, dynamic>> bottomNavigationMenus = [
    {'icon': 'beranda.png', 'iconText': 'Beranda', 'navigation': HomePage()},
    {'icon': 'pesanan.png', 'iconText': 'Pesanan', 'navigation': PesananSaya()},
    {'icon': 'pahlawan-dapur.png', 'iconText': 'Resep', 'navigation': Resep()},
    {'icon': 'promo.png', 'iconText': 'Promo', 'navigation': Promo()},
    {'icon': 'akun.png', 'iconText': 'Akun', 'navigation': UserAndDashboard()}
  ];

  void changeIndex(int index) async {
    Map<String, dynamic> itemMenu = bottomNavigationMenus[index];
    Widget? navigationWidget = itemMenu['navigation'];

    if (navigationWidget != null) {
      Navigator.pushAndRemoveUntil(
          context,
          MaterialPageRoute(builder: (_) => navigationWidget),
          (route) => false);
    }
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    List<BottomNavigationBarItem> navigationItems = bottomNavigationMenus
        .map((navItem) => BottomNavigationBarItem(
            icon: Padding(
              padding: EdgeInsets.only(bottom: 2.5),
              child: Image.asset(
                  ClientPath.assetsImg +
                      '/icons/bottomNavigation/' +
                      navItem['icon'],
                  width: SizeConfig.horizontalBlock * 5.5,
                  height: SizeConfig.horizontalBlock * 5.5),
            ),
            label: navItem['iconText']))
        .toList();

    return BottomNavigationBar(
        elevation: 10,
        currentIndex: currentIndex,
        selectedLabelStyle: TextStyle(fontSize: SizeConfig.horizontalBlock * 3),
        selectedItemColor: Colors.grey[400],
        unselectedItemColor: Colors.grey[400],
        showUnselectedLabels: true,
        onTap: changeIndex,
        items: navigationItems);
  }
}
