import 'package:flutter/material.dart';
import 'package:laukpauk/views/SplashScreen.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

import 'package:laukpauk/views/pembeli/components/Keranjang/PenjualCard.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/views/pembeli/components/DetailTransaksi/GiftTransaksiPembeli.dart';

class ListKeranjang extends StatelessWidget {
  final List dataKeranjangPerPenjual;
  final Function onRemoveItemSuccessed;
  final Function onEditItemSuccessed;
  final Function onRefresh;

  ListKeranjang(this.dataKeranjangPerPenjual,
      {required this.onRefresh,
      required this.onRemoveItemSuccessed,
      required this.onEditItemSuccessed});

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    List<Widget> listKeranjang = dataKeranjangPerPenjual
        .map((dataKeranjangPerPenjualItem) =>
            PenjualCard(dataKeranjangPerPenjualItem,
                onRemoveItemSuccessed: (removedItemID) {
              onRemoveItemSuccessed(removedItemID);
            }, onEditItemSuccessed: () {
              onEditItemSuccessed();
            }))
        .toList();

    return Expanded(
        child: (dataKeranjangPerPenjual.length >= 1)
            ? RefreshIndicator(
                onRefresh: () async {
                  onRefresh();
                },
                child: ListView(
                    shrinkWrap: true,
                    padding: EdgeInsets.only(top: 0),
                    children: <Widget>[...listKeranjang, FutureBuilder<String>(
                        future: LocalStorage.getCartCode(),
                        builder: (giftTransaksiPembeliContext,
                            giftTransaksiPembeliSnapshot) {
                          String? cartCode = giftTransaksiPembeliSnapshot.data;
                          if (cartCode != null) {
                            return GiftTransaksiPembeli(
                                cartCode: cartCode, useCard: true);
                          }
                          return SizedBox();
                        })]),
              )
            : LayoutBuilder(builder: (context, constraints) {
                return RefreshIndicator(
                  onRefresh: () async {
                    onRefresh();
                  },
                  child: ListView(shrinkWrap: true, children: [
                    Container(
                        constraints:
                            BoxConstraints(minHeight: constraints.maxHeight),
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Image.asset(
                                ClientPath.assetsImg + '/empty-cart.png',
                                width: SizeConfig.horizontalBlock * 85),
                            SizedBox(height: SizeConfig.horizontalBlock * 15.0),
                            CustomButton.button(context, 'Belanja Yuk', () {
                              Navigator.pushAndRemoveUntil(
                                  context,
                                  MaterialPageRoute(
                                      builder: (_) => SplashScreen()),
                                  (route) => false);
                            }, color: CustomButton.themeColor, isPill: true)
                          ],
                        ))
                  ]),
                );
              }));
  }
}
